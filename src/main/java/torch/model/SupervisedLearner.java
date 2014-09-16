package torch.model;

import torch.RecordComparator;
import torch.counter.Counter;

import java.util.Random;

public class SupervisedLearner
{
    public SupervisedLearner(Random rng, Counter[] counters, int nMatchClasses)
    {
        if (counters.length < 2)
            throw new IllegalArgumentException("need at least two classes");

        if (nMatchClasses > counters.length - 1)
            throw new IllegalArgumentException("need at least one nonmatch class");

        for (int j = 1; j < counters.length; j++) {
            if (counters[0].recordComparator() != counters[j].recordComparator())
                throw new IllegalArgumentException(
                        "all counters should have the same record comparator");

        }

        _nClasses = counters.length;
        _nMatchClasses = nMatchClasses;
        _cmp = counters[0].recordComparator();
        _mWeights = new double[_nClasses][_cmp.nComparators()][];
        _logMWeights = new double[_nClasses][_cmp.nComparators()][];
        for (int j = 0; j < _nClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                _mWeights[j][k] = new double[_cmp.nLevels(k)];
                _logMWeights[j][k] = new double[_cmp.nLevels(k)];
            }
        }

        estimate(rng, counters);
        _model = new MixtureModel(_cmp, _mWeights, _nMatchClasses);
    }

    public SupervisedLearner(Counter[] counters, int nMatchClasses) {
        this(new Random(), counters, nMatchClasses);
    }

    public MixtureModel model() {
        return _model;
    }

    /**
     * Compute the ML estimate for the labeled data.
     */
    private void estimate(Random rng, Counter[] counters) {
        for (int j = 0; j < _nClasses; j++) {
            double classTotal = 0.0;
            int[] counts = counters[j].nonzeroCounts();
            int[][] patterns = counters[j].nonzeroPatterns();

            for (int i = 0; i < patterns.length; i++) {
                classTotal += counts[i];

                for (int k = 0; k < _cmp.nComparators(); k++) {
                    _mWeights[j][k][patterns[i][k]] += counts[i];
                }
            }

            for (int k = 0; k < _cmp.nComparators(); k++) {
                for (int x = 0; x < _mWeights[j][k].length; x++)
                    _mWeights[j][k][x] /= classTotal;
            }
        }

        for (int j = 0; j < _mWeights.length; j++)
            for (int k = 0; k < _cmp.nComparators(); k++)
                for (int x = 0; x < _cmp.nLevels(k); x++)
                    _logMWeights[j][k][x] = Math.log(_mWeights[j][k][x]);
    }

    private final int _nClasses, _nMatchClasses;
    private final RecordComparator _cmp;
    private final double[][][] _mWeights, _logMWeights;
    private final MixtureModel _model;
}
