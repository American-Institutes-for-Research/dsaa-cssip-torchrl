package gov.census.torch;

import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RecordTest {
    private RecordSchema schema;
    private Record rec1, rec2, rec3, rec4, rec5;
    private List<Record> list;

    @Before 
    public void setup() {
        schema = new RecordSchema(
                new String[] {"key", "first", "last"},
                new String[] {"key"},
                new String[0]);

        rec1 = schema.newRecord(new String[] {"asdf", "George", "Washington"});
        rec2 = schema.newRecord(new String[] {"qwer", "John", "Adams"});
        rec3 = schema.newRecord(new String[] {"asdf", "Thomas", "Jefferson"});
        rec4 = schema.newRecord(new String[] {"qwer", "James", "Madison"});
        rec5 = schema.newRecord(new String[] {"asdf", "James", "Monroe"});

        list = new LinkedList<>();
        list.add(rec1);
        list.add(rec2);
        list.add(rec3);
        list.add(rec4);
        list.add(rec5);
    }

    @Test
    public void testBlocking() {
        Map<String, List<Record>> blocks = Record.block(list);
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get("asdf").size(), is(3));
        assertThat(blocks.get("qwer").size(), is(2));
    }

    @Test
    public void testNewRecord() {
        assertThat(rec1.field(0).toString(), is("George"));
        assertThat(rec2.field(1).toString(), is("Adams"));
        assertThat(rec3.nFields(), is(2));
        assertThat(rec4.schema(), is(schema));
    }
}
