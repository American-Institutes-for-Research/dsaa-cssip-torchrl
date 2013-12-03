package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * An object that counts occurences of record comparison patterns
 */
public class Counter {

    /**
     * Count the comparison patterns for blocked pairs in the two lists.
     */
    public static Counter count(RecordComparator cmp, List<Record> list1, List<Record> list2)
    {
        IncrementalCounter inc = new IncrementalCounter(cmp);
        Map<String, List<Record>> blocks = Record.block(list1);

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                List<Record> thisBlock = blocks.get(key);
                for (Record otherRec: thisBlock)
                    inc.add(rec, otherRec);
            }
        }

        return inc.toCounter();
    }

    /**
     * Counts truth patterns. This method uses blocking to bring together pairs, and so may not
     * count every true match.
     *
     * @return a length 2 Counter array, in which the first element represents the observed patterns
     * among match pairs, and the second element represents observed patterns amont nonmatch pairs.
     */
    public static Counter[] countTruth(RecordComparator cmp, List<Record> list1, List<Record> list2) {
        IncrementalCounter trueMatch = new IncrementalCounter(cmp);
        IncrementalCounter trueNonmatch = new IncrementalCounter(cmp);

        if (list1.size() > 0 && list2.size() > 0) {
            if (!(list1.get(0).schema().hasId() && list2.get(0).schema().hasId()))
                throw new IllegalArgumentException("Records must have ID fields");
        }

        Map<String, List<Record>> blocks = Record.block(list1);

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                List<Record> thisBlock = blocks.get(key);
                for (Record otherRec: thisBlock) {
                    if (rec.id().equals(otherRec.id()))
                        trueMatch.add(rec, otherRec);
                    else
                        trueNonmatch.add(rec, otherRec);
                }
            }
        }

        return new Counter[] {trueMatch.toCounter(), trueNonmatch.toCounter()};
    }

    public Counter(RecordComparator cmp, SortedMap<Integer, Integer> countMap) {
        _cmp = cmp;
        _countMap = countMap;

        // compact the counts into an array

        _nonzeroCounts = new int[_countMap.size()];
        _nonzeroPatterns = new int[_countMap.size()][cmp.nComparators()];
        _nonzeroPatternIndex = new int[_countMap.size()];
        int insertIndex = 0;

        int total = 0;
        int maxCount = 0;
        for (Integer ix: _countMap.keySet()) {
            _nonzeroPatternIndex[insertIndex] = ix;
            int thisCount = _countMap.get(ix);
            _nonzeroCounts[insertIndex] = thisCount;
            total += thisCount;

            if (thisCount > maxCount)
                maxCount = thisCount;

            int[] pattern = cmp.patternFor(ix);
            for (int k = 0; k < pattern.length; k++)
                _nonzeroPatterns[insertIndex][k] = pattern[k];

            insertIndex++;
        }

        _total = total;
        _maxCount = maxCount;
    }

    /**
     * Returns the <code>RecordComparator</code> object used to produce comparison patterns.
     */
    public RecordComparator recordComparator() {
        return _cmp;
    }

    /**
     * Returns the total number of individuals counted.
     */
    public int total() {
        return _total;
    }

    /**
     * Returns the nonzero counts. Each element of the returned array is a positive integer
     * corresponding to the number of times a certain pattern was observed. The <code>i</code>th
     * entry corresponds to the <code>i</code>th pattern in {@link #nonzeroPatterns}.
     */
    public int[] nonzeroCounts() {
        return _nonzeroCounts;
    }

    /**
     * Returns an array of patterns that were observed at least once. The number of times the
     * <code>i</code>th pattern was observed is given by the <code>i</code>th entry in {@link
     * #nonzeroCounts}.
     */
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
            builder.append(String.format(fmt, java.util.Arrays.toString(_nonzeroPatterns[i]), _nonzeroCounts[i]));

        return builder.toString();
    }

    private final RecordComparator _cmp;
    private final SortedMap<Integer, Integer> _countMap;
    private final int[] _nonzeroCounts, _nonzeroPatternIndex;
    private final int[][] _nonzeroPatterns;
    private final int _maxCount, _total;
}
