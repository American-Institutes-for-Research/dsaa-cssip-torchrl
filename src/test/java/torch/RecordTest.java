package torch;

import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RecordTest {
    private RecordSchema schema;
    private Record rec1, rec2, rec3, rec4, rec5;
    private IRecordIterator list;

    @Before 
    public void setup() {
        schema = new RecordSchema(
                new String[] {"key", "first", "last"},
                new String[] {"key"},
                null, null);

        rec1 = schema.newRecord(new String[] {"asdf", "George", "Washington"});
        rec2 = schema.newRecord(new String[] {"qwer", "John", "Adams"});
        rec3 = schema.newRecord(new String[] {"asdf", "Thomas", "Jefferson"});
        rec4 = schema.newRecord(new String[] {"qwer", "James", "Madison"});
        rec5 = schema.newRecord(new String[] {"asdf", "James", "Monroe"});

        LinkedList<Record> ll = new LinkedList<>();
        ll.add(rec1);
        ll.add(rec2);
        ll.add(rec3);
        ll.add(rec4);
        ll.add(rec5);
        list = new RecordIterator(ll);
    }

    @Test
    public void testBlocking() {
        Map<String, List<Record>> blocks = null;
        try {
            blocks = Record.block(list);
        }
        catch(RecordIteratorException e) {}

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
