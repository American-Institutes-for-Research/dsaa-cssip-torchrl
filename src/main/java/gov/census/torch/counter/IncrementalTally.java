package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.SortedMap;
import java.util.TreeMap;

public class IncrementalTally {

    public IncrementalTally(RecordComparator cmp) {
        _cmp = cmp;
        _countMap = new TreeMap<>();
    } 

    public void add(Record rec1, Record rec2) {
        Integer pattern = _cmp.compareIndex(rec1, rec2);

        if (_countMap.containsKey(pattern))
            _countMap.put(pattern, _countMap.get(pattern) + 1);
        else
            _countMap.put(pattern, 1);
    }

    /**
     * Returns a new <code>Tally</code> representing the current state of the
     * <code>IncrementalTally</code>.
     */
    public Tally tally() {
        return new Tally(_cmp, _countMap);
    }

    private final RecordComparator _cmp;
    private final SortedMap<Integer, Integer> _countMap;
}
