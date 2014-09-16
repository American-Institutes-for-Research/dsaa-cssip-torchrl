package torch.comparators;

import torch.Field;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ComparatorsTest {

    @Test
    public void testProratedComparator() {

        ProratedComparator cmp = 
            new ProratedComparator(
                new double[] {0.1, 0.2, 0.3},
                new double[] {0.0, 0.0, 0.0});

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

    @Test
    public void testAbsoluteDifferenceComparator() {
        AbsoluteDifferenceComparator cmp = 
            new AbsoluteDifferenceComparator( new double[] {0.0, 3.0, 10.0});

        Field f1 = new Field("100.0");
        Field f2 = new Field("100.0");
        assertThat(cmp.compare(f1, f2), is(3));

        f1 = new Field("102.0");
        assertThat(cmp.compare(f1, f2), is(2));

        f1 = new Field("108.0");
        assertThat(cmp.compare(f1, f2), is(1));

        f1 = new Field("111.0");
        assertThat(cmp.compare(f1, f2), is(0));
    }

}
