package gov.census.torch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A Record consists of several Fields, a blocking key, and, optionally, an id.
 */
public class Record {

    public static HashMap<String, List<Record>> block(List<Record> list) {
        HashMap<String, List<Record>> blocks = new HashMap<>();
        for (Record rec: list) {
            String key = rec.blockingKey();

            if (blocks.containsKey(key)) {
                blocks.get(key).add(rec);
            } else {
                LinkedList<Record> ll = new LinkedList<>();
                ll.add(rec);
                blocks.put(key, ll);
            }
        }

        return blocks;
    }

    public Field field(int i) {
        return _fields[i];
    }

    public String blockingKey() {
        return _blockingKey;
    }

    public String id() {
        return _id;
    }

    public int nFields() {
        return _fields.length;
    }

    public RecordSchema schema() {
        return _schema;
    }

    protected Record(String blockingKey, String id, Field[] fields) {
        this(null, blockingKey, id, fields);
    }
    
    protected Record(RecordSchema schema, String blockingKey, String id, Field[] fields) 
    {
        _schema = schema;
        _blockingKey = blockingKey;
        _id = id;
        _fields = Arrays.copyOf(fields, fields.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Record))
            return false;

        Record rec = (Record)obj;
        
        if (_schema != rec.schema())
            return false;

        if (!_blockingKey.equals(rec.blockingKey()))
            return false;

        if (!_id.equals(rec.id()))
            return false;

        for (int i = 0; i < _fields.length; i++)
            if (!_fields[i].equals(rec.field(i)))
                return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Key: " + _blockingKey + ", ");

        if (_schema.hasId())
            b.append("ID: " + _id + ", ");

        b.append("Fields: " + Arrays.toString(_fields));
        return b.toString();
    }

    private final RecordSchema _schema;
    private final String _blockingKey;
    private final String _id;
    private final Field[] _fields;
}
