package gov.census.torch.comparators;

import gov.census.torch.Field;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ProratedComparatorTest {

    private ProratedComparator cmp;

    @Before
    public void setUp() {
        cmp = new ProratedComparator(
                new double[] {0.1, 0.2, 0.3},
                new double[] {0.0, 0.0, 0.0});
    }


    @Test
    public void testCompare() {
        Field f1 = new Field("100.0");
        Field f2 = new Field("100.0");
        assertThat(cmp.compare(f1, f2), is(3));

        f1 = new Field("109.0");
        assertThat(cmp.compare(f1, f2), is(3));

        f1 = new Field("111.0");
        assertThat(cmp.compare(f1, f2), is(2));

        f1 = new Field("119.0");
        assertThat(cmp.compare(f1, f2), is(2));

        f1 = new Field("121.0");
        assertThat(cmp.compare(f1, f2), is(1));

        f1 = new Field("129.0");
        assertThat(cmp.compare(f1, f2), is(1));

        f1 = new Field("131.0");
        assertThat(cmp.compare(f1, f2), is(0));
    }

}
