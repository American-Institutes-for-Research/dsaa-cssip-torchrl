package gov.census.torch;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IFieldComparator {
    public int compare(Field field1, Field field2);
    public int nLevels();
}
