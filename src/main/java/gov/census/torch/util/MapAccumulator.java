package gov.census.torch.util;

import java.util.Map;
import java.util.Set;

public class MapAccumulator<Key, SingleValue, AggregateValue> 
{
    public MapAccumulator(Map<Key, AggregateValue> map,
                          IAccumulate<SingleValue, AggregateValue> acc) 
    {
        _map = map;
        _acc = acc;
    }

    public Map<Key, AggregateValue> map() {
        return _map;
    }

    public AggregateValue add(Key key, SingleValue val) {
        AggregateValue agg;

        if (_map.containsKey(key))
            agg =_acc.accumulate(_map.get(key), val);
        else
            agg = _acc.newAggregateValue(val);

        _map.put(key, agg);
        return agg;
    }

    private final Map<Key, AggregateValue> _map;
    private final IAccumulate<SingleValue, AggregateValue> _acc;
}
