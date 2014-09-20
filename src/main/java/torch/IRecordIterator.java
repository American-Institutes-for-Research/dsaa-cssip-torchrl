package torch;

public interface IRecordIterator {
    public Record next() throws RecordIteratorException;
}
