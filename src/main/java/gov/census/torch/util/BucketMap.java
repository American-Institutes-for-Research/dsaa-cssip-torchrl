package gov.census.torch.util;

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

    public AggregateValue add(Key key, SingleValue val) {
        AggregateValue agg;

        if (_map.containsKey(key))
            agg =_bucket.accumulate(_map.get(key), val);
        else
            agg = _bucket.create(val);

        _map.put(key, agg);
        return agg;
    }

    private final Map<Key, AggregateValue> _map;
    private final IBucket<SingleValue, AggregateValue> _bucket;
}
