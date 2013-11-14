package gov.census.torch;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class describes how to construct a record from an array of column
 * values. In this project, I use "column" to refer to a column of data being
 * imported into the program (from a file, database, etc) and I use field to
 * refer to a field that can be used in a record comparison.
 */
public class RecordSchema {

    public RecordSchema(String[] columns, String[] blockingFields,
                        String[] idFields) 
    {
        _columns = columns;
        _blockingFields = blockingFields;
        _idFields = idFields;

        _columnIndex = new HashMap<>();
        int i = 0;
        for (String name: columns)
            _columnIndex.put(name, i++);
        
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(columns));
        for (String name: blockingFields)
            fields.remove(name);
        for (String name: idFields)
            fields.remove(name);
        _fields = fields.toArray(new String[0]);
        
        _fieldIndex = new HashMap<>();
        i = 0;
        for (String name: fields)
            _fieldIndex.put(name, i++);
    }

    public Record newRecord(String[] columns) {
        if (columns.length != _columns.length)
            throw new IllegalArgumentException("not enough columns");

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

    public int fieldIndex(String name) {
        if (!_fieldIndex.containsKey(name))
            throw new IllegalArgumentException("No such field: " + name);

        return _fieldIndex.get(name);
    }

    public int columnIndex(String name) {
        if (!_columnIndex.containsKey(name))
            throw new IllegalArgumentException("No such column: " + name);

        return _columnIndex.get(name);
    }

    private final HashMap<String, Integer> _columnIndex, _fieldIndex;
    private final String[] _columns, _fields, _blockingFields, _idFields;
}
