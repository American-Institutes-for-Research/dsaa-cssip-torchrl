package gov.census.torch;

public interface IModel {
    public double matchScore(Record rec1, Record rec2);
    public RecordComparator recordComparator();
}
