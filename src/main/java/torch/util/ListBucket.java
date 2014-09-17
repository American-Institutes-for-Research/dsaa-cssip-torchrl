package torch.util;

import java.util.LinkedList;
import java.util.List;

public class ListBucket<V>
    implements IBucket<V, List<V>>
{
    public List<V> create(V init) {
        LinkedList<V> list = new LinkedList<>();
        list.add(init);
        return list;
    }

    public void accumulate(List<V> list, V val) {
        list.add(val);
    }
}
