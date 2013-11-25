package gov.census.torch.matcher;

import gov.census.torch.Record;

import java.util.List;
import java.util.TreeMap;

public interface IMatchingAlgorithm {
    public TreeMap<Double, List<MatchRecord>>
        computeScores(List<Record> list1, List<Record> list2);

    public int nComparisons();
    public long elapsedTime();
}
