package torch.io;

import torch.IRecordIterator;
import torch.Record;
import torch.RecordIteratorException;

import java.io.FileReader;
import java.io.IOException;

import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

public class DelimitedFileIterator 
    implements IRecordIterator
{
    public DelimitedFileIterator(DelimitedFileSchema schema, String file) 
        throws IOException
    {
        _file = file;
        java.io.FileReader rdr = new FileReader(file);

        _csv =
            new CSVReaderBuilder<Record>(rdr)
            .strategy(schema.csvStrategy())
            .entryParser(schema)
            .build();
    }

    @Override
    public Record next() 
        throws RecordIteratorException
    {
        try {
            return _csv.readNext();
        }
        catch(IOException e) {
            String msg = "There was a problem reading from the file: " + _file;
            throw new RecordIteratorException(msg, e);
        }
    }

    private String _file;
    private final CSVReader<Record> _csv;
}
