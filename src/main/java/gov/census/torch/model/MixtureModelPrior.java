package gov.census.torch.model;

import cc.mallet.types.Dirichlet;

public class MixtureModelPrior {

    public MixtureModelPrior(double[][][] multinomialParam, double[] classParam) {
        _multinomialParam = multinomialParam;
        _classParam = classParam;
    }

    private final double[][][] _multinomialParam;
    private final double[]  _classParam;
}
