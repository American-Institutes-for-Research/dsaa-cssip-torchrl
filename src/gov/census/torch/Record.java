package gov.census.torch;

/**
 * A Record consists of several Fields, a blocking key, and, optionally, an id.
 */
public class Record {

    public Record(String blockingKey, String id, Field[] fields) {
        this._blockingKey = blockingKey;
        this._id = id;
        this._fields = fields;
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

    private String _blockingKey;
    private String _id;
    private Field[] _fields;
}