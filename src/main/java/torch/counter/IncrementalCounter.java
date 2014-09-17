package torch.counter;

import torch.Record;
import torch.RecordComparator;
import torch.util.BucketMap;
import torch.util.IntBucket;
import torch.util.P;

import java.util.TreeMap;

public class IncrementalCounter {

    public IncrementalCounter(RecordComparator cmp) {
        _cmp = cmp;
        _acc = new BucketMap<>(new TreeMap<Integer, P<Integer>>(),
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
        TreeMap<Integer, P<Integer>> map = (TreeMap<Integer, P<Integer>>)_acc.map();
        TreeMap<Integer, Integer> imap = new TreeMap<>();

        for (Integer k: map.keySet())
            imap.put(k, map.get(k).value);

        return new Counter(_cmp, imap);
    }

    private final RecordComparator _cmp;
    private final BucketMap<Integer, Integer, P<Integer>> _acc;
}
