package gov.census.torch.matcher;

import gov.census.torch.Record;

public class MatchRecord {
    public MatchRecord(Record a, Record b, double score) {
        _a = a;
        _b = b;
        _score = score;
    }

    public Record a() {
        return _a;
    }

    public Record b() {
        return _b;
    }

    public double score() {
        return _score;
    }

    private final Record _a, _b;
    private final double _score;
}
