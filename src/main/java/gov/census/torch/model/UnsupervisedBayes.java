package gov.census.torch.model;

import gov.census.torch.RecordComparator;
import gov.census.torch.counter.Counter;
import gov.census.torch.util.Util;

import java.util.Arrays;
import java.util.Random;

import cc.mallet.types.Dirichlet;

public class UnsupervisedBayes {
    public final static int BURN_IN = 500;

    public UnsupervisedBayes(Random rng, MixtureModelPrior prior, Counter counter) {
        _prior = prior;
        _cmp = counter.recordComparator();

        // TODO there should be a test to ensure that the prior is compatible 
        // with the record comparator
        // TODO user should be able to specify burnin
    }

    /**
     * Compute posterior mean parameters.
     */
    private void mcmc(Random rng, Counter counter) {
        int[] counts = counter.nonzeroCounts();
        int[][] patterns = counter.nonzeroPatterns();

        double[][][] mWeights = new double[_prior.nClasses()][_cmp.nComparators()][];
        double[] classWeights = new double[_prior.nClasses()];
        int[][] classAssign = new int[patterns.length][_prior.nClasses()];

        for (int j = 0; j < _prior.nClasses(); j++)
            for (int k = 0; k < _cmp.nComparators(); k++)
                mWeights[j][k] = new double[_cmp.nLevels(k)];

        // draw initial match weights from the prior
        drawWeights(rng, mWeights, classWeights, 
                    _prior.multinomialWeightParameter(), _prior.classWeightParameter());

        // draw initial class assignments
        drawClasses(rng, classAssign, counts, patterns, mWeights, classWeights);

        for (int n = 0; n < BURN_IN; n++) {
            drawWeights(rng, mWeights, classWeights, counts, patterns, classAssign);
            drawClasses(rng, classAssign, counts, patterns, mWeights, classWeights);
        }
    }

    private void drawClasses(Random rng, int[][] classAssign,
                             int[] counts, int[][] patterns,
                             double[][][] mWeights, double[] classWeights)
    {
        int nClasses = _prior.nClasses();
        double[][] conditionalClassProbab = new double[patterns.length][nClasses];

        for (int i = 0; i < patterns.length; i++) {
            double classTotal = 0.0;
            for (int j = 0; j < _prior.nClasses(); j++) {
                conditionalClassProbab[i][j] = classWeights[j];
                for (int k = 0; k < _cmp.nComparators(); k++) {
                    conditionalClassProbab[i][j] *= mWeights[j][k][patterns[j][k]];
                }
                classTotal += conditionalClassProbab[i][j] ;
            }

            for (int j = 0; j < _prior.nClasses(); i++) {
                conditionalClassProbab[i][j] /= classTotal;
            }
        }

        for (int i = 0; i < patterns.length; i++) {
            Arrays.fill(classAssign[i], 0);

            for (int n = 0; n < counts[i]; n++) {
                int draw = Util.sampleMultinomial(rng, conditionalClassProbab[i]);
                classAssign[i][draw]++;
            }
        }
    }

    /**
     * Draw match weights from a Dirichlet distribution with the given parameter.
     */
    private void drawWeights(Random rng, double[][][] mWeights, double[] classWeights,
                             double[][][] mParam, double[] classParam) 
    {

        double[] newClassWeights = new Dirichlet(classParam).nextDistribution();
        for (int j = 0; j < _prior.nClasses(); j++)
            classWeights[j] = newClassWeights[j];

        for (int j = 0; j < _prior.nClasses(); j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                double[] newWeights = new Dirichlet(mParam[j][k]).nextDistribution();
                for (int x = 0; x < _cmp.nLevels(k); x++)
                    mWeights[j][k][x] = newWeights[x];
            }
        }
    }

    /**
     * Compute posterior parameters and draw new weights.
     */
    private void drawWeights(Random rng, double[][][] mWeights, double[] classWeights,
                             int[] counts, int[][] patterns, int[][] classAssign) 
    {
        double[][][] mPrior = _prior.multinomialWeightParameter();
        double[] classPrior = _prior.classWeightParameter();

        double[] classParam = Arrays.copyOf(classPrior, classPrior.length);
        double[][][] mParam = new double[classPrior.length][_cmp.nComparators()][];

        for (int j = 0; j < _prior.nClasses(); j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                mParam[j][k] = Arrays.copyOf(mPrior[j][k], _cmp.nLevels(k));
            }
        }

        for (int i = 0; i < patterns.length; i++) {
            for (int j = 0; j < _prior.nClasses(); j++) {
                classParam[j] += classAssign[i][j];

                for (int k = 0; k < _cmp.nComparators(); k++) {
                    mParam[j][k][patterns[i][k]] += classAssign[i][j];
                }
            }
        }

        drawWeights(rng, mWeights, classWeights, mParam, classParam);
    }

    private final MixtureModelPrior _prior;
    private final RecordComparator _cmp;
}
