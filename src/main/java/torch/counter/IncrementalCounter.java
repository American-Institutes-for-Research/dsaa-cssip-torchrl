package torch.counter;

import torch.Record;
import torch.RecordComparator;
import torch.util.IntBucket;
import torch.util.BucketMap;

import java.util.TreeMap;

public class IncrementalCounter {

    public IncrementalCounter(RecordComparator cmp) {
        _cmp = cmp;
        _acc = new BucketMap<>(new TreeMap<Integer, Integer>(),
                               IntBucket.INSTANCE);
    } 

    public void add(Record rec1, Record rec2) {
        Integer pattern = _cmp.compareIndex(rec1, rec2);
        _acc.add(pattern, 1);
    }

    /**
     * Returns a new <code>Counter</code> representing the current state of the
     * <code>IncrementalCounter</code>.
     */
    public Counter toCounter() {
        return new Counter(_cmp, (TreeMap<Integer, Integer>)_acc.map());
    }

    private final RecordComparator _cmp;
    private final BucketMap<Integer, Integer, Integer> _acc;
}
