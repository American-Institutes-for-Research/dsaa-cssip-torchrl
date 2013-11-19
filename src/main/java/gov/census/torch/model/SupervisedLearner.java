package gov.census.torch.model;

import gov.census.torch.RecordComparator;
import gov.census.torch.counter.Tally;

import java.util.Random;

public class SupervisedLearner
{
    public SupervisedLearner(Random rng, Tally[] tallies, int nMatchClasses)
    {
        if (tallies.length < 2)
            throw new IllegalArgumentException("need at least two classes");

        if (nMatchClasses > tallies.length - 1)
            throw new IllegalArgumentException("need at least one nonmatch class");

        for (int j = 1; j < tallies.length; j++) {
            if (tallies[0].recordComparator() != tallies[j].recordComparator())
                throw new IllegalArgumentException(
                        "all tallies should have the same record comparator");

        }

        _nClasses = tallies.length;
        _nMatchClasses = nMatchClasses;
        _cmp = tallies[0].recordComparator();
        _mWeights = new double[_nClasses][_cmp.nComparators()][];
        _logMWeights = new double[_nClasses][_cmp.nComparators()][];
        for (int j = 0; j < _nClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++) {
                _mWeights[j][k] = new double[_cmp.nLevels(k)];
                _logMWeights[j][k] = new double[_cmp.nLevels(k)];
            }
        }

        estimate(rng, tallies);
        _model = new MixtureModel(_cmp, _mWeights, _nMatchClasses);
    }

    public SupervisedLearner(Tally[] tallies, int nMatchClasses) {
        this(new Random(), tallies, nMatchClasses);
    }

    public MixtureModel model() {
        return _model;
    }

    /**
     * Compute the ML estimate for the labeled data.
     */
    private void estimate(Random rng, Tally[] tallies) {
        for (int j = 0; j < _nClasses; j++) {
            double classTotal = 0.0;
            int[] counts = tallies[j].nonzeroCounts();
            int[][] patterns = tallies[j].nonzeroPatterns();

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
