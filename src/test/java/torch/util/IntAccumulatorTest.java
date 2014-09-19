package torch.util;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class IntAccumulatorTest {
    IntAccumulator acc = IntAccumulator.INSTANCE;
    P<Integer> ptr;

    @Before
    public void setup() {
        ptr = acc.create(99);
    }

    @Test
    public void testCreate() {
        assertThat(ptr.value, is(99));
    }

    @Test
    public void testAccumulate() {
        acc.accumulate(ptr, 1);
        assertThat(ptr.value, is(100));

        acc.accumulate(ptr, 100);
        assertThat(ptr.value, is(200));
    }
}
