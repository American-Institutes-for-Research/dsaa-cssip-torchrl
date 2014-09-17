package torch;

import torch.util.BucketMap;
import torch.util.ListBucket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A Record consists of several Fields, a blocking key, and, optionally, an id.
 */
public class Record {

    /**
     * Perform blocking on a list of <code>Record</code>.
     *
     * @return a <code>HashMap</code> that associates blocking keys to the list of
     * <code>Record</code> with that blocking key.
     */
    public static Map<String, List<Record>> block(Iterable<Record> list) {
        BucketMap<String, Record, List<Record>> acc =
            new BucketMap<>(new HashMap<String, List<Record>>(),
                            new ListBucket<Record>());
        for (Record rec: list)
            acc.add(rec.blockingKey(), rec);

        return acc.map();
    }

    /**
     * Returns the <code>i</code>th field in this <code>Record</code>.
     */
    public Field field(int i) {
        return _fields[i];
    }

    /**
     * Return the blocking key.
     */
    public String blockingKey() {
        return _blockingKey;
    }

    /**
     * Returns the unique identifier for this field. Returns an empty <code>String</code> if the
     * <code>Record</code> doesn't have a unique identifier.
     */
    public String id() {
        return _id;
    }

    /**
     * Returns the number of fields in this <code>Record</code>.
     */
    public int nFields() {
        return _fields.length;
    }

    /**
     * Returns the {@link RecordSchema} for this <code>Record</code>.
     */
    public RecordSchema schema() {
        return _schema;
    }

    /**
     * Construct a new <code>Record</code> from the given values.
     */
    protected Record(RecordSchema schema, String blockingKey, String id, Field[] fields) 
    {
        _schema = schema;
        _blockingKey = blockingKey;
        _id = id;
        _fields = Arrays.copyOf(fields, fields.length);
    }

    /**
     * Two <code>Record</code>s are equal if they have the same schema, id, blocking key, and
     * fields.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Record))
            return false;

        Record rec = (Record)obj;
        
        if (_schema != rec.schema())
            return false;

        if (!_id.equals(rec.id()))
            return false;

        if (!_blockingKey.equals(rec.blockingKey()))
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
