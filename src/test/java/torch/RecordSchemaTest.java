package torch;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RecordSchemaTest {
    private RecordSchema schema;

    @Before
    public void setUp() {
        schema = new RecordSchema(
                new String[] {"a", "b", "c", "d", "e", "f"},
                new String[] {"a", "e"},
                "b",
                "f"
                );
    }

    @Test
    public void testConstructor() {
        assertThat(schema.fieldIndex("c"), is(0));
        assertThat(schema.columnIndex("c"), is(2));
        assertThat(schema.hasId(), is(true));
    }

    @Test
    public void testNewRecord() {
        Record rec = schema.newRecord(new String[] {"aa", "bb", "cc", "dd", "ee", "ff"});
        Record rec2 = schema.newRecord(new String[] {"aaa", "bbb", "ccc", "ddd", "eee", "fff"});

        assertThat(rec.blockingKey(), is("aaee"));
        assertThat(rec.seq(), is("bb"));
        assertThat(rec.id(), is("ff"));
        assertThat(rec.schema(), is(schema));
        assertThat(rec.nFields(), is(2));
        assertThat(rec.field(0), is(new Field("cc")));
        assertThat(rec.field(1), is(new Field("dd")));
    }

    @Test
    public void testSequenceField() {
        schema = new RecordSchema(
                new String[] {"a", "b", "c", "d", "e", "f"},
                new String[] {"a", "e"},
                null,
                "f"
                );

        Record rec1 = schema.newRecord(new String[] {"aa", "bb", "cc", "dd", "ee", "ff"});
        Record rec2 = schema.newRecord(new String[] {"aaa", "bbb", "ccc", "ddd", "eee", "fff"});
        assertThat(rec1.seq(), is("0"));
        assertThat(rec2.seq(), is("1"));
    }

    @Test
    public void testColumnIndex() {
        assertThat(schema.columnIndex("a"), is(0));
        assertThat(schema.columnIndex("d"), is(3));
    }

    @Test
    public void testFieldIndex() {
        assertThat(schema.fieldIndex("c"), is(0));
        assertThat(schema.fieldIndex("d"), is(1));
    }
}
