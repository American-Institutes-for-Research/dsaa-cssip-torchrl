package gov.census.torch.matcher;

import gov.census.torch.Record;

public class MatchRecord {
    public MatchRecord(Record rec1, Record rec2, double score) {
        _rec1 = rec1;
        _rec2 = rec2;
        _score = score;
    }

    public Record record1() {
        return _rec1;
    }

    public Record record2() {
        return _rec2;
    }

    public double score() {
        return _score;
    }

    private final Record _rec1, _rec2;
    private final double _score;
}
