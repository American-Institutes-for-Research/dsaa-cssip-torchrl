package torch.io;

import torch.Record;
import torch.RecordLoadingException;
import torch.RecordSchema;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

public class DelimitedFileSchema 
    implements torch.IRecordLoader, CSVEntryParser<Record>
{
    /**
     * Construct a new DelimitedFileSchema instance. Columns names must be
     * specified to the builder whether or not there is a header row. Setting
     * 'header' to true means that the first row of the file will be skipped.
     */
    public static class Builder {
        public final static char COMMENT_CHAR = '#';
        public final static char QUOTE_CHAR = '"';
        public final static boolean IGNORE_EMPTY_LINES = true;
        public final static String BLANK_INDICATOR = "NA";

        public Builder() {
            _delimiter = ',';
            _header = false;
            _columns = new LinkedList<>(); 
            _blockingFields = new LinkedList<>(); 
            _seqField = null;
            _idField = null;
            _blankIndicator = BLANK_INDICATOR;
        }

        public Builder delimiter(char d) {
            _delimiter = d;
            return this;
        }

        public Builder header(boolean b) {
            _header = b;
            return this;
        }

        public Builder column(String name) {
            _columns.add(name);
            return this;
        }

        public Builder columns(String... names) {
            for (String name: names)
                _columns.add(name);

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

        public Builder blankIndicator(String indicator) {
            _blankIndicator = indicator;
            return this;
        }

        public DelimitedFileSchema build() {
            String[] columns = _columns.toArray(new String[0]);
            String[] blockingFields = _blockingFields.toArray(new String[0]);
            RecordSchema schema = new RecordSchema(columns, blockingFields, _seqField, _idField);

            CSVStrategy strategy = 
                new CSVStrategy(_delimiter, QUOTE_CHAR, COMMENT_CHAR, _header, IGNORE_EMPTY_LINES);

            return new DelimitedFileSchema(schema, strategy, _blankIndicator);
        }

        private char _delimiter;
        private boolean _header;
        private final LinkedList<String> _columns, _blockingFields;
        private String _seqField, _idField, _blankIndicator;
    }

    @Override
    public List<Record> load(String file) 
        throws RecordLoadingException
    {
        FileReader rdr = null;
        try {
            rdr = new FileReader(file);
        }
        catch(IOException e) {
            String msg = "There was a problem opening the file: " + file;

            if (rdr != null) {
                try {
                    rdr.close();
                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RecordLoadingException(msg, e);
        }

        CSVReader<Record> csv =
            new CSVReaderBuilder<Record>(rdr)
            .strategy(_strategy)
            .entryParser(this)
            .build();

        List<Record> list = null;

        try {
            list = csv.readAll();
        }
        catch(IOException e) {
            String msg = "There was a problem reading from the file: " + file;
            throw new RecordLoadingException(msg, e);
        }
        finally {
            if (rdr != null) {
                try {
                    rdr.close();
                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return list;
    }

    @Override
    public RecordSchema schema() {
        return _schema;
    }

    @Override
    public Record parseEntry(String... columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(_blankIndicator))
                columns[i] = "";
        }

        return _schema.newRecord(columns);
    }

    private DelimitedFileSchema(RecordSchema schema, CSVStrategy strategy, String blankIndicator) {
        _schema = schema;
        _strategy = strategy;
        _blankIndicator = blankIndicator;
    }

    private final RecordSchema _schema;
    private final CSVStrategy _strategy;
    private final String _blankIndicator;
}
