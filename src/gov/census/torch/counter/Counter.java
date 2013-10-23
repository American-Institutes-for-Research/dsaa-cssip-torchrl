package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.HashMap;
import java.util.LinkedList;

public class Counter {

    public Counter(RecordComparator comparator) {
        this.comparator = comparator;
        this._counts = new HashMap<>();
    }

    public void countPatterns(LinkedList<Record> list1, LinkedList<Record> list2) {

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
                    int[] pattern = comparator.compare(rec, otherRec);

                    if (_counts.containsKey(pattern)) {
                        int oldCount = _counts.get(pattern);
                        _counts.put(pattern, oldCount + 1);
                    }
                }
            }
        }
    }

    public HashMap<int[], Integer> counts() {
        return _counts;
    }

    private RecordComparator comparator;
    private HashMap<int[], Integer> _counts;
}
