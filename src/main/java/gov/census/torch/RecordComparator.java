package gov.census.torch;

import gov.census.torch.comparators.ExactComparator;

import java.util.LinkedList;

/**
 * A Fellegi-Sunter record comparator.
 */
public class RecordComparator {

    public static class Builder {

        public Builder(RecordSchema schema1, RecordSchema schema2) 
        {
            _schema1 = schema1;
            _schema2 = schema2;
            _handleBlanks = true;

            _compareFields = new LinkedList<>();
            _comparators = new LinkedList<>();
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

        public Builder compare(String name, IFieldComparator cmp) {
            _compareFields.add(name);
            _comparators.add(cmp);
            return this;
        }

        public Builder handleBlanks(boolean b) {
            _handleBlanks = b;
            return this;
        }

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
        return _levels[i];
    }

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

    public int patternIndex(int[] pattern) {
        int index = 0;

        for (int i = 0; i < _nComparators; i++)
            index += pattern[i] * _steps[i];

        return index;
    }

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
    }

    private final RecordSchema _schema1, _schema2;
    private final int _nComparators, _nPatterns, _levelOffset;
    private final int[] _fieldIndex1, _fieldIndex2, _levels, _steps;
    private final IFieldComparator[] _comparators;
    private final boolean _handleBlanks;
}
