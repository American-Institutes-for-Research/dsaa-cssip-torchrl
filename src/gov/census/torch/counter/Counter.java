package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.HashMap;
import java.util.LinkedList;

public class Counter {

    public Counter(RecordComparator comparator) {
        this.comparator = comparator;
        this._counts = new int[comparator.nPatterns()];
    }

    public void countPatterns(Iterable<Record> list1, Iterable<Record> list2) {

        // Create an index on list1

        HashMap<String, LinkedList<Record>> index = new HashMap<>();
        for (Record rec: list1) {
            String key = rec.blockingKey();

            if (index.containsKey(key)) {
                index.get(key).add(rec);
            } else {
                LinkedList<Record> ll = new LinkedList<>();
                ll.add(rec);
                index.put(key, ll);
            }
        }
        
        // Compare records in list2 to records in list1

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!index.containsKey(key)) {
                continue;
            } else {
                LinkedList<Record> thisBlock = index.get(key);
                for (Record otherRec: thisBlock) {
                    int pattern = comparator.compareIndex(rec, otherRec);
                    _counts[pattern]++;
                }
            }
        }
    }

    public int[] counts() {
        return _counts;
    }

    private RecordComparator comparator;
    private int[] _counts;
}
