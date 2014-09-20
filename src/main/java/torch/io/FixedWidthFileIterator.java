package torch.io;

import torch.IRecordIterator;
import torch.Record; 
import torch.RecordIteratorException; 

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class FixedWidthFileIterator 
    implements IRecordIterator
{
    public FixedWidthFileIterator(FixedWidthFileSchema schema, String file) 
        throws IOException
    {
        _schema = schema;
        _file = file;
        _in = new BufferedReader(new FileReader(file));
    }

    @Override
    public Record next()
        throws RecordIteratorException
    {
        try {
            String line = _in.readLine();
            Record record = null;

            if (line != null) {
                record = _schema.newRecord(line);
            } else {
                try { 
                    _in.close(); 
                }
                catch(IOException ex) { 
                    ex.printStackTrace(); 
                }
            }

            return record;
        }
        catch(IOException e) {
            String msg = "There was a problem reading from the file: " + _file;

            try {
                _in.close();
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }

            throw new RecordIteratorException(msg, e);
        }
    }

    private final FixedWidthFileSchema _schema;
    private final String _file;
    private final BufferedReader _in;
}
