package gov.census.torch.model;

import gov.census.torch.counter.Counter;
import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.Random;

public class ConditionalIndependenceModel {

    public final static double TOLERANCE = 0.0000001;
    public final static int MAX_ITER = 500;

    public ConditionalIndependenceModel(Random rng, Counter counter, int nClasses)
    {
        if (nClasses < 1)
            throw new IllegalArgumentException("'nClasses' must be greater than 0");

        _nClasses = nClasses;
        _cmp = counter.recordComparator();

        _matchWeights = new double[nClasses][_cmp.nComparators()][];
        for (int i = 0; i < nClasses; i++) {
            for (int j = 0; j < _cmp.nComparators(); j++) {
                _matchWeights[i][j] = new double[_cmp.nLevels(j)];
            }
        }

        _classWeights = new double[nClasses];

        estimate(rng, counter);
    }

    public ConditionalIndependenceModel(Counter counter, int nClasses) {
        this(new Random(), counter, nClasses);
    }

    /**
     * Fill <code>ary</code> with a partition of 1.
     */
    public void partitionOne(Random rng, double[] ary) {
        double[] u = new double[ary.length - 1];
        for (int i = 0; i < u.length; i++)
            u[i] = rng.nextDouble();

        Arrays.sort(u);
        double uLast = 0.0;
        for (int i = 0; i < u.length; i++) {
            ary[i] = u[i] - uLast;
            uLast = u[i];
        }

        ary[ary.length - 1] = 1.0 - uLast;
    }

    /**
     * Initialize the weight arrays. For now, this method creates random partitions
     * of unity.
     */
    public void initWeights(Random rng) {
        partitionOne(rng, _classWeights);

        for (int i = 0; i < _nClasses; i++) {
            for (int j = 0; j < _cmp.nComparators(); j++) {
                partitionOne(rng, _matchWeights[i][j]);
            }
        }
    }

    public int nClasses() {
        return _nClasses;
    }

    /**
     * Return the matching weights for this model. The matching weights are stored
     * in a three-dimensional array indexed by class, comparator, and level. For
     * example, the probability that the kth comparator value is x, conditional on
     * being in the jth class is <code>matchWeights()[j][k][x]</code>.
     */
    public double[][][] matchWeights() {
        return _matchWeights;
    }

    /**
     * Return the marginal probability of each class.
     */
    public double[] classWeights() {
        return _classWeights;
    }

    private void estimate(Random rng, Counter counter) {

        int[] counts = counter.nonzeroCounts();
        int[][] patterns = counter.nonzeroPatterns();

        initWeights(rng);

        double[][] expectedClass = new double[patterns.length][_nClasses];
        for (int i = 0; i < patterns.length; i++)
            partitionOne(rng, expectedClass[i]);

        double oldll = logLikelihood(counts, patterns, expectedClass);
        double newll, delta;

        for (int iter = 1; iter <= MAX_ITER; iter++) {
            estep(patterns, expectedClass);
            mstep(counts, patterns, expectedClass);

            newll = logLikelihood(counts, patterns, expectedClass);
            delta = newll - oldll;

            if (iter % 10 == 0) {
                System.out.println("iteration: " + iter + 
                                   ", likelihood: " + newll + 
                                   ", delta: " + delta);
            }

            // TODO: should the first test be necessary?
            if (delta >= 0 && delta < TOLERANCE) {
                System.out.println("iteration: " + iter + 
                                   ", likelihood: " + newll + 
                                   ", delta: " + delta);
                break;
            }

            oldll = newll;
        }
    }

    private double logLikelihood(int[] counts, int[][] patterns, double[][] expectedClass) 
    {
        double ll = 0.0;
        int patternLength = _cmp.nComparators();

        for (int i = 0; i < patterns.length; i++) {
            double patll = 0.0;

            for (int j = 0; j < _nClasses; j++) {
                if (expectedClass[i][j] == 0)
                    continue;

                for (int k = 0; k < patternLength; k++) {
                    patll += Math.log(_matchWeights[j][k][patterns[i][k]]);
                }

                patll += Math.log(_classWeights[j]);
                patll *= expectedClass[i][j];
            }

            ll += patll * counts[i];
        }

        return ll;
    }

    private void estep(int[][] patterns, double[][] expectedClass) 
    {
        int patternLength = _cmp.nComparators();

        for (int i = 0; i < patterns.length; i++) {
            double patternTotal = 0.0;

            for (int j = 0; j < _nClasses; j++) {
                expectedClass[i][j] = _classWeights[j];

                for (int k = 0; k < patternLength; k++) {
                    expectedClass[i][j] *= _matchWeights[j][k][patterns[i][k]];
                }

                patternTotal += expectedClass[i][j];
            }

            for (int j = 0; j < _nClasses; j++)
                expectedClass[i][j] /= patternTotal;
        }
    }

    private void mstep(int[] counts, int[][] patterns, double[][] expectedClass) 
    {
        for (int i = 0; i < _nClasses; i++) {
            for (int j = 0; j < _cmp.nComparators(); j++)
                Arrays.fill(_matchWeights[i][j], 0.0);
        }

        double[] classTotal = new double[_nClasses];
        double countTotal = 0.0;
        for (int j = 0; j < _nClasses; j++) {

            for (int u = 0; u < patterns.length; u++) {
                double d = expectedClass[u][j] * counts[u];
                classTotal[j] += d;
                countTotal += d;

                for (int k = 0; k < _cmp.nComparators(); k++) {
                    _matchWeights[j][k][patterns[u][k]] += d;
                }
            }

            for (int k = 0; k < _cmp.nComparators(); k++) {
                for (int x = 0; x < _matchWeights[j][k].length; x++)
                    _matchWeights[j][k][x] /= classTotal[j];
            }
        }

        for (int j = 0; j < _nClasses; j++)
            _classWeights[j] = classTotal[j] / countTotal;
    }

    private final int _nClasses;
    private final RecordComparator _cmp;
    private final double[][][] _matchWeights;
    private final double[] _classWeights;
}
