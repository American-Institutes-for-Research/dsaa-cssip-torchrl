package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;
import gov.census.torch.util.BucketMap;
import gov.census.torch.util.ListBucket;

import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelMatchingAlgo 
    implements IMatchingAlgorithm
{

    public ParallelMatchingAlgo(IModel model, int workThreshold) 
    {
        _workThreshold = workThreshold;
        _model = model;
        _queue = new ConcurrentLinkedQueue<MatchRecord>();
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
        _blocks = blocks;
        _records = list.toArray(new Record[0]);

        BucketMap<Double, MatchRecord, List<MatchRecord>> bmap =
            new BucketMap<>(new TreeMap<Double, List<MatchRecord>>(),
                            new ListBucket<MatchRecord>());
        ForkJoinPool pool = new ForkJoinPool();
        MatchAction action = new MatchAction(0, _records.length);
        pool.execute(action);

        MatchRecord matchRec;
        while (!action.isDone()) {
            if ((matchRec = _queue.poll()) != null) {
                bmap.add(matchRec.score(), matchRec);
                _nComparisons++;
            }
        }
        
        // make sure the queue is empty
        for (MatchRecord mrec: _queue) {
            bmap.add(mrec.score(), mrec);
            _nComparisons++;
        }

        _endTime = System.currentTimeMillis();
        return (TreeMap<Double, List<MatchRecord>>)bmap.map();
    }

    @Override
    public int nComparisons() {
        return _nComparisons;
    }

    @Override
    public long elapsedTime() {
        return _endTime - _startTime;
    }

    private class MatchAction extends RecursiveAction
    {

        protected MatchAction(int start, int off) {
            _start = start;
            _off = off;
        }

        @Override
        protected void compute() {
            if (_off - _start < _workThreshold) {
                for (int i = _start; i < _off; i++) {
                    Record rec = _records[i];
                    String key = rec.blockingKey();
                    if (!_blocks.containsKey(key)) {
                        continue;
                    } else {
                        for (Record otherRec: _blocks.get(key)) {
                            double score = _model.matchScore(rec, otherRec);
                            _queue.add(new MatchRecord(rec, otherRec, score));
                        }
                    }
                }
            } else {
                int n = (_off - _start) / 2;
                MatchAction task1 = new MatchAction(_start, _start + n);
                MatchAction task2 = new MatchAction(_start + n, _off);

                invokeAll(task1, task2);
            }
        }

        private final int _start, _off;
    }

    private final int _workThreshold;
    private final IModel _model;
    private final ConcurrentLinkedQueue<MatchRecord> _queue;
    private Map<String, List<Record>> _blocks;
    private Record[] _records;
    private long _startTime, _endTime;
    private int _nComparisons;
}
