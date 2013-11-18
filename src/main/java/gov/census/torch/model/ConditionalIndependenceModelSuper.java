package gov.census.torch.model;

import gov.census.torch.IModel;
import gov.census.torch.Record;
import gov.census.torch.RecordComparator;
import gov.census.torch.counter.Counter;

import java.util.Random;

public class ConditionalIndependenceModelSuper
    implements IModel
{
    public ConditionalIndependenceModelSuper(Random rng, Counter[] counters, int nMatchClasses)
    {
        if (counters.length < 2)
            throw new IllegalArgumentException("need at least two classes");

        if (nMatchClasses >= counters.length - 1)
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
    }

    public ConditionalIndependenceModelSuper(Counter[] counters, int nMatchClasses) {
        this(new Random(), counters, nMatchClasses);
    }

    public int nClasses() {
        return _nClasses;
    }

    public double[][][] multinomialWeights() {
        return _mWeights;
    }

    @Override
    public double matchScore(Record rec1, Record rec2) {
        double score = 0.0;
        int[] pattern = _cmp.compare(rec1, rec2);

        for (int j = 0; j < _nMatchClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++)
                score += _logMWeights[j][k][pattern[k]];
        }

        for (int j = _nMatchClasses; j < _nClasses; j++) {
            for (int k = 0; k < _cmp.nComparators(); k++)
                score -= _logMWeights[j][k][pattern[k]];
        }

        return score;
    }

    @Override
    public RecordComparator recordComparator() {
        return _cmp;
    }

    @Override
    public String toString() {
        int precision = 4;
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Model: conditional independence with %d classes%n", _nClasses));

        for (int j = 0; j < _nClasses; j++) {
            builder.append(String.format("Class %2d:%n", j));
            builder.append(String.format("%-7s%s%n", "Field", "Weights"));

            for (int k = 0; k < _cmp.nComparators(); k++) {
                builder.append(String.format("%-7d", k));
                for (int x = 0; x < _cmp.nLevels(k); x++)
                    builder.append(String.format("%6.4f ", _mWeights[j][k][x]));

                builder.append("\n");
            }
        }

        return builder.toString();
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
}
