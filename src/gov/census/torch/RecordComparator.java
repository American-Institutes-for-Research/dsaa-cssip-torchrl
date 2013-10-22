package gov.census.torch;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecordComparator {

    public RecordComparator() {
        insertIndex = 0;
        fieldIndices = new int[INITIAL_CAPACITY];
        comparators = new IFieldComparator[INITIAL_CAPACITY];
    }

    public void addComparator(int fieldIndex, IFieldComparator cmp) {
        if (insertIndex == fieldIndices.length) {
            int newLength = 2*fieldIndices.length;
            fieldIndices = Arrays.copyOf(fieldIndices, newLength);
            comparators = Arrays.copyOf(comparators, newLength);
        }

        fieldIndices[insertIndex] = fieldIndex;
        comparators[insertIndex] = cmp;
        insertIndex++;
    }

    public int[] compare(Record rec1, Record rec2) {
        int[] pattern = new int[fieldIndices.length];

        for (int i = 0; i < pattern.length; i++) {
            int index = fieldIndices[i];
            pattern[i] = comparators[i].compare(rec1.field(index), rec2.field(index));
        }

        return pattern;
    }

    protected final static int INITIAL_CAPACITY = 10;

    private int insertIndex;
    private int[] fieldIndices;
    private IFieldComparator[] comparators;
}
