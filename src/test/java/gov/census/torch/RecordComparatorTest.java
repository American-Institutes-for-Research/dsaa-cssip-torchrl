package gov.census.torch;

import gov.census.torch.comparators.StandardComparators;
import gov.census.torch.io.FixedWidthFileSchema;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RecordComparatorTest {

    private FixedWidthFileSchema schema1;
    private FixedWidthFileSchema schema2;
    private RecordComparator cmp, cmpBlanks;

    @Before
    public void setUp() {
        schema1 = 
            new FixedWidthFileSchema.Builder()
            .column("zip", 1, 5)
            .column("first", 5, 15)
            .column("last", 15, 25)
            .column("age", 25, 25)
            .blockingField("zip")
            .build();

        schema2 = 
            new FixedWidthFileSchema.Builder()
            .column("age", 25, 25)
            .column("last", 15, 25)
            .column("first", 5, 15)
            .column("zip", 1, 5)
            .blockingField("zip")
            .build();

        cmp =
            new RecordComparator.Builder(schema1, schema2)
            .compare("last", StandardComparators.STRING)
            .compare("age", StandardComparators.YEAR)
            .compare("first", StandardComparators.STRING)
            .handleBlanks(false)
            .build();

        cmpBlanks = 
            new RecordComparator.Builder(schema1, schema2)
            .compare("last", StandardComparators.STRING)
            .compare("age", StandardComparators.YEAR)
            .compare("first", StandardComparators.STRING)
            .handleBlanks(true)
            .build();
    }


    @Test
    public void testBuilder() {
        assertThat(cmp.handleBlanks(), is(false));
        assertThat(cmp.nComparators(), is(3));
        assertThat(cmp.nPatterns(), is(4 * 4 * 4));

        assertThat(cmpBlanks.handleBlanks(), is(true));
        assertThat(cmpBlanks.nComparators(), is(3));
        assertThat(cmpBlanks.nPatterns(), is(5 * 5 * 5));
    }

    @Test
    public void testPatternIndex() {
        int[] pattern = new int[3];

        int n = 0;
        for (int k = 0; k < 4; k++) {
            pattern[2] = k;
            for (int j = 0; j < 4; j++) {
                pattern[1] = j;
                for (int i = 0; i < 4; i++) {
                    pattern[0] = i;
                    assertThat(cmp.patternIndex(pattern), is(n));
                    n++;
                }
            }
        }

        n = 0;
        for (int k = 0; k < 5; k++) {
            pattern[2] = k;
            for (int j = 0; j < 5; j++) {
                pattern[1] = j;
                for (int i = 0; i < 5; i++) {
                    pattern[0] = i;
                    assertThat(cmpBlanks.patternIndex(pattern), is(n));
                    n++;
                }
            }
        }
    }

    @Test
    public void testPatternFor() {
        int[] pattern = new int[3];

        int n = 0;
        for (int k = 0; k < 4; k++) {
            pattern[2] = k;
            for (int j = 0; j < 4; j++) {
                pattern[1] = j;
                for (int i = 0; i < 4; i++) {
                    pattern[0] = i;
                    assertThat(cmp.patternFor(n), is(pattern));
                    n++;
                }
            }
        }

        n = 0;
        for (int k = 0; k < 5; k++) {
            pattern[2] = k;
            for (int j = 0; j < 5; j++) {
                pattern[1] = j;
                for (int i = 0; i < 5; i++) {
                    pattern[0] = i;
                    assertThat(cmpBlanks.patternFor(n), is(pattern));
                    n++;
                }
            }
        }
    }

    @Test
    public void testComparisonFields() {
        Record rec1 = schema1.schema().newRecord(new String[] {
            "12345", "john", "smith", "99"
        });

        Record rec2 = schema2.schema().newRecord(new String[] {
            "11", "smythe", "jane", "54321"
        });

        String[] fields1 = new String[] {"smith", "99", "john"};
        String[] fields2 = new String[] {"smythe", "11", "jane"};

        assertThat(cmp.comparisonFields1(rec1), is(fields1));
        assertThat(cmp.comparisonFields2(rec2), is(fields2));
    }
}
