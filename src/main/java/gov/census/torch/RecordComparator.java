package gov.census.torch;

import gov.census.torch.comparators.ExactComparator;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * An object which computes the matching pattern betwen two {@link Record}s.
 */
public class RecordComparator {

    /**
     * A builder object to simplify constructing a new
     * <code>RecordComparator</code> object.
     */
    public static class Builder {

        /**
         * Constructs a new Builder object. The resulting <code>RecordComparator</code> object will
         * compare records of type <code>schema1</code> and <code>schema2</code>.
         */
        public Builder(RecordSchema schema1, RecordSchema schema2) 
        {
            _schema1 = schema1;
            _schema2 = schema2;
            _handleBlanks = true;

            _compareFields = new LinkedList<>();
            _comparators = new LinkedList<>();
        }

        /**
         * Constructs a new Builder object. The resulting <code>RecordComparator</code> object will
         * compare records with the same <code>schema</code>.
         */
        public Builder(RecordSchema schema) {
            this(schema, schema);
        }

        /**
         * Constructs a new Builder object. Use the record schemas returned by <code>load1</code> and
         * <code>load2</code>.
         */
        public Builder(IRecordLoader load1, IRecordLoader load2) {
            this(load1.schema(), load2.schema());
        }


        /**
         * Constructs a new Builder object. Use the record schema returned by <code>load</code>.
         */
        public Builder(IRecordLoader load) {
            this(load.schema());
        }

        /**
         * Adds a comparator to the field with the given <code>name</code>.
         */
        public Builder compare(String name, IFieldComparator cmp) {
            _compareFields.add(name);
            _comparators.add(cmp);
            return this;
        }

        /**
         * Indicates whether the <code>RecordComparator</code> should handle blank values.
         */
        public Builder handleBlanks(boolean b) {
            _handleBlanks = b;
            return this;
        }

        /**
         * Builds the <code>RecordComparator</code> object.
         */
        public RecordComparator build() {
            String[] compareFields = _compareFields.toArray(new String[0]);
            IFieldComparator[] comparators = 
                _comparators.toArray(new IFieldComparator[0]);

            return new RecordComparator(_schema1, _schema2, compareFields,
                                        comparators, _handleBlanks);
        }

        private boolean _handleBlanks;
        
        private final LinkedList<String> _compareFields;
        private final LinkedList<IFieldComparator> _comparators;
        private final RecordSchema _schema1, _schema2;
    }

    /**
     * Computes the comparison pattern between two <code>Record</code>s. The length of the
     * comparison pattern is <code>nComparators()</code>.
     *
     * @see #nComparators
     */
    public int[] compare(Record rec1, Record rec2) {
        int[] pattern = new int[_nComparators];

        for (int i = 0; i < pattern.length; i++) {
            int index1 = _fieldIndex1[i];
            int index2 = _fieldIndex2[i];
            Field field1 = rec1.field(index1);
            Field field2 = rec2.field(index2);

            // short circuit evaluation if either of the fields is blank
            if (field1.empty() || field2.empty()) {
                pattern[i] = 0;
                continue;
            }

            IFieldComparator cmp = _comparators[i];

            // short circuit evaluation if exact match
            if (field1.stringValue().equals(field2.stringValue())) {
                pattern[i] = cmp.nLevels() - 1 + _levelOffset;
                continue;
            }

            // if you've made it this far and you're using ExactComparator, then you disagree
            if (cmp.getClass() == ExactComparator.class) {
                pattern[i] = _levelOffset;
                continue;
            }

            pattern[i] = 
                _comparators[i].compare(rec1.field(index1), rec2.field(index2)) + _levelOffset;
        }

        return pattern;
    }

    /**
     * Same as {@link #compare} but returns the comparison pattern as an <code>int</code>. The
     * <code>int</code> value is a number between 0 and <code>nPatterns() - 1</code>, and it can be
     * converted to an array by calling <code>patternFor(int)</code>.
     *
     * @see #nPatterns
     * @see #patternFor
     */
    public int compareIndex(Record rec1, Record rec2) {
        int[] pattern = compare(rec1, rec2);

        return patternIndex(pattern);
    }

    /**
     * Indicates whether this <code>RecordComparator</code> handles blanks specially.
     */
    public boolean handleBlanks() {
        return _handleBlanks;
    }

    /**
     * The number of field comparators used by this <code>RecordComparator</code>.
     */
    public int nComparators() {
        return _nComparators;
    }

    /**
     * The number of different comparison patterns that can be returned.
     */
    public int nPatterns() {
        return _nPatterns;
    }

    /**
     * The number of different levels (values) that can be returned by the <code>i</code>th field
     * comparator.
     */
    public int nLevels(int i) {
        return _levels[i];
    }

    /**
     * The umber of different levels (values) that can be returned by the comparator associated to
     * the field with the given name.
     *
     * @throws IllegalArgumentException if no comparatator is associated to the given field name.
     */
    public int nLevels(String name) {
        if (!_compareFieldIndex.containsKey(name))
            throw new IllegalArgumentException(String.format("No such field: %s", name));

        return _levels[_compareFieldIndex.get(name)];
    }

    /**
     * The names of fields that are examined by this record comparator.
     */
    public String[] compareFields() {
        return _compareFields;
    }

    /**
     * The array-valued comparison corresponding to the given <code>index</code>.
     *
     * @throws ArrayIndexOutOfBoundsException if the <code>index</code> doesn't corresond to a
     * pattern.
     */
    public int[] patternFor(int index) {
        if (index < 0 || index >= _nPatterns)
            throw new ArrayIndexOutOfBoundsException();

        int[] pattern = new int[_nComparators];

        for (int i = 0; i < _nComparators; i++) {
            int j = _nComparators - 1 - i;
            pattern[j] = index / _steps[j];
            index %= _steps[j];
        }

        return pattern;
    }

    /**
     * Returns the <code>int</code> value corresponding to the given <code>pattern</code>. Note that
     * if <code>pattern</code> isn't a valid value that could be returned by <code>compare(Record,
     * Record)</code> then the returned value is nonsense.
     */
    public int patternIndex(int[] pattern) {
        int index = 0;

        for (int i = 0; i < _nComparators; i++)
            index += pattern[i] * _steps[i];

        return index;
    }

    /**
     * Returns the fields in the given <code>Record</code> that would be used in a comparison.
     *
     * @throws IllegalArgumentException if the given <code>Record</code>'s schema isn't known by
     * this <code>RecordComparator</code>.
     */
    public String[] comparisonFields(Record rec) {
        if (_schema1 == rec.schema())
            return extractFields(_fieldIndex1, rec);
        else if (_schema2 == rec.schema())
            return extractFields(_fieldIndex2, rec);
        else
            throw new IllegalArgumentException("Unknown record schema");
    }

    private static String[] extractFields(int[] index, Record rec) {
        String[] fields = new String[index.length];
        for (int i = 0; i < index.length; i++)
            fields[i] = rec.field(index[i]).stringValue();

        return fields;
    }

    private RecordComparator(RecordSchema schema1, RecordSchema schema2,
                             String[] compareFields,
                             IFieldComparator[] comparators,
                             boolean handleBlanks) 
    {
        _schema1 = schema1;
        _schema2 = schema2;
        _compareFields = compareFields;
        _nComparators = comparators.length;
        _comparators = comparators;
        _handleBlanks = handleBlanks;
        _levelOffset = handleBlanks ? 1 : 0;

        _fieldIndex1 = new int[_nComparators];
        _fieldIndex2 = new int[_nComparators];
        _levels = new int[_nComparators];
        _steps = new int[_nComparators];

        _steps[0] = 1;
        int nPatterns = 1;

        for (int i = 0; i < _nComparators; i++) {
            int nLevels = _comparators[i].nLevels() + _levelOffset;
            _levels[i] = nLevels;
            nPatterns *= nLevels;

            if (i > 0)
                _steps[i] = _steps[i - 1] * _levels[i - 1];

            _fieldIndex1[i] = schema1.fieldIndex(compareFields[i]);
            _fieldIndex2[i] = schema2.fieldIndex(compareFields[i]);
        }

        _nPatterns = nPatterns;

        _compareFieldIndex = new HashMap<>();
        for (int i = 0; i < _compareFields.length; i++)
            _compareFieldIndex.put(_compareFields[i], i);
    }

    private final RecordSchema _schema1, _schema2;
    private final String[] _compareFields;
    private final HashMap<String, Integer> _compareFieldIndex;
    private final int _nComparators, _nPatterns, _levelOffset;
    private final int[] _fieldIndex1, _fieldIndex2, _levels, _steps;
    private final IFieldComparator[] _comparators;
    private final boolean _handleBlanks;
}
