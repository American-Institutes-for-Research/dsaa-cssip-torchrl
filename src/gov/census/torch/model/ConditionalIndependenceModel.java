package gov.census.torch.model;

import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.Random;

public class ConditionalIndependenceModel {

    public final static double TOLERANCE = 0.0000001;
    public final static int MAX_ITER = 500;

    public ConditionalIndependenceModel(RecordComparator cmp, int nClasses)
    {
        if (nClasses < 1)
            throw new IllegalArgumentException("'nClasses' must be greater than 0");

        _nClasses = nClasses;
        this.cmp = cmp;

        _matchWeights = new double[nClasses][cmp.nComparators()][];
        for (int i = 0; i < nClasses; i++) {
            for (int j = 0; j < cmp.nComparators(); j++) {
                _matchWeights[i][j] = new double[cmp.nLevels(j)];
            }
        }

        _classWeights = new double[nClasses];
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
            for (int j = 0; j < cmp.nComparators(); j++) {
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

    public void estimate(int[] counts) {
        this.estimate(new Random(), counts);
    }

    public void estimate(Random rng, int[] counts) {

        if (counts.length != cmp.nPatterns())
            throw new IllegalArgumentException("counts.length != cmp.nPatterns()");

        // count the number of nonzero patterns and create an index
        int nNonzeroPatterns = 0;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0)
                nNonzeroPatterns++;
        }

        int[] nonzeroPatternIndex = new int[nNonzeroPatterns];
        int[][] nonzeroPatterns = new int[nNonzeroPatterns][cmp.nComparators()];
        int insertIndex = 0;

        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                nonzeroPatternIndex[insertIndex] = i;
                int[] pattern = cmp.patternFor(i);

                for (int j = 0; j < pattern.length; j++) {
                    nonzeroPatterns[insertIndex][j] = pattern[j];
                }
                insertIndex++;
            }
        }

        initWeights(rng);

        double[][] expectedClass = new double[nNonzeroPatterns][_nClasses];
        for (int i = 0; i < nNonzeroPatterns; i++)
            partitionOne(rng, expectedClass[i]);

        double oldll = logLikelihood(nonzeroPatternIndex, 
                                     nonzeroPatterns, 
                                     counts, 
                                     expectedClass);
        double newll, delta;

        for (int iter = 1; iter <= MAX_ITER; iter++) {
            estep(nonzeroPatternIndex, nonzeroPatterns, expectedClass);
            mstep(counts, nonzeroPatternIndex, nonzeroPatterns, expectedClass);

            newll = logLikelihood(nonzeroPatternIndex, 
                                  nonzeroPatterns, 
                                  counts, 
                                  expectedClass);
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

    private double logLikelihood(int[] nonzeroPatternIndex, int[][] nonzeroPatterns,
                                 int[] counts, double[][] expectedClass) 
    {
        double ll = 0.0;
        int patternLength = cmp.nComparators();

        for (int i = 0; i < nonzeroPatternIndex.length; i++) {
            double patll = 0.0;

            for (int j = 0; j < _nClasses; j++) {
                if (expectedClass[i][j] == 0)
                    continue;

                for (int k = 0; k < patternLength; k++) {
                    patll += Math.log(_matchWeights[j][k][nonzeroPatterns[i][k]]);
                }

                patll += Math.log(_classWeights[j]);
                patll *= expectedClass[i][j];
            }

            ll += patll * counts[nonzeroPatternIndex[i]];
        }

        return ll;
    }

    private void estep(int[] nonzeroPatternIndex, int[][] nonzeroPatterns, 
                       double[][] expectedClass) 
    {
        int patternLength = cmp.nComparators();

        for (int i = 0; i < nonzeroPatternIndex.length; i++) {
            double patternTotal = 0.0;

            for (int j = 0; j < _nClasses; j++) {
                expectedClass[i][j] = _classWeights[j];

                for (int k = 0; k < patternLength; k++) {
                    expectedClass[i][j] *= _matchWeights[j][k][nonzeroPatterns[i][k]];
                }

                patternTotal += expectedClass[i][j];
            }

            for (int j = 0; j < _nClasses; j++)
                expectedClass[i][j] /= patternTotal;
        }
    }

    private void mstep(int[] counts,
                       int[] nonzeroPatternIndex, 
                       int[][] nonzeroPatterns,
                       double[][] expectedClass) 
    {
        for (int i = 0; i < _nClasses; i++) {
            for (int j = 0; j < cmp.nComparators(); j++)
                Arrays.fill(_matchWeights[i][j], 0.0);
        }

        double[] classTotal = new double[_nClasses];
        double countTotal = 0.0;
        for (int j = 0; j < _nClasses; j++) {

            for (int u = 0; u < nonzeroPatterns.length; u++) {
                double d = expectedClass[u][j] * counts[nonzeroPatternIndex[u]];
                classTotal[j] += d;
                countTotal += d;

                for (int k = 0; k < cmp.nComparators(); k++) {
                    _matchWeights[j][k][nonzeroPatterns[u][k]] += d;
                }
            }

            for (int k = 0; k < cmp.nComparators(); k++) {
                for (int x = 0; x < _matchWeights[j][k].length; x++)
                    _matchWeights[j][k][x] /= classTotal[j];
            }
        }

        for (int j = 0; j < _nClasses; j++)
            _classWeights[j] = classTotal[j] / countTotal;


    }

    private final int _nClasses;
    private final RecordComparator cmp;
    private final double[][][] _matchWeights;
    private final double[] _classWeights;
}
