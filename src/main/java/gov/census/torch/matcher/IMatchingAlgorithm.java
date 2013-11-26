package gov.census.torch.matcher;

import gov.census.torch.Record;

import java.util.List;
import java.util.TreeMap;

public interface IMatchingAlgorithm {
    /**
     * Computes match scores for the records in the two lists.
     */
    public TreeMap<Double, List<MatchRecord>>
        computeScores(List<Record> list1, List<Record> list2);

    /**
     * Returns the number of record comparisons that were performed during the last call to
     * <code>computeScores</code>.
     */
    public int nComparisons();

    /**
     * Returns the elapsed time in milliseconds of the last call to
     * <code>computeScores</code>.
     */
    public long elapsedTime();
}
