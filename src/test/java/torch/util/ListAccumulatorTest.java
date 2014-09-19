package torch.util;

import java.util.List;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ListAccumulatorTest {
    ListAccumulator<Integer> acc;
    List<Integer> list;


    @Before
    public void setup() {
        acc = new ListAccumulator<>();
        list = acc.create(99);
    }

    @Test
    public void testCreate() {
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is(99));
    }

    @Test
    public void testAccumulate() {
        acc.accumulate(list, 100);
        assertThat(list.size(), is(2));
        assertThat(list.get(0), is(99));
        assertThat(list.get(1), is(100));

        acc.accumulate(list, 101);
        assertThat(list.size(), is(3));
        assertThat(list.get(2), is(101));
    }
}
