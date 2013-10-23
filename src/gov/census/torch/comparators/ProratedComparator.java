package gov.census.torch.comparators;

import gov.census.torch.Field;
import gov.census.torch.IFieldComparator;

/**
 * Compare the scaled difference of two fields to threshold values.
 */
public class ProratedComparator implements IFieldComparator {
    public ProratedComparator(double[] slope, double[] intercept) {
        if (slope.length != intercept.length)
            throw new IllegalArgumentException("'slope' and 'intercept' should have same length");

        this.slope = slope;
        this.intercept = intercept;
    }

    /**
     * Compare the scaled difference of the fields to threshold values. The
     * absolute difference and the minimum values of the two fields are
     * computed, and then
     *      diff &lt; slope[i]  min + intercept[i]
     * for each of the slopes and intercepts that were passed to the constructer.
     *
     * @return An Integer between 0 (inclusive) and this.nLevels() (exclusive).
     */
    @Override
    public int compare(Field field1, Field field2) {
        double value1 = field1.doubleValue();
        double value2 = field2.doubleValue();
        return compare(value1, value2);
    }

    public int compare(double value1, double value2) {
        double min = (value1 <= value2) ? value1 : value2;
        double diff = Math.abs(value1 - value2);

        int matchLevel = 0;
        for (int i = 0; i < slope.length; i++) {
            if (diff < slope[i] * min + intercept[i]) {
                matchLevel = slope.length - i;
                break;
            }
        }

        return matchLevel;
    }

    @Override
    public int nLevels() {
        return slope.length + 1;
    }

    private double[] slope, intercept;
}