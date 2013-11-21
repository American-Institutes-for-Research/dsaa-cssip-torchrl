package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class PMatcher {

    public final static int N_THREADS = 4;
    
    public PMatcher(IModel model, List<Record> list1, List<Record> list2) 
        throws InterruptedException, ExecutionException
    {
        _model = model;
        _map = PMatcher.computeScores(model, list1, list2);
        _scores = _map.keySet().toArray(new Double[0]);
    }

    protected static TreeMap<Double, List<MatchRecord>>
        computeScores(IModel model, List<Record> list1, List<Record> list2)
        throws InterruptedException, ExecutionException
    {
        TreeMap<Double, List<MatchRecord>> map = new TreeMap<>();
        HashMap<String, List<Record>> blocks = Record.block(list1);

        List<LinkedList<Record>> records = new java.util.ArrayList<>();
        for (int i = 0; i < N_THREADS; i++)
            records.set(i, new LinkedList<Record>());

        int i = 0;
        for (Record rec: list2) {
            records.get(i).add(rec);
            i = (i + 1) % N_THREADS;
        }

        List<MatchTask> tasks = new LinkedList<>();
        for (LinkedList<Record> ll: records)
            tasks.add(new MatchTask(model, blocks, ll));

        ExecutorService exec = Executors.newFixedThreadPool(N_THREADS);
        List<Future<TreeMap<Double, List<MatchRecord>>>> result = exec.invokeAll(tasks);

        for (Future<TreeMap<Double, List<MatchRecord>>> future: result) {
            TreeMap<Double, List<MatchRecord>> thisMap = future.get();
        }

        return map;
    }

    static class MatchTask 
            implements Callable<TreeMap<Double, List<MatchRecord>>>
    {
        public MatchTask(IModel model,
                         HashMap<String, List<Record>> blocks,
                         List<Record> list) 
        {
            _model = model;
            _blocks = blocks;
            _list = list;
        }

        public TreeMap<Double, List<MatchRecord>> call() {
            TreeMap<Double, List<MatchRecord>> map = new TreeMap<>();

            for (Record rec: _list) {
                String key = rec.blockingKey();

                if (!_blocks.containsKey(key)) {
                    continue;
                } else {
                    for (Record otherRec: _blocks.get(key)) {
                        double score = _model.matchScore(rec, otherRec);
                        MatchRecord matchRec = new MatchRecord(rec, otherRec, score);

                        if (map.containsKey(score)) {
                            map.get(score).add(matchRec);
                        } else {
                            LinkedList<MatchRecord> ll = new LinkedList<>();
                            ll.add(matchRec);
                            map.put(score, ll);
                        }
                    }
                }
            }

            return map;
        }
        
        private final IModel _model;
        private final HashMap<String, List<Record>> _blocks;
        private final List<Record> _list;
    }

    private final IModel _model;
    private final TreeMap<Double, List<MatchRecord>> _map;
    private final Double[] _scores;
}
