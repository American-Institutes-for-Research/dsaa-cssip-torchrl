package torch.matcher;

import torch.Record;
import torch.RecordComparator;

import java.io.Writer;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

public class DefaultFormatter 
    implements IMatchingFormatter
{

    public DefaultFormatter(Writer writer, RecordComparator cmp, double cutoff) {
        _csvWriter = 
            new CSVWriterBuilder<MatchRecord>(writer)
            .entryConverter(new MatchRecordEntryConverter(cmp))
            .strategy(CSVStrategy.UK_DEFAULT)
            .build();

        _cutoff = cutoff;
    }

    @Override
    public void format(Record rec1, Record rec2, double score) 
        throws torch.FormatterException
    {
        if (score >= _cutoff) {
            try {
                _csvWriter.write(new MatchRecord(rec1, rec2, score));
                _csvWriter.flush();
            }
            catch (java.io.IOException e) {
                throw new torch.FormatterException("There was a problem formatting the output", e);
            }
        }
    }

    private final CSVWriter<MatchRecord> _csvWriter;
    private final double _cutoff;
}
