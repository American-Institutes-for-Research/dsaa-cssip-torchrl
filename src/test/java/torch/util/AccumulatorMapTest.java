package torch.util;

import java.util.HashMap;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;

public class AccumulatorMapTest {
    HashMap<String, P<Integer>> map;
    AccumulatorMap<String, Integer, P<Integer>> acc;

    @Before
    public void setup() {
        map = new HashMap<>();
        acc = new AccumulatorMap<>(map, IntAccumulator.INSTANCE);
    }

    @Test
    public void testConstructor() {
        assertTrue(acc.map() == map);
        assertThat(map.size(), is(0));
    }

    @Test
    public void testAdd() {
        acc.add("foo", 7);
        acc.add("bar", 9);
        acc.add("foo", 11);

        assertThat(map.get("foo").value, is(18));
        assertThat(map.get("bar").value, is(9));
        assertTrue(map.get("baz") == null);
    }
}
