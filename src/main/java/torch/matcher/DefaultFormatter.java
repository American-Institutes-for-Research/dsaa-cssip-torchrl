package torch.matcher;

import torch.Record;
import torch.RecordComparator;

import java.io.Writer;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVColumnJoinerImpl;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

public class DefaultFormatter 
    implements IMatchingFormatter
{

    public DefaultFormatter(Writer writer, RecordComparator cmp, double cutoff) 
        throws torch.FormatterException
    {
        MatchRecordEntryConverter converter = new MatchRecordEntryConverter(cmp);
        CSVColumnJoinerImpl joiner = new CSVColumnJoinerImpl();

        try {
            // write the header row
            String header = 
                joiner.joinColumns(converter.header(), CSVStrategy.UK_DEFAULT);
            writer.write(header);
            writer.write('\n');
            writer.flush();
        }
        catch (java.io.IOException e) {
            throw new torch.FormatterException("There was a problem formattig the output", e);
        }

        _csvWriter = 
            new CSVWriterBuilder<MatchRecord>(writer)
            .entryConverter(converter)
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
