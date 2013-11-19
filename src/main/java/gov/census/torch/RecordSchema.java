package gov.census.torch;

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
     * @param idFields an array of id field names, should be a subset of <code>columns</code>.
     *
     * @throws IllegalArgumentException if <code>blockingFields</code> or <code>idFields</code>
     * contains a name that doesn't appear in <code>columns</code>.
     */
    public RecordSchema(String[] columns, String[] blockingFields, String[] idFields) 
    {
        _columns = columns;
        _blockingFields = blockingFields;
        _idFields = idFields;
        _hasId = (idFields.length > 0);

        _columnIndex = new HashMap<>();
        int i = 0;
        for (String name: columns)
            _columnIndex.put(name, i++);
        
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(columns));
        for (String name: blockingFields) {
            if (_columnIndex.get(name) == null)
                throw new IllegalArgumentException("no such column: " + name);

            fields.remove(name);
        }

        for (String name: idFields) {
            if (_columnIndex.get(name) == null)
                throw new IllegalArgumentException("no such column: " + name);

            fields.remove(name);
        }

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
        StringBuilder id = new StringBuilder();
        Field[] fields = new Field[_fields.length];

        for (String name: _blockingFields)
            blockingKey.append(map.get(name));

        for (String name: _idFields)
            id.append(map.get(name));

        for (int i = 0; i < _fields.length; i++)
            fields[i] = new Field(map.get(_fields[i]));

        return new Record(this, blockingKey.toString(), id.toString(), fields);
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
    private final String[] _columns, _fields, _blockingFields, _idFields;
    private final boolean _hasId;
}
