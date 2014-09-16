package torch;

/**
 * An object which compares two <code>Field</code>s.
 */
public interface IFieldComparator {
    /**
     * Compare <code>field1</code> and <code>field2</code>. The result should
     * be an <code>int</code> number between 0 and <code>nLevels() - 1</code>.
     */
    public int compare(Field field1, Field field2);

    /**
     * The number of possible values returned by this comparator.
     */
    public int nLevels();
}
