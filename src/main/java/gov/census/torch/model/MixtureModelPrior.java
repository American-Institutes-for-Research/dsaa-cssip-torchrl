package gov.census.torch.model;

import cc.mallet.types.Dirichlet;

public class MixtureModelPrior {

    public MixtureModelPrior(double[][][] mWeightParam, double[] classWeightParam, int nMatchClasses) 
    {
        _mWeightParam = mWeightParam;
        _classWeightParam = classWeightParam;
        _nMatchClasses = nMatchClasses;
        int nClasses = classWeightParam.length;

        if (nClasses < 2)
            throw new IllegalArgumentException("need at least two classes");

        if (nMatchClasses == nClasses)
            throw new IllegalArgumentException("need at least one nonmatch class");

        if (mWeightParam.length != nClasses)
            throw new IllegalArgumentException("mWeightParam.length != classWeightParam.length");
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

    public int nMatchClasses() {
        return _nMatchClasses;
    }

    private final double[][][] _mWeightParam;
    private final double[]  _classWeightParam;
    private final int _nMatchClasses;
}
