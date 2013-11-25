package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;
import gov.census.torch.util.BucketMap;
import gov.census.torch.util.ListBucket;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DefaultMatchingAlgo 
    implements IMatchingAlgorithm
{

    public DefaultMatchingAlgo(IModel model) {
        _model = model;
    }

    @Override
    public TreeMap<Double, List<MatchRecord>>
        computeScores(List<Record> list1, List<Record> list2)
    {
        return computeScores(Record.block(list1), list2);
    }

    public TreeMap<Double, List<MatchRecord>>
        computeScores(Map<String, List<Record>> blocks, List<Record> list)
    {
        _startTime = System.currentTimeMillis();
        _nComparisons = 0;

        BucketMap<Double, MatchRecord, List<MatchRecord>> bmap =
            new BucketMap<>(new TreeMap<Double, List<MatchRecord>>(),
                            new ListBucket<MatchRecord>());

        for (Record rec: list) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                for (Record otherRec: blocks.get(key)) {
                    double score = _model.matchScore(rec, otherRec);
                    bmap.add(score, new MatchRecord(rec, otherRec, score));
                    _nComparisons++;
                }
            }
        }

        _endTime = System.currentTimeMillis();
        return (TreeMap<Double, List<MatchRecord>>)bmap.map();
    }

    /**
     * Returns the number of record comparisons that were performed.
     */
    @Override
    public int nComparisons() {
        return _nComparisons;
    }

    /**
     * Returns the elapsed time in milliseconds.
     */
    @Override
    public long elapsedTime() {
        return _endTime - _startTime;
    }

    private final IModel _model;
    private int _nComparisons;
    private long _startTime, _endTime;
}
