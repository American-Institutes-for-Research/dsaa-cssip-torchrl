package torch.io;

import torch.IRecordLoader;
import torch.IRecordIterator;
import torch.Record;
import torch.RecordLoadingException;
import torch.RecordSchema;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.List;

public class FixedWidthFileSchema implements IRecordLoader
{
    public static class Builder {

        public Builder() {
            _columns = new LinkedList<>();
            _blockingFields = new LinkedList<>();
            _columnStart = new LinkedList<>();
            _columnOff = new LinkedList<>();
            _seqField = null;
            _idField = null;
        }

        public FixedWidthFileSchema build() {
            String[] columns = _columns.toArray(new String[0]);
            String[] blockingFields = _blockingFields.toArray(new String[0]);
            RecordSchema schema = new RecordSchema(columns, blockingFields, _seqField, _idField);

            int[] columnStart = new int[_columnStart.size()];
            int[] columnOff = new int[_columnStart.size()];
            for (int i = 0; i < _columnStart.size(); i++) {
                columnStart[i] = _columnStart.get(i);
                columnOff[i] = _columnOff.get(i);
            }

            return new FixedWidthFileSchema(schema, columnStart, columnOff);
        }

        public Builder column(String name, int start, int off) {
            _columns.add(name);
            _columnStart.add(start);
            _columnOff.add(off);
            return this;
        }

        public Builder blockingField(String name) {
            _blockingFields.add(name);
            return this;
        }

        public Builder blockingFields(String... names) {
            for (String name: names)
                _blockingFields.add(name);

            return this;
        }

        public Builder seqField(String name) {
            _seqField = name;
            return this;
        }


        public Builder idField(String name) {
            _idField = name;
            return this;
        }

        private final LinkedList<String> _columns, _blockingFields;
        private final LinkedList<Integer> _columnStart, _columnOff;
        private String _seqField, _idField;
    }


    @Override
    public IRecordIterator load(String file)
        throws RecordLoadingException
    {
        try {
            return new FixedWidthFileIterator(this, file);
        }
        catch(IOException e) {
            String msg = "There was a problem opening the file: " + file;
            throw new RecordLoadingException(msg, e);
        }
    }

    @Override
    public RecordSchema schema() {
        return _schema;
    }

    /**
     * Construct a new record from the given line
     */
    public Record newRecord(String line) {
        String[] columns = new String[_columnStart.length];
        for (int i = 0; i < _columnStart.length; i++)
            columns[i] = line.substring(_columnStart[i], _columnOff[i]);

        return _schema.newRecord(columns);
    }

    private FixedWidthFileSchema(RecordSchema schema, int[] columnStart, int[] columnOff) {
        _schema = schema;
        _columnStart = columnStart;
        _columnOff = columnOff;
    }

    private final RecordSchema _schema;
    private final int[] _columnStart, _columnOff;
}

