package gov.census.torch;

public interface IFileSchema {
    public int getFieldIndex(String name);
    public Record parseRecord(String line);
}
