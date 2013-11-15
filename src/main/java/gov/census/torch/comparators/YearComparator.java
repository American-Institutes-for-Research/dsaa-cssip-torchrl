package gov.census.torch.comparators;

import gov.census.torch.Field;

public class YearComparator extends ProratedComparator {
    public YearComparator(int baseYear, double[] slope, double[] intercept) {
        super(slope, intercept);
        this.baseYear = baseYear;
    }

    @Override
    public int compare(Field field1, Field field2) {
        double value1 = baseYear - field1.doubleValue();
        double value2 = baseYear - field2.doubleValue();
        return super.compare(value1, value2);
    }

    private int baseYear;
}