package gov.census.torch;

import gov.census.torch.comparators.ExactComparator;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecordComparator {

    public final static int BLANK = -1;
    public final static int DISAGREE = 0;

    public static boolean isBlank(Field field) {
        return field.stringValue().trim().isEmpty();
    }

    public RecordComparator(boolean handleBlanks) {
        insertIndex = 0;
        fieldIndices = new int[INITIAL_CAPACITY];
        comparators = new IFieldComparator[INITIAL_CAPACITY];
        _nPatterns = 0;
        this.handleBlanks = handleBlanks;
    }

    public RecordComparator() {
        this(true);
    }

    public void addComparator(int fieldIndex, IFieldComparator cmp) {
        if (insertIndex == fieldIndices.length) {
            int newLength = 2*fieldIndices.length;
            fieldIndices = Arrays.copyOf(fieldIndices, newLength);
            comparators = Arrays.copyOf(comparators, newLength);
        }

        fieldIndices[insertIndex] = fieldIndex;
        comparators[insertIndex] = cmp;

        if (insertIndex == 0)
            _nPatterns = cmp.nLevels();
        else
            _nPatterns *= cmp.nLevels();

        insertIndex++;
    }

    public int[] compare(Record rec1, Record rec2) {
        int[] pattern = new int[insertIndex];

        for (int i = 0; i < pattern.length; i++) {
            int index = fieldIndices[i];
            Field field1 = rec1.field(index);
            Field field2 = rec2.field(index);

            // short circuit evaluation if either of the fields is blank
            if (isBlank(field1) || isBlank(field2)) {
                if (this.handleBlanks)
                    pattern[i] = BLANK;
                else
                    pattern[i] = DISAGREE;

                continue;
            }

            IFieldComparator cmp = comparators[i];

            // short circuit evaluation if exact match
            if (field1.stringValue().equals(field2.stringValue())) {
                pattern[i] = cmp.nLevels() - 1;
                continue;
            }

            // if you've made it this far and you're using ExactComparator, then you disagree
            if (cmp.getClass() == ExactComparator.class) {
                pattern[i] = DISAGREE;
                continue;
            }

            pattern[i] = comparators[i].compare(rec1.field(index), rec2.field(index));
        }

        return pattern;
    }

    public long nPatterns() {
        return _nPatterns;
    }

    public int[] patternFor(int i) {
        if (i < 0 || i >= _nPatterns)
            throw new ArrayIndexOutOfBoundsException();

        int[] pattern = new int[insertIndex];

        return pattern;
    }

    protected final static int INITIAL_CAPACITY = 10;

    private int insertIndex;
    private int[] fieldIndices;
    private IFieldComparator[] comparators;
    private long _nPatterns;
    private boolean handleBlanks;
}
