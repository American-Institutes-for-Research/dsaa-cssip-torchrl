package gov.census.torch;

/**
 * An IRecordSchema is an object that can be used to look up column and
 * field indices given names. This interface only serves to simplify some
 * method signatures.
 */
public interface IRecordSchema {
    public int fieldIndex(String name);
    public int columnIndex(String name);
}
