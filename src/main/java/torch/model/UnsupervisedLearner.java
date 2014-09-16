package torch.model;

import torch.counter.Counter;
import torch.RecordComparator;

import java.util.Arrays;
import java.util.Random;

public class UnsupervisedLearner
{

    public final static double TOLERANCE = 0.0000001;
    public final static int MAX_ITER = 50000;

    /**
     * Construct a new UnsupervisedLearner by fitting unlabeled data.  After construction, it's up
     * to the user to declare which of the classes correspond to matches for the purpose of
     * computing match weights.
     */
    public UnsupervisedLearner(Random rng, Counter counter, int nClasses)
    {
        if (nClasses < 2)
            throw new IllegalArgumentException("'nClasses' must be greater than 1");

        _nClasses = nClasses;
        _matchClass = new boolean[nClasses];
        _cmp = counter.recordComparator();

        _mWeights = new double[nClasses][_cmp.nComparators()][];
        for (int i = 0; i < nClasses; i++) {
            for (int j = 0; j < _cmp.nComparators(); j++) {
                _mWeights[i][j] = new double[_cmp.nLevels(j)];
            }
        }

        _classWeights = new double[nClasses];

        estimate(rng, counter);

        _model = null;
    }

    public UnsupervisedLearner(Counter counter, int nClasses) {
        this(new Random(), counter, nClasses);
    }

    /**
     * Fill <code>ary</code> with a partition of 1.
     */
    public static void partitionOne(Random rng, double[] ary) {
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

    public int nClasses() {
        return _nClasses;
    }

    /**
     * Return the marginal probability of each class.
     */
    public double[] classWeights() {
        return _classWeights;
    }

    public void setMatchClass(int j, boolean isMatchClass) {
        _matchClass[j] = isMatchClass;
    }

    /**
     * Returns the mixture model defined by the fitted probabilities. The first time this method
     * is called it constructs the model. This will fail if there are no match classes.
     */
    public MixtureModel model() {
        if (_model == null) {
            // shuffle _mWeights so that the match classes are at the front
            double[][][] mWeights = new double[_nClasses][][];

            int nMatchClasses = 0;
            for (int j = 0; j < _nClasses; j++) {
                if (_matchClass[j]) {
                    mWeights[nMatchClasses++] = _mWeights[j];
                }
            }

            int i = nMatchClasses;
            for (int j = 0; j < _nClasses; j++) {
                if (!_matchClass[j]) {
                    mWeights[i++] = _mWeights[j];
                }
            }

            _model = new MixtureModel(_cmp, mWeights, nMatchClasses);
        }

        return _model;
    }

    /**
     * Initialize the weight arrays. For now, this method creates random partitions
     * of unity.
     */
    private void initWeights(Random rng) {
        partitionOne(rng, _classWeights);

        for (int i = 0; i < _nClasses; i++) {
            for (int j = 0; j < _cmp.nComparators(); j++) {
                partitionOne(rng, _mWeights[i][j]);
            }
        }
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

        System.out.format("%10s%16s%16s%n", "iteration", "likelihood", "delta");

        for (int iter = 1; iter <= MAX_ITER; iter++) {
            estep(patterns, expectedClass);
            mstep(counts, patterns, expectedClass);

            newll = logLikelihood(counts, patterns, expectedClass);
            delta = newll - oldll;

            if (iter < 101 && iter % 10 == 0 ||
                iter < 1001 && iter % 100 == 0 ||
                iter % 1000 == 0) 
            {
                System.out.format("%10d%16.7f%16.7f%n", iter, newll, delta);
            }

            // TODO: should the first test be necessary?
            if (delta >= 0 && delta < TOLERANCE) {
                System.out.format("%10d%16.7f%16.7f%n", iter, newll, delta);
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
                    patll += Math.log(_mWeights[j][k][patterns[i][k]]);
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
                    expectedClass[i][j] *= _mWeights[j][k][patterns[i][k]];
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
                Arrays.fill(_mWeights[i][j], 0.0);
        }

        double[] classTotal = new double[_nClasses];
        double countTotal = 0.0;
        for (int j = 0; j < _nClasses; j++) {

            for (int u = 0; u < patterns.length; u++) {
                double d = expectedClass[u][j] * counts[u];
                classTotal[j] += d;
                countTotal += d;

                for (int k = 0; k < _cmp.nComparators(); k++) {
                    _mWeights[j][k][patterns[u][k]] += d;
                }
            }

            for (int k = 0; k < _cmp.nComparators(); k++) {
                for (int x = 0; x < _mWeights[j][k].length; x++)
                    _mWeights[j][k][x] /= classTotal[j];
            }
        }

        for (int j = 0; j < _nClasses; j++)
            _classWeights[j] = classTotal[j] / countTotal;
    }

    private final int _nClasses;
    private final boolean[] _matchClass;
    private final RecordComparator _cmp;
    private final double[][][] _mWeights;
    private final double[] _classWeights;
    private MixtureModel _model;
}
