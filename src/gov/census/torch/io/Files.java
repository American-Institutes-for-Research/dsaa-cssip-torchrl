package gov.census.torch.io;

import gov.census.torch.Record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/23/13
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class Files {

    public static LinkedList<Record> loadFixedWidthFile(String name, FixedWidthFileSchema schema)
            throws IOException
    {
        LinkedList<Record> records = new LinkedList<>();
        BufferedReader in = new BufferedReader(new FileReader(name));
        String line;

        while ((line = in.readLine()) != null) {
            Record record = schema.parseRecord(line);
            records.add(record);
        }

        return records;
    }
}
