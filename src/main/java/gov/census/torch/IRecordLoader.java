package gov.census.torch;

import java.util.List;

public interface IRecordLoader extends IRecordSchema {
    public List<Record> load(String source)
        throws RecordLoadingException;

    public RecordSchema schema();
}
