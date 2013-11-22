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

public class PMatcher {

    public PMatcher(int workThreshold, IModel model, List<Record> list1, List<Record> list2) 
    {
        _workThreshold = workThreshold;
        _model = model;
        _blocks = Record.block(list1);
        _records = list2.toArray(new Record[0]);
        _acc = new ConcurrentLinkedQueue<MatchRecord>();
    }

    public TreeMap<Double, List<MatchRecord>> scores() {
        BucketMap<Double, MatchRecord, List<MatchRecord>> bmap =
            new BucketMap<>(new TreeMap<Double, List<MatchRecord>>(),
                            new ListBucket<MatchRecord>());
        ForkJoinPool pool = new ForkJoinPool();
        MatchAction action = new MatchAction(0, _records.length);
        pool.execute(action);

        MatchRecord matchRec;
        while (!action.isDone()) {
            if ((matchRec = _acc.poll()) != null) {
                bmap.add(matchRec.score(), matchRec);
            }
        }

        return (TreeMap<Double, List<MatchRecord>>)bmap.map();
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
                            _acc.add(new MatchRecord(rec, otherRec, score));
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
    private final Map<String, List<Record>> _blocks;
    private final Record[] _records;
    private final ConcurrentLinkedQueue<MatchRecord> _acc;
}
