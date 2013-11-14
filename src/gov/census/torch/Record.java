package gov.census.torch;

import java.util.Arrays;

/**
 * A Record consists of several Fields, a blocking key, and, optionally, an id.
 */
public class Record {

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

    private final RecordSchema _schema;
    private final String _blockingKey;
    private final String _id;
    private final Field[] _fields;
}
