package gov.census.torch;

import java.util.List;

/**
 * An object which loads {@link Record}s from an external source.
 */
public interface IRecordLoader {
    /**
     * Loads records from the specified <code>source</code> and returns them as
     * a <code>List</code>.
     */
    public List<Record> load(String source)
        throws RecordLoadingException;

    /**
     * The {@link RecordSchema} used to construct the returned <code>Record</code>s.
     */
    public RecordSchema schema();
}
