package torch.comparators;

import torch.Field;
import torch.IFieldComparator;

/**
 * Compare the absolute difference of two fields to threshold values.
 */
public class AbsoluteDifferenceComparator implements IFieldComparator {

    /**
     * Constructs a new <code>AbsoluteDifferenceComparator</code> with the given
     * thresholds. Thresholds are tested in the order they are given here, so the
     * values in <code>thresholds</code> should be increasing.
     *
     * @param thresholds a strictly increasing array of threshold values
     */
    public AbsoluteDifferenceComparator(double[] thresholds) {
        _thresholds = thresholds;
    }

    /**
     * Compares the difference of the fields to threshold values.
     *
     * @return An integer between 0 (inclusive) and <code>this.nLevels()</code>
     * (exclusive).
     */
    public int compare(Field field1, Field field2) {
        double value1 = field1.doubleValue();
        double value2 = field2.doubleValue();
        return compare(value1, value2);
    }

    public int compare(double value1, double value2) {
        double diff = Math.abs(value1 - value2);
        int matchLevel = 0;
        for (int i = 0; i < _thresholds.length; i++) {
            if (diff <= _thresholds[i]) {
                matchLevel = _thresholds.length - i;
                break;
            }
        }

        return matchLevel;
    }

    @Override
    public int nLevels() {
        return _thresholds.length + 1;
    }

    private final double[] _thresholds;
}
