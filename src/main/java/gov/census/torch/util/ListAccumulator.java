package gov.census.torch.util;

import java.util.LinkedList;
import java.util.List;

public class ListAccumulator<V>
    implements IAccumulate<V, List<V>>
{
    public List<V> newAggregateValue(V init) {
        LinkedList<V> list = new LinkedList<>();
        list.add(init);
        return list;
    }

    public List<V> accumulate(List<V> list, V val) {
        list.add(val);
        return list;
    }
}
