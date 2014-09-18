package torch;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class describes how to construct a record from an array of column values. Throughout
 * this library "column" refers to a column of data being imported into the program (from a
 * file, database, etc) and "field" refers to a field that can be used in a record comparison.
 * A <code>RecordSchema</code> can be used to construct new {@link Record} instances.
 */
public class RecordSchema
{
    /**
     * Constructs a new <code>RecordSchema</code> with the given columns, blocking fields, and ID
     * fields. Entries in <code>columns</code> that are not blocking fields or ID fields will become
     * <code>Record</code> fields.
     *
     * @param columns an array of column names corresponding to incoming data.
     * @param blockingFields an array of blocking field names, should be a subset of
     * <code>columns</code>.
     * @param seqField name of the sequence field. Should appear in <code>columns</code>.
     * @param idField name of the ID field. Should appear in <code>columns</code>.
     *
     * @throws IllegalArgumentException if <code>blockingFields</code> or <code>idFields</code>
     * contains a name that doesn't appear in <code>columns</code>.
     */
    public RecordSchema(String[] columns, String[] blockingFields, String seqField, String idField) 
    {
        _columns = columns;
        _blockingFields = blockingFields;
        _seqField = seqField;
        _idField = idField;
        _hasId = (idField != null);

        _columnIndex = new HashMap<>();
        int i = 0;
        for (String name: columns)
            _columnIndex.put(name, i++);
        
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(columns));
        for (String name: blockingFields) {
            if (_columnIndex.get(name) == null)
                throw new IllegalArgumentException("no such column (blocking): " + name);

            fields.remove(name);
        }

        if (_seqField == null) {
            _seqValue = 0;
        } else {
            if (_columnIndex.get(_seqField) == null)
                throw new IllegalArgumentException("no such column (seq): " + _seqField);

            fields.remove(_seqField);
        }

        if (_hasId && _columnIndex.get(_idField) == null)
            throw new IllegalArgumentException("no such column (id): " + _idField);
        
        fields.remove(_idField);

        _fields = fields.toArray(new String[0]);
        
        _fieldIndex = new HashMap<>();
        i = 0;
        for (String name: fields)
            _fieldIndex.put(name, i++);
    }

    /**
     * Constructs a new <code>Record</code> from the given column values. The blocking key and ID
     * are constructed by appending blocking key fields and ID fields.
     */
    public Record newRecord(String[] columns) {
        if (columns.length != _columns.length)
            throw new IllegalArgumentException(
                    "too few columns to construct a record: " +
                    Arrays.toString(columns));

        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < _columns.length; i++)
            map.put(_columns[i], columns[i]);

        StringBuilder blockingKey = new StringBuilder();
        Field[] fields = new Field[_fields.length];

        for (String name: _blockingFields)
            blockingKey.append(map.get(name));

        String id = null;

        if (_hasId)
            id = map.get(_idField);

        String seq;

        if (_seqField == null)
            seq = "" + _seqValue++;
        else
            seq = map.get(_seqField);

        for (int i = 0; i < _fields.length; i++)
            fields[i] = new Field(map.get(_fields[i]));

        return new Record(this, blockingKey.toString(), seq, id, fields);
    }

    /**
     * Returns the field index of the field with the given name, i.e., the index of the field in a
     * <code>Record</code> index.
     */
    public int fieldIndex(String name) {
        if (!_fieldIndex.containsKey(name))
            throw new IllegalArgumentException("No such field: " + name);

        return _fieldIndex.get(name);
    }

    /**
     * Returns the column index of the field with the given name, i.e., the index in an array of
     * column values from the data source.
     */
    public int columnIndex(String name) {
        if (!_columnIndex.containsKey(name))
            throw new IllegalArgumentException("No such column: " + name);

        return _columnIndex.get(name);
    }

    /**
     * Returns whether this <code>RecordSchema</code> has any ID fields.
     */
    public boolean hasId() {
        return _hasId;
    }

    private final HashMap<String, Integer> _columnIndex, _fieldIndex;
    private final String[] _columns, _fields, _blockingFields;
    private final String _idField, _seqField;
    private final boolean _hasId;
    private int _seqValue;
}
