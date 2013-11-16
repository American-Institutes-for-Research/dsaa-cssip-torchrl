package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Counter {

    public Counter(RecordComparator cmp, List<Record> list1, List<Record> list2) {
        _cmp = cmp;
        _countMap = Counter.countPatterns(cmp, list1, list2);

        // compact the counts into an array

        _nonzeroCounts = new int[_countMap.size()];
        _nonzeroPatterns = new int[_countMap.size()][cmp.nComparators()];
        _nonzeroPatternIndex = new int[_countMap.size()];
        int insertIndex = 0;

        int maxCount = 0;
        for (Integer ix: _countMap.keySet()) {
            _nonzeroPatternIndex[insertIndex] = ix;
            int thisCount = _countMap.get(ix);
            _nonzeroCounts[insertIndex] = thisCount;

            if (thisCount > maxCount)
                maxCount = thisCount;

            int[] pattern = cmp.patternFor(ix);
            for (int k = 0; k < pattern.length; k++)
                _nonzeroPatterns[insertIndex][k] = pattern[k];

            insertIndex++;
        }

        _maxCount = maxCount;
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

    public String toString() {
        int countWidth = 4;
        int n = _maxCount;
        while (n > 0) {
            countWidth++;
            n /= 10;
        }

        int patternWidth = 3 * _cmp.nComparators() + 4;

        StringBuilder builder = new StringBuilder();
        String fmtTitle = "%-" + patternWidth + "s%" + countWidth + "s%n";
        String fmt = "%-" + patternWidth + "s%" + countWidth + "d%n";

        builder.append(String.format(fmtTitle, "pattern", "count"));

        for (int i = 0; i < _nonzeroPatterns.length; i++)
            builder.append(String.format(fmt, Arrays.toString(_nonzeroPatterns[i]), _nonzeroCounts[i]));

        return builder.toString();
    }

    protected static TreeMap<Integer, Integer> 
        countPatterns(RecordComparator cmp, 
                      List<Record> list1, List<Record> list2) 
    {

        TreeMap<Integer, Integer> countMap = new TreeMap<>();

        // Create an index on list1

        HashMap<String, List<Record>> index = Record.block(list1);

        // Compare records in list2 to records in list1

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!index.containsKey(key)) {
                continue;
            } else {
                List<Record> thisBlock = index.get(key);
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

    private final int _maxCount;
}
