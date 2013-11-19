package gov.census.torch.model;

import gov.census.torch.counter.Tally;

import java.util.Arrays;

/**
 * A convenience class to collect maximum-likelihood estimators.
 */
public class ML {
    /**
     * Fit a mixture model tow unlabeled data. This method assumes that the latent class with
     * the smallest probability is the unique match class.
     */
    public static MixtureModel fit(Tally tally, int nClasses) {
        UnsupervisedLearner lr = new UnsupervisedLearner(tally, nClasses);
        double[] classWeights = lr.classWeights();
        double minWeight = Double.MAX_VALUE;
        int minIndex = -1;

        for (int j = 0; j < nClasses; j++) {
            if (classWeights[j] < minWeight) {
                minIndex = j;
                minWeight = classWeights[j];
            }
        }

        lr.setMatchClass(minIndex, true);
        System.out.println(fitMessage(classWeights));
        return lr.model();
    }

    /**
     * Fit a mixture model to labeled data.
     */
    public static MixtureModel fit(Tally[] tallies, int nMatchClasses) {
        SupervisedLearner lr = new SupervisedLearner(tallies, nMatchClasses);
        return lr.model();
    }

    /**
     * Fit a mixture model using labeled and unlabeled data.
     *
     * @param lambda the relative weight of labeled to unlabeled data.
     */
    public static MixtureModel fit(Tally unlabeled, Tally[] labeled,
                                   int nMatchClasses, double lambda)
    {
        SemisupervisedLearner lr = 
            new SemisupervisedLearner(unlabeled, labeled, nMatchClasses, lambda);
        System.out.println(fitMessage(lr.classWeights()));
        return lr.model();
    }

    private static String fitMessage(double[] classWeights) {
        StringBuilder b = new StringBuilder();
        b.append(String.format("Fitted %d classes with weights ( ", classWeights.length));

        for (int j = 0; j < classWeights.length; j++) {
            b.append(String.format("%6.4f ", classWeights[j]));
        }

        b.append(")");
        return b.toString();
    }

}
