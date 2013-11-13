package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

public class Counter {

    public Counter(RecordComparator cmp, Iterable<Record> list1, Iterable<Record> list2) {
        _cmp = cmp;
        _countMap = Counter.countPatterns(cmp, list1, list2);

        // compact the counts into an array

        _nonzeroCounts = new int[_countMap.size()];
        _nonzeroPatterns = new int[_countMap.size()][cmp.nComparators()];
        _nonzeroPatternIndex = new int[_countMap.size()];
        int insertIndex = 0;

        for (Integer ix: _countMap.keySet()) {
            _nonzeroPatternIndex[insertIndex] = ix;
            _nonzeroCounts[insertIndex] = _countMap.get(ix);

            int[] pattern = cmp.patternFor(ix);
            for (int k = 0; k < pattern.length; k++)
                _nonzeroPatterns[insertIndex][k] = pattern[k];

            insertIndex++;
        }
    }

    public RecordComparator recordComparator() {
        return _cmp;
    }

    public int[] nonzeroCounts() {
        return _nonzeroCounts;
    }

    public int[][] nonzeroPatterns() {
        return _nonzeroPatterns;
    }

    protected static TreeMap<Integer, Integer> 
        countPatterns(RecordComparator cmp, 
                      Iterable<Record> list1, Iterable<Record> list2) 
    {

        TreeMap<Integer, Integer> countMap = new TreeMap<>();

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
                    Integer pattern = cmp.compareIndex(rec, otherRec);
                    if (countMap.containsKey(pattern)) {
                        countMap.put(pattern, countMap.get(pattern) + 1);
                    } else {
                        countMap.put(pattern, 1);
                    }
                }
            }
        }

        return countMap;
    }

    private final RecordComparator _cmp;
    private final TreeMap<Integer, Integer> _countMap;
    private final int[] _nonzeroCounts;
    private final int[][] _nonzeroPatterns;
    private final int[] _nonzeroPatternIndex;
}
