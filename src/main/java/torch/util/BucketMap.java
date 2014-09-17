package torch.util;

import java.util.Map;
import java.util.Set;

public class BucketMap<Key, SingleValue, AggregateValue> 
{
    public BucketMap(Map<Key, AggregateValue> map,
                     IBucket<SingleValue, AggregateValue> bucket) 
    {
        _map = map;
        _bucket = bucket;
    }

    public Map<Key, AggregateValue> map() {
        return _map;
    }

    public void add(Key key, SingleValue val) {
        AggregateValue agg;

        if (_map.containsKey(key))
            _bucket.accumulate(_map.get(key), val);
        else {
            _map.put(key, _bucket.create(val));
        }
    }

    private final Map<Key, AggregateValue> _map;
    private final IBucket<SingleValue, AggregateValue> _bucket;
}
