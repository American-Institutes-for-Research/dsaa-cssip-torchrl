package gov.census.torch.test;

import gov.census.torch.RecordComparator;
import gov.census.torch.comparators.StandardComparators;
import gov.census.torch.io.FixedWidthFileSchema;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RecordComparatorTest {

    private FixedWidthFileSchema schema1;
    private FixedWidthFileSchema schema2;
    private RecordComparator cmp;

    @Before
    public void setUp() {
        schema1 = 
            new FixedWidthFileSchema.Builder()
            .blockingField(1, 5)
            .field("first", 5, 15)
            .field("last", 15, 25)
            .field("age", 25, 25)
            .build();

        schema2 = 
            new FixedWidthFileSchema.Builder()
            .blockingField(1, 5)
            .field("first", 5, 15)
            .field("last", 15, 25)
            .field("age", 25, 25)
            .build();

        cmp =
            new RecordComparator.Builder(schema1, schema2, false)
            .comparator("last", StandardComparators.STRING)
            .comparator("age", StandardComparators.YEAR)
            .comparator("first", StandardComparators.STRING)
            .build();
    }


    @Test
    public void testBuilder() {
        assertThat(cmp.handleBlanks(), is(false));
        assertThat(cmp.nComparators(), is(3));
        assertThat(cmp.nPatterns(), is(3 * 4 * 3));
    }

    @Test
    public void testPatternIndex() {
        int[] pattern = new int[3];

        int n = 0;
        for (int k = 0; k < 3; k++) {
            pattern[2] = k;
            for (int j = 0; j < 4; j++) {
                pattern[1] = j;
                for (int i = 0; i < 3; i++) {
                    pattern[0] = i;
                    assertThat(cmp.patternIndex(pattern), is(n));
                    n++;
                }
            }
        }
    }

    @Test
    public void testPatternForm() {
        int[] pattern = new int[3];

        int n = 0;
        for (int k = 0; k < 3; k++) {
            pattern[2] = k;
            for (int j = 0; j < 4; j++) {
                pattern[1] = j;
                for (int i = 0; i < 3; i++) {
                    pattern[0] = i;
                    assertThat(cmp.patternFor(n), is(pattern));
                    n++;
                }
            }
        }
    }
}
