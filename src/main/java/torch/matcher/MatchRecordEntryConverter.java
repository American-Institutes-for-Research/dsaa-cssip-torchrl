package torch.matcher;

import torch.RecordComparator;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * An implementation of <code>CSVEntryConverter</code> which is used to output
 * <code>MatchRecord</code>s in CSV format.
 */
public class MatchRecordEntryConverter 
    implements CSVEntryConverter<MatchRecord>
{

    public MatchRecordEntryConverter(RecordComparator cmp) {
        _cmp = cmp;
    }

    @Override
    public String[] convertEntry(MatchRecord matchRec) {
        String[] row = new String[3 + 2 * _cmp.nComparators()];
        row[0] = "" + matchRec.score();
        row[1] = matchRec.record1().seq();
        row[2] = matchRec.record2().seq();
        String[] fields1 = _cmp.comparisonFields(matchRec.record1());
        String[] fields2 = _cmp.comparisonFields(matchRec.record2());

        for (int i = 0; i < _cmp.nComparators(); i++) {
            row[2 * i + 3] = fields1[i];
            row[2 * i + 4] = fields2[i];
        }

        return row;
    }

    private final RecordComparator _cmp;
}
