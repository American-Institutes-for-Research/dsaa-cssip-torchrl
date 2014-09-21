package torch.matcher;

import torch.IRecordIterator;
import torch.Record;

import java.util.TreeMap;

public interface IMatchingAlgorithm {
    /**
     * Computes match scores for the records in the two lists.
     */
    public void computeScores(IRecordIterator list1, IRecordIterator list2)
        throws torch.FormatterException, torch.RecordIteratorException;

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
