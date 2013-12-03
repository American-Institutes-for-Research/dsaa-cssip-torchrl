package gov.census.torch.model;

import gov.census.torch.counter.Counter;

import java.util.Random;

public class UnsupervisedBayes {
    public final static int BURN_IN = 500;

    public UnsupervisedBayes(MixtureModelPrior prior) {
        _prior = prior;
    }

    /**
     * Compute posterior mean parameters.
     */
    private void mcmc(Random rng, Counter counter) {
        int[] counts = counter.nonzeroCounts();
        int[][] patterns = counter.nonzeroPatterns();

        int[] classAssign = new int[counter.total()];
    }

    private void drawClasses(double[] classPrior, int[] classAssign) {
        int i = 0;
        while (i < classAssign.length) {

        }
    }

    private final MixtureModelPrior _prior;
}
