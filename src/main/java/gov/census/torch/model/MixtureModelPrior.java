package gov.census.torch.model;

import cc.mallet.types.Dirichlet;

public class MixtureModelPrior {

    public MixtureModelPrior(double[][][] mWeightParam, double[] classWeightParam) {
        _mWeightParam = mWeightParam;
        _classWeightParam = classWeightParam;
    }

    public int nClasses() {
        return _classWeightParam.length;
    }

    public double[][][] multinomialWeightParameter() {
        return _mWeightParam;
    }

    public double[] classWeightParameter() {
        return _classWeightParam;
    }

    private final double[][][] _mWeightParam;
    private final double[]  _classWeightParam;
}
