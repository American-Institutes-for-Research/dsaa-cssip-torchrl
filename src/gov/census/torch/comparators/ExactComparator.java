package gov.census.torch.comparators;

import gov.census.torch.Field;
import gov.census.torch.IFieldComparator;

/**
 * A comparator that tests for string equality.
 */
public class ExactComparator implements IFieldComparator {

    public final static Integer MATCH = 1;
    public final static Integer NONMATCH = 0;

    public final static ExactComparator INSTANCE = new ExactComparator();

    /**
     * Compare the two fields as strings for equality.
     *
     * @return 1 if the string values are equal, 0 otherwise.
     */
    @Override
    public int compare(Field field1, Field field2) {
        if (field1.stringValue().equals(field2.stringValue()))
            return MATCH;
        else
            return NONMATCH;
    }

    @Override
    public int nLevels() {
        return 2;
    }
}