package gov.census.torch.counter;

import gov.census.torch.RecordComparator;

import java.util.SortedMap;

/**
 * An immutable object to store pattern counts.
 */
public class Tally {
    public Tally(RecordComparator cmp, SortedMap<Integer, Integer> countMap) {
        _cmp = cmp;
        _countMap = countMap;

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

    /**
     * Returns the <code>RecordComparator</code> object used to produce comparison patterns.
     */
    public RecordComparator recordComparator() {
        return _cmp;
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
     * nonzeroCounts}.
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
    private final int _maxCount;
}
