package gov.census.torch;

import java.util.List;

public interface IRecordLoader {
    public List<Record> load(String source)
        throws RecordLoadingException;

    public RecordSchema schema();
}
