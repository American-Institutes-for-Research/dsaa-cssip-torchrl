package gov.census.torch;

import gov.census.torch.comparators.ExactComparator;

import java.util.Arrays;

/**
 * A Fellegi-Sunter record comparator.
 */
public class RecordComparator {

    public static class Builder {

        public Builder(RecordSchema schema1, RecordSchema schema2) 
        {
            this.schema1 = schema1;
            this.schema2 = schema2;
            this.handleBlanks = true;

            insertIndex = 0;
            fieldIndex1 = new int[INITIAL_CAPACITY];
            fieldIndex2 = new int[INITIAL_CAPACITY];
            comparators = new IFieldComparator[INITIAL_CAPACITY];
        }

        public Builder(RecordSchema schema) {
            this(schema, schema);
        }

        public Builder(IRecordLoader load1, IRecordLoader load2) {
            this(load1.schema(), load2.schema());
        }

        public Builder(IRecordLoader load) {
            this(load.schema());
        }

        public Builder compare(int field1, int field2, IFieldComparator cmp) {
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

        public Builder compare(String name, IFieldComparator cmp) {
            return this.compare(schema1.fieldIndex(name),
                                schema2.fieldIndex(name), cmp);
        }

        public Builder handleBlanks(boolean b) {
            handleBlanks = b;
            return this;
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
        
        private final RecordSchema schema1, schema2;
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
        this._handleBlanks = handleBlanks;
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
                this.steps[i] = this.steps[i - 1] * this.levels[i - 1];
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
                pattern[i] = 0;
                continue;
            }

            IFieldComparator cmp = comparators[i];

            // short circuit evaluation if exact match
            if (field1.stringValue().equals(field2.stringValue())) {
                pattern[i] = cmp.nLevels() - 1 + levelOffset;
                continue;
            }

            // if you've made it this far and you're using ExactComparator, then you disagree
            if (cmp.getClass() == ExactComparator.class) {
                pattern[i] = levelOffset;
                continue;
            }

            pattern[i] = 
                comparators[i].compare(rec1.field(index1), rec2.field(index2)) + levelOffset;
        }

        return pattern;
    }

    public int compareIndex(Record rec1, Record rec2) {
        int[] pattern = compare(rec1, rec2);

        return patternIndex(pattern);
    }

    public boolean handleBlanks() {
        return _handleBlanks;
    }

    public int nComparators() {
        return _nComparators;
    }

    public int nPatterns() {
        return _nPatterns;
    }

    public int nLevels(int i) {
        return levels[i];
    }

    public int[] patternFor(int index) {
        if (index < 0 || index >= _nPatterns)
            throw new ArrayIndexOutOfBoundsException();

        int[] pattern = new int[_nComparators];

        for (int i = 0; i < _nComparators; i++) {
            int j = _nComparators - 1 - i;
            pattern[j] = index / steps[j];
            index %= steps[j];
        }

        return pattern;
    }

    public int patternIndex(int[] pattern) {
        int index = 0;

        for (int i = 0; i < _nComparators; i++)
            index += pattern[i] * steps[i];

        return index;
    }

    /**
     * Return an array giving the fields in this record that would be compared.
     * Use the indices corresponding to schema1 in the Builder constructor.
     */
    public String[] comparisonFields1(Record rec) {
        return extractFields(fieldIndex1, rec);
    }

    /**
     * Return an array giving the fields in this record that would be compared.
     * Use the indices corresponding to schema2 in the Builder constructor.
     */
    public String[] comparisonFields2(Record rec) {
        return extractFields(fieldIndex2, rec);
    }

    private static String[] extractFields(int[] index, Record rec) {
        String[] fields = new String[index.length];
        for (int i = 0; i < index.length; i++)
            fields[i] = rec.field(index[i]).stringValue();

        return fields;
    }

    private final int _nComparators;
    private final int[] fieldIndex1;
    private final int[] fieldIndex2;
    private final IFieldComparator[] comparators;
    private final int _nPatterns;
    private final boolean _handleBlanks;
    private final int[] levels, steps;
    private final int levelOffset;
}
