package torch.util;

import java.util.Map;
import java.util.Set;

/**
 * A class that wraps a map in order faciliate a common usage pattern. Namely,
 * check if key exists, if not then create a new empty container at that key
 * and add the value to the container. Uses the <code>IAccumulate</code>
 * interface.
 */
public class AccumulatorMap<Key, SingleValue, AggregateValue> 
{
    /**
     * Creates a new <code>AccumulatorMap</code>.
     *
     * @param map A <code>Map</code> where values will be stored.
     * @param acc An accumulator that will be used to create empty aggregates
     * and accumulate new values.
     */
    public AccumulatorMap(Map<Key, AggregateValue> map,
                          IAccumulate<SingleValue, AggregateValue> acc) 
    {
        _map = map;
        _acc = acc;
    }

    /**
     * Returns the underlying <code>Map</code>.
     */
    public Map<Key, AggregateValue> map() {
        return _map;
    }

    /**
     * Adds a value to the aggregate class stored at the given key. If there is no value
     * at the given key, the method will create a new empty aggregate value and add the
     * value to it.
     *
     * @param key The key where the value will be stored
     * @param val The value to add to the aggregate at the given key
     */
    public void add(Key key, SingleValue val) {
        AggregateValue agg;

        if (_map.containsKey(key))
            _acc.accumulate(_map.get(key), val);
        else {
            _map.put(key, _acc.create(val));
        }
    }

    private final Map<Key, AggregateValue> _map;
    private final IAccumulate<SingleValue, AggregateValue> _acc;
}
