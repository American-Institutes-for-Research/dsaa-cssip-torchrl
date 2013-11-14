package gov.census.torch.io;

import gov.census.torch.Field;
import gov.census.torch.Record;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A class describing a fixed-width file. In all addFoo methods, it is assumed that the
 * user is entering <code>start</code> and <code>off</code> using 1 for the first
 * column in the file.
 *
 * TODO: start from column 0, why mess around?
 */
public class FixedWidthFileSchema 
    implements gov.census.torch.IFileSchema
{

    public static class Builder {

        public Builder() {
            blockingStart = new int[INITIAL_CAPACITY];
            blockingOff = new int[INITIAL_CAPACITY];
            nBlockingFields = 0;
            hasId = false;
            idStart = new int[INITIAL_CAPACITY];
            idOff = new int[INITIAL_CAPACITY];
            nIdFields = 0;
            fieldStart = new int[INITIAL_CAPACITY];
            fieldOff = new int[INITIAL_CAPACITY];
            fieldName = new String[INITIAL_CAPACITY];
            nFields = 0;
        }

        public FixedWidthFileSchema build() {
            return new FixedWidthFileSchema(blockingStart, blockingOff, nBlockingFields,
                                            hasId, idStart, idOff, nIdFields,
                                            fieldStart, fieldOff, nFields, fieldName);
        }

        public Builder field(String name, int start, int off) {
            if (fieldStart.length == nFields) {
                int newLength = 2 * nFields;
                fieldStart = Arrays.copyOf(fieldStart, newLength);
                fieldOff = Arrays.copyOf(fieldOff, newLength);
                fieldName = Arrays.copyOf(fieldName, newLength);
            }

            fieldName[nFields] = name;
            fieldStart[nFields] = start - 1;
            fieldOff[nFields] = off - 1;
            nFields++;

            return this;
        }

        public Builder blockingField(int start, int off) {
            if (blockingStart.length == nBlockingFields) {
                int newLength = 2 * nBlockingFields;
                blockingStart = Arrays.copyOf(blockingStart, newLength);
                blockingOff = Arrays.copyOf(blockingOff, newLength);
            }

            blockingStart[nBlockingFields] = start - 1;
            blockingOff[nBlockingFields] = off - 1;
            nBlockingFields++;

            return this;
        }

        public Builder idField(int start, int off) {
            if (!hasId)
                hasId = true;

            if (idStart.length == nIdFields) {
                int newLength = 2 * nIdFields;
                idStart = Arrays.copyOf(idStart, newLength);
                idOff = Arrays.copyOf(idOff, newLength);
            }

            idStart[nIdFields] = start - 1;
            idOff[nIdFields] = off - 1;
            nIdFields++;

            return this;
        }

        protected final static int INITIAL_CAPACITY = 10;

        private int[] blockingStart;
        private int[] blockingOff;
        private int nBlockingFields;

        private boolean hasId;
        private int[] idStart;
        private int[] idOff;
        private int nIdFields;

        private int[] fieldStart;
        private int[] fieldOff;
        private int nFields;
        private String[] fieldName;
    }

    private FixedWidthFileSchema(int[] blockingStart,
                                 int[] blockingOff,
                                 int nBlockingFields,
                                 boolean hasId,
                                 int[] idStart,
                                 int[] idOff,
                                 int nIdFields,
                                 int[] fieldStart,
                                 int[] fieldOff,
                                 int nFields,
                                 String[] fieldName) 
    {
        this.blockingStart = Arrays.copyOf(blockingStart, nBlockingFields);
        this.blockingOff = Arrays.copyOf(blockingOff, nBlockingFields);
        this.nBlockingFields = nBlockingFields;
        this.hasId = hasId;
        this.idStart = Arrays.copyOf(idStart, nIdFields);
        this.idOff = Arrays.copyOf(idOff, nIdFields);
        this.nIdFields = nIdFields;
        this.fieldStart = Arrays.copyOf(fieldStart, nFields);
        this.fieldOff = Arrays.copyOf(fieldOff, nFields);
        this.nFields = nFields;
        this.fieldName = Arrays.copyOf(fieldName, nFields);

        this.fieldIndex = new HashMap<>();
        for (int i = 0; i < nFields; i++)
            this.fieldIndex.put(fieldName[i], i);
    }

    /**
     * Get the field index corresponding to the given field name. Indices start at 0.
     *
     * @throws IllegalArgumentException if there is now field with the given name
     */
    @Override
    public int getFieldIndex(String name) {
        Integer ix = fieldIndex.get(name);

        if (ix == null)
            throw new IllegalArgumentException("No such field: " + name);

        return ix;
    }

    /**
     * Parse one line from a fixed-width file. This method assumes that the
     * line is has enough characters to build the record.
     *
     * @param line  A line from a fixed-width file
     * @return      A record representing the fields given in the line
     */
    @Override
    public Record parseRecord(String line) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nBlockingFields; i++)
            key.append(line.substring(blockingStart[i], blockingOff[i]));

        StringBuilder id = new StringBuilder();
        for (int i = 0; i < nIdFields; i++)
            id.append(line.substring(idStart[i], idOff[i]));

        Field[] fields = new Field[nFields];
        for (int i = 0; i < nFields; i++)
            fields[i] = new Field(line.substring(fieldStart[i], fieldOff[i]));

         return new Record(key.toString(), id.toString(), fields);
    }

    protected final static int INITIAL_CAPACITY = 10;

    private final int[] blockingStart;
    private final int[] blockingOff;
    private final int nBlockingFields;

    private final boolean hasId;
    private final int[] idStart;
    private final int[] idOff;
    private final int nIdFields;

    private final int[] fieldStart;
    private final int[] fieldOff;
    private final int nFields;
    private final String[] fieldName;
    private final HashMap<String, Integer> fieldIndex;
}

