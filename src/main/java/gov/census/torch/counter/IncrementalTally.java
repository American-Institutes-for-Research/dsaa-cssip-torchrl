package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;
import gov.census.torch.util.IntegerAccumulator;
import gov.census.torch.util.MapAccumulator;

import java.util.TreeMap;

public class IncrementalTally {

    public IncrementalTally(RecordComparator cmp) {
        _cmp = cmp;
        _acc = new MapAccumulator<>(new TreeMap<Integer, Integer>(),
                                    IntegerAccumulator.INSTANCE);
    } 

    public void add(Record rec1, Record rec2) {
        Integer pattern = _cmp.compareIndex(rec1, rec2);
        _acc.add(pattern, 1);
    }

    /**
     * Returns a new <code>Tally</code> representing the current state of the
     * <code>IncrementalTally</code>.
     */
    public Tally tally() {
        return new Tally(_cmp, (TreeMap<Integer, Integer>)_acc.map());
    }

    private final RecordComparator _cmp;
    private final MapAccumulator<Integer, Integer, Integer> _acc;
}
