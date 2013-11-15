package gov.census.torch;

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
                new String[] {"b"}
                );
    }

    @Test
    public void testNewRecord() {
        Record rec = schema.newRecord(new String[] {"aa", "bb", "cc", "dd", "ee", "ff"});
        assertThat(rec.blockingKey(), is("aaee"));
        assertThat(rec.id(), is("bb"));
        assertThat(rec.schema(), is(schema));
        assertThat(rec.nFields(), is(3));
        assertThat(rec.field(0), is(new Field("cc")));
        assertThat(rec.field(1), is(new Field("dd")));
        assertThat(rec.field(2), is(new Field("ff")));
    }

    @Test
    public void testColumnIndex() {
        assertThat(schema.columnIndex("a"), is(0));
        assertThat(schema.columnIndex("f"), is(5));
    }

    @Test
    public void testFieldIndex() {
        assertThat(schema.fieldIndex("c"), is(0));
        assertThat(schema.fieldIndex("f"), is(2));
    }
}
