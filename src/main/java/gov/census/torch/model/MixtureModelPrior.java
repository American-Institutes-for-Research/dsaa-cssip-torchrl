package gov.census.torch.model;

import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MixtureModelPrior {

    public static class Builder {
        public Builder(RecordComparator cmp) {
            _cmp = cmp;
            _matchWeights = new HashMap<>();
            _nonmatchWeights = new HashMap<>();
            _classWeightParam = null;
            _priorWeight = 1.0;
        }

        public Builder withField(String name) {
            _currentField = name;

            Integer nLevels;
            if ((nLevels = _cmp.nLevels(name)) == null) {
                String msg = String.format("Not a valid comparison field: '%s'", name);
                throw new IllegalArgumentException(msg);
            }
            _currentNLevels = nLevels.intValue();
            
            if (!_matchWeights.containsKey(name))
                _matchWeights.put(name, new LinkedList<double[]>());

            if (!_nonmatchWeights.containsKey(name))
                _nonmatchWeights.put(name, new LinkedList<double[]>());

            return this;
        }

        public Builder matchClass(double... weights) {
            if (weights.length != _currentNLevels) {
                String msg = "Expected %d levels for '%s', received %d";
                msg = String.format(msg, _currentNLevels, _currentField, weights.length);
                throw new IllegalArgumentException(msg);
            }

            _matchWeights.get(_currentField).add(Arrays.copyOf(weights, weights.length));
            return this;
        }

        public Builder nonmatchClass(double... weights) {
            if (weights.length != _currentNLevels) {
                String msg = "Expected %d levels for '%s', received %d";
                msg = String.format(msg, _currentNLevels, _currentField, weights.length);
                throw new IllegalArgumentException(msg);
            }

            _nonmatchWeights.get(_currentField).add(Arrays.copyOf(weights, weights.length));
            return this;
        }

        public Builder classWeights(double... weights) {
            _classWeightParam = weights;
            return this;
        }

        public Builder priorWeight(double d) {
            _priorWeight = d;
            return this;
        }

        public MixtureModelPrior build() {
            String[] compareFields = _cmp.compareFields();
            int nMatchClass = 0;
            int nNonmatchClass = 0;
            double[][][] mWeightParam = null;

            if (_classWeightParam == null) {
                throw new IllegalArgumentException("No class weights given");
            }

            for (int j = 0; j < _classWeightParam.length; j++)
                _classWeightParam[j] *= _priorWeight;

            for (int k = 0; k < compareFields.length; k++) {
                String name = compareFields[k];
                if (!_matchWeights.containsKey(name)) {
                    String msg = "No match class specified for '%s'";
                    throw new IllegalArgumentException(String.format(msg, name));
                }

                if (!_nonmatchWeights.containsKey(name)) {
                    String msg = "No nonmatch class specified for '%s'";
                    throw new IllegalArgumentException(String.format(msg, name));
                }

                List<double[]> mlist = _matchWeights.get(name);
                List<double[]> ulist = _nonmatchWeights.get(name);

                if (k == 0) {
                    nMatchClass = mlist.size();
                    nNonmatchClass = ulist.size();
                    int nClass = nMatchClass + nNonmatchClass;
                    mWeightParam = new double[nClass][_cmp.nComparators()][];

                } else {
                    if (mlist.size() != nMatchClass) {
                        String msg = "Specify the same number of match classes for each field";
                        throw new IllegalArgumentException(msg);
                    }

                    if (ulist.size() != nNonmatchClass) {
                        String msg = "Specify the same number of nonmatch classes for each field";
                        throw new IllegalArgumentException(msg);
                    }
                }

                for (int j = 0; j < nMatchClass; j++) {
                    double[] ary = mlist.get(j);
                    for (int x = 0; x < ary.length; x++)
                        ary[x] *= _priorWeight;
                    mWeightParam[j][k] = ary;
                }

                for (int j = 0; j < nNonmatchClass; j++) {
                    double[] ary = ulist.get(j);
                    for (int x = 0; x < ary.length; x++)
                        ary[x] *= _priorWeight;
                    mWeightParam[nMatchClass + j][k] = ary;
                }
            }

            return new MixtureModelPrior(_cmp, mWeightParam, _classWeightParam, nMatchClass);
        }

        private final RecordComparator _cmp;
        private final HashMap<String, List<double[]>> _matchWeights;
        private final HashMap<String, List<double[]>> _nonmatchWeights;

        private String _currentField;
        private int _currentNLevels;
        private double[] _classWeightParam;
        private double _priorWeight;
    }

    public MixtureModelPrior(RecordComparator cmp, double[][][] mWeightParam, 
                             double[] classWeightParam, int nMatchClasses) 
    {
        _mWeightParam = mWeightParam;
        _classWeightParam = classWeightParam;
        _nClasses = classWeightParam.length;
        _nMatchClasses = nMatchClasses;

        if (_nClasses < 2)
            throw new IllegalArgumentException("need at least two classes");

        if (_nMatchClasses == _nClasses)
            throw new IllegalArgumentException("need at least one nonmatch class");

        if (mWeightParam.length != _nClasses)
            throw new IllegalArgumentException("mWeightParam.length != classWeightParam.length");

         // check the dimensions of mWeightParam
        String err1 = "mWeightParam[%d].length incompatible with comparator";
        String err2 = "mWeightParam[%d][%d].length incompatible with comparator";
        for (int j = 0; j < _nClasses; j++) {
            if (mWeightParam[j].length != cmp.nComparators())
                throw new IllegalArgumentException(String.format(err1, j));
            for (int k = 0; k < cmp.nComparators(); k++)
                if (mWeightParam[j][k].length != cmp.nLevels(k))
                    throw new IllegalArgumentException(String.format(err2, j, k));
        }

   }

    public int nClasses() {
        return _nClasses;
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
    private final int _nClasses, _nMatchClasses;
}
