package gov.census.torch.matcher;

import gov.census.torch.RecordComparator;

import com.googlecode.jcsv.writer.CSVEntryConverter;

public class MatchRecordEntryConverter 
    implements CSVEntryConverter<MatchRecord>
{

    public MatchRecordEntryConverter(RecordComparator cmp) {
        _cmp = cmp;
    }

    @Override
    public String[] convertEntry(MatchRecord matchRec) {
        String[] row = new String[1 + 2 * _cmp.nComparators()];
        row[0] = "" + matchRec.score();
        String[] fields1 = _cmp.comparisonFields(matchRec.record1());
        String[] fields2 = _cmp.comparisonFields(matchRec.record2());

        for (int i = 0; i < _cmp.nComparators(); i++) {
            row[2 * i + 1] = fields1[i];
            row[2 * i + 2] = fields2[i];
        }

        return row;
    }

    private final RecordComparator _cmp;
}
