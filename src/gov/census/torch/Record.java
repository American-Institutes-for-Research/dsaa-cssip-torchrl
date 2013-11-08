package gov.census.torch;

import java.util.Arrays;

/**
 * A Record consists of several Fields, a blocking key, and, optionally, an id.
 */
public class Record {

    public Record(String blockingKey, String id, Field[] fields) {
        this._blockingKey = blockingKey;
        this._id = id;
        this._fields = Arrays.copyOf(fields, fields.length);
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

    private final String _blockingKey;
    private final String _id;
    private final Field[] _fields;
}
