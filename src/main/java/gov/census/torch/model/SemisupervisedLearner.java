package gov.census.torch.model;

import gov.census.torch.counter.Counter;
import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.Random;

/**
 * TODO: This class has not been tested and debugged.
 */
public class SemisupervisedLearner
{
    public final static double TOLERANCE = 0.0000001;
    public final static int MAX_ITER = 50000;

    public SemisupervisedLearner(Random rng, Counter unlabeled,
                                 Counter[] labeled, int nMatchClasses,
                                 double lambda)
    {
        if (labeled.length < 2)
            throw new IllegalArgumentException("need at least two classes");

        if (nMatchClasses >= labeled.length - 1)
            throw new IllegalArgumentException("need at least one nonmatch class");

        for (int j = 0; j < labeled.length; j++) {
            if (unlabeled.recordComparator() != labeled[j].recordComparator())
                throw new IllegalArgumentException(
                        "all counters should have the same record comparator");

        }

        _nClasses = labeled.length;
        _nMatchClasses = nMatchClasses;
        _cmp = unlabeled.recordComparator();
        _lambda = lambda;
        _mWeights = new double[_nClasses][_cmp.nComparators()][];
        _logMWeights = new double[_nClasses][_cmp.nComparators()][];
        for (int j = 0; j < _nClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                _mWeights[j][k] = new double[_cmp.nLevels(k)];
                _logMWeights[j][k] = new double[_cmp.nLevels(k)];
            }
        }

        _classWeights = new double[_nClasses];

        estimate(rng, labeled, unlabeled);
        _model = new MixtureModel(_cmp, _mWeights, _nMatchClasses);
    }

    public SemisupervisedLearner(Counter unlabeled, 
                                 Counter[] labeled, int nMatchClasses, double lambda) 
    {
        this(new Random(), unlabeled, labeled, nMatchClasses, lambda);
    }

    public double[] classWeights() {
        return _classWeights;
    }

    public MixtureModel model() {
        return _model;
    }

    /**
     * Initialize the weight arrays.
     */
    private void initWeights(Random rng) {
        UnsupervisedLearner.partitionOne(rng, _classWeights);

        for (int i = 0; i < _nClasses; i++) {
            for (int j = 0; j < _cmp.nComparators(); j++) {
                UnsupervisedLearner.partitionOne(rng, _mWeights[i][j]);
            }
        }
    }

    /**
     * Approximate the ML estimate for the given counts.
     */
    private void estimate(Random rng, Counter[] labeled, Counter unlabeled) {
        int[] ucounts = unlabeled.nonzeroCounts();
        int[][] upats = unlabeled.nonzeroPatterns();

        int[][] lcounts = new int[_nClasses][];
        int[][][] lpats = new int[_nClasses][][];
        double[][][] lmWeights = new double[_nClasses][_cmp.nComparators()][];
        double[][][] umWeights = new double[_nClasses][_cmp.nComparators()][];

        for (int j = 0; j < _nClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                lmWeights[j][k] = new double[_cmp.nLevels(k)];
                umWeights[j][k] = new double[_cmp.nLevels(k)];
            }
        }

        for (int j = 0; j < _nClasses; j++) {
            lcounts[j] = labeled[j].nonzeroCounts();
            lpats[j] = labeled[j].nonzeroPatterns();
        }

        initWeights(rng);

        double[][] expectedClass = new double[upats.length][_nClasses];
        for (int i = 0; i < upats.length; i++)
            UnsupervisedLearner.partitionOne(rng, expectedClass[i]);

        double oldll = logLikelihood(lcounts, lpats, ucounts, upats, expectedClass);
        double newll, delta;

        System.out.format("%10s%16s%16s%n", "iteration", "likelihood", "delta");

        for (int iter = 1; iter <= MAX_ITER; iter++) {
            estep(upats, expectedClass);
            mstep(lcounts, lpats, ucounts, upats, expectedClass, lmWeights, umWeights);

            newll = logLikelihood(lcounts, lpats, ucounts, upats, expectedClass);
            delta = newll - oldll;

            if (iter < 101 && iter % 10 == 0 ||
                iter < 1001 && iter % 100 == 0 ||
                iter % 1000 == 0) 
            {
                System.out.format("%10d%16.7f%16.7f%n", iter, newll, delta);
            }

            if (delta >= 0 && delta < TOLERANCE) {
                System.out.format("%10d%16.7f%16.7f%n", iter, newll, delta);
                break;
            }

            oldll = newll;
        }

        for (int j = 0; j < _mWeights.length; j++)
            for (int k = 0; k < _cmp.nComparators(); k++)
                for (int x = 0; x < _cmp.nLevels(k); x++)
                    _logMWeights[j][k][x] = Math.log(_mWeights[j][k][x]);
    }

    private double logLikelihood(int[][] lcounts, int[][][] lpats,
                                 int[] ucounts, int[][] upats, double[][] expectedClass) 
    {
        double lll = 0.0; // log-likelihood of the labeled portion
        double ull = 0.0; // log-likelihood of the unlabeled portion
        int patternLength = _cmp.nComparators();

        for (int j = 0; j < _nClasses; j++) {
            for (int i = 0; i < lcounts[j].length; i++) {
                double patll = 0.0;

                for (int k = 0; k < patternLength; k++)
                    patll += Math.log(_mWeights[j][k][lpats[j][i][k]]);

                patll += Math.log(_classWeights[j]);
                lll += patll * lcounts[j][i];
            }
        }

        for (int i = 0; i < upats.length; i++) {
            double patll = 0.0;

            for (int j = 0; j < _nClasses; j++) {
                if (expectedClass[i][j] == 0)
                    continue;

                for (int k = 0; k < patternLength; k++) {
                    patll += Math.log(_mWeights[j][k][upats[i][k]]);
                }

                patll += Math.log(_classWeights[j]);
                patll *= expectedClass[i][j];
            }

            ull += patll * ucounts[i];
        }

        return _lambda * lll + (1 - _lambda) * ull;
    }

    private void estep(int[][] upats, double[][] expectedClass) {
        int patternLength = _cmp.nComparators();

        for (int i = 0; i < upats.length; i++) {
            double patternTotal = 0.0;

            for (int j = 0; j < _nClasses; j++) {
                expectedClass[i][j] = _classWeights[j];

                for (int k = 0; k < patternLength; k++) {
                    expectedClass[i][j] *= _mWeights[j][k][upats[i][k]];
                }

                patternTotal += expectedClass[i][j];
            }

            for (int j = 0; j < _nClasses; j++)
                expectedClass[i][j] /= patternTotal;
        }
    }

    private void mstep(int[][] lcounts, int[][][] lpats,
                       int[] ucounts, int[][] upats, double[][] expectedClass,
                       double[][][] lmWeights, double[][][] umWeights) 
    {
        for (int j = 0; j < _nClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                Arrays.fill(lmWeights[j][k], 0.0);
                Arrays.fill(umWeights[j][k], 0.0);
            }
        }

        double[] lClassTotal = new double[_nClasses];
        double[] uClassTotal = new double[_nClasses];
        double[] classTotal = new double[_nClasses];
        double lTotal = 0.0;
        double uTotal = 0.0;

        for (int j = 0; j < _nClasses; j++) {
            for (int i = 0; i < lpats[j].length; i++) {
                lClassTotal[j] += lcounts[j][i];
                lTotal += lcounts[j][i];

                for (int k = 0; k < _cmp.nComparators(); k++) {
                    lmWeights[j][k][lpats[j][i][k]] += lcounts[j][i];
                }
            }

            for (int i = 0; i < upats.length; i++) {
                double d = expectedClass[i][j] * ucounts[i];
                uClassTotal[j] += d;
                uTotal += d;

                for (int k = 0; k < _cmp.nComparators(); k++) {
                    umWeights[j][k][upats[i][k]] += d;
                }
            }

            for (int k = 0; k < _cmp.nComparators(); k++) {
                for (int x = 0; x < _mWeights[j][k].length; x++) {
                    _mWeights[j][k][x] = 
                        (_lambda * lmWeights[j][k][x] + (1 - _lambda) * umWeights[j][k][x]) /
                        (_lambda * lClassTotal[j] + (1 - _lambda) * uClassTotal[j]);
                }
            }
        }

        for (int j = 0; j < _nClasses; j++) {
            _classWeights[j] =
                (_lambda * lClassTotal[j] + (1 - _lambda) * uClassTotal[j]) /
                (_lambda * lTotal + (1 - _lambda) * uTotal);
        }
    }

    private final int _nClasses, _nMatchClasses;
    private final RecordComparator _cmp;
    private final double _lambda;
    private final double[][][] _mWeights, _logMWeights;
    private final double[] _classWeights;
    private final MixtureModel _model;
}
