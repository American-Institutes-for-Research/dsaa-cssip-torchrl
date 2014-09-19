package torch.counter;

import torch.Record;
import torch.RecordComparator;
import torch.util.AccumulatorMap;
import torch.util.IntAccumulator;
import torch.util.P;

import java.util.TreeMap;

/**
 * A utility class to build up a <code>Counter</code> incrementally from record
 * comparisons.
 */
public class IncrementalCounter {

    /**
     * Constructs a new incremental counter.
     *
     * @param cmp The comparator that will be used to compare records.
     */
    public IncrementalCounter(RecordComparator cmp) {
        _cmp = cmp;

        TreeMap<Integer, P<Integer>> map = new TreeMap<>();
        _acc = new AccumulatorMap<>(map, IntAccumulator.INSTANCE);
    } 

    /**
     * Compute the comparison pattern between the two records and increment the tally
     * for the result.
     */
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
    private final AccumulatorMap<Integer, Integer, P<Integer>> _acc;
}
