package gov.census.torch;

import gov.census.torch.comparators.ExactComparator;

import java.util.Arrays;

/**
 * A Fellegi-Sunter record comparator.
 */
public class RecordComparator {

    public final static int BLANK = -1;
    public final static int DISAGREE = 0;

    public static class Builder {

        public Builder(IFileSchema schema1, IFileSchema schema2, 
                       boolean handleBlanks) 
        {
            insertIndex = 0;
            fieldIndex1 = new int[INITIAL_CAPACITY];
            fieldIndex2 = new int[INITIAL_CAPACITY];
            comparators = new IFieldComparator[INITIAL_CAPACITY];
            this.handleBlanks = handleBlanks;
            this.schema1 = schema1;
            this.schema2 = schema2;
        }

        public Builder(IFileSchema schema1, IFileSchema schema2) {
            this(schema1, schema2, true);
        }

        public Builder comparator(int field1, int field2, IFieldComparator cmp) {
            if (insertIndex == fieldIndex1.length) {
                int newLength = 2*fieldIndex1.length;
                fieldIndex1 = Arrays.copyOf(fieldIndex1, newLength);
                fieldIndex2 = Arrays.copyOf(fieldIndex2, newLength);
                comparators = Arrays.copyOf(comparators, newLength);
            }

            fieldIndex1[insertIndex] = field1;
            fieldIndex2[insertIndex] = field2;
            comparators[insertIndex] = cmp;

            insertIndex++;
            return this;
        }

        public Builder comparator(String name, IFieldComparator cmp) {
            return this.comparator(schema1.getFieldIndex(name),
                                   schema2.getFieldIndex(name), cmp);
        }

        public RecordComparator build() {
            return new RecordComparator(insertIndex, fieldIndex1, fieldIndex2,
                                        comparators, handleBlanks);
        }

        protected final static int INITIAL_CAPACITY = 10;

        private int insertIndex;
        private int[] fieldIndex1;
        private int[] fieldIndex2;
        private IFieldComparator[] comparators;
        private boolean handleBlanks;
        
        private IFileSchema schema1;
        private IFileSchema schema2;
    }

    private RecordComparator(int nComparators,
                             int[] fieldIndex1,
                             int[] fieldIndex2,
                             IFieldComparator[] comparators,
                             boolean handleBlanks) 
    {
        _nComparators = nComparators;
        this.fieldIndex1 = Arrays.copyOf(fieldIndex1, nComparators);
        this.fieldIndex2 = Arrays.copyOf(fieldIndex2, nComparators);
        this.comparators = Arrays.copyOf(comparators, nComparators);
        this.handleBlanks = handleBlanks;
        this.levelOffset = handleBlanks ? 1 : 0;

        int nPatterns = 1;
        this.levels = new int[nComparators];
        this.steps = new int[nComparators];
        this.steps[0] = 1;

        for (int i = 0; i < nComparators; i++) {
            int nLevels = this.comparators[i].nLevels() + levelOffset;
            this.levels[i] = nLevels;
            nPatterns *= nLevels;

            if (i > 0)
                this.steps[i] = this.steps[i - 1] * nLevels;
        }

        this._nPatterns = nPatterns;
    }

    public static boolean isBlank(Field field) {
        return field.stringValue().trim().isEmpty();
    }

    public int[] compare(Record rec1, Record rec2) {
        int[] pattern = new int[_nComparators];

        for (int i = 0; i < pattern.length; i++) {
            int index1 = fieldIndex1[i];
            int index2 = fieldIndex2[i];
            Field field1 = rec1.field(index1);
            Field field2 = rec2.field(index2);

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

            pattern[i] = comparators[i].compare(rec1.field(index1), rec2.field(index2));
        }

        return pattern;
    }

    public int compareIndex(Record rec1, Record rec2) {
        int[] pattern = compare(rec1, rec2);

        return patternIndex(pattern);
    }

    public int nComparators() {
        return _nComparators;
    }

    public int nPatterns() {
        return _nPatterns;
    }

    public int[] patternFor(int index) {
        if (index < 0 || index >= _nPatterns)
            throw new ArrayIndexOutOfBoundsException();

        int[] pattern = new int[_nComparators];

        for (int i = 0; i < _nComparators; i++) {
            int j = _nComparators - 1 - i;
            pattern[j] = index / steps[j] - levelOffset;
            index %= steps[j];
        }

        return pattern;
    }

    public int patternIndex(int[] pattern) {
        int index = 0;

        for (int i = 0; i < _nComparators; i++)
            index += (pattern[i] + levelOffset) * steps[i];

        return index;
    }

    private final int _nComparators;
    private final int[] fieldIndex1;
    private final int[] fieldIndex2;
    private final IFieldComparator[] comparators;
    private final int _nPatterns;
    private final boolean handleBlanks;
    private final int[] levels, steps;
    private final int levelOffset;
}
