package torch;

/**
 * An object which loads {@link Record}s from an external source.
 */
public interface IRecordLoader {
    /**
     * Returns an iterator which iterates over records from the given source.
     */
    public IRecordIterator load(String source)
        throws RecordLoadingException;

    /**
     * The {@link RecordSchema} used by the iterator to construct {@link Record}s.
     */
    public RecordSchema schema();
}
