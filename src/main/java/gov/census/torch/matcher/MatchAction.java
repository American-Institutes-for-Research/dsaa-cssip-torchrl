package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;

import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

public class MatchAction extends RecursiveAction
{
    public final static int WORK_THRESHOLD = 1000;

    public MatchAction(IModel model, List<Record> list1, List<Record> list2) 
    {
        this(model, Record.block(list1), list2);
    }

    public MatchAction(IModel model, Map<String, List<Record>> blocks, List<Record> list) 
    {
        _model = model;
        _blocks = blocks;
        _list = list;
        _result = null;
    }

    public TreeMap<Double, List<MatchRecord>> result() {
        return _result;
    }

    @Override
    protected void compute() {
        if (_list.size() < WORK_THRESHOLD) {
            _result = Matcher.computeScores(_model, _blocks, _list);
        } else {
            int n = _list.size() / 2;
            MatchAction task1 = new MatchAction(_model, _blocks, _list.subList(0, n));
            MatchAction task2 = new MatchAction(_model, _blocks, _list.subList(n, _list.size()));

            invokeAll(task1, task2);

            _result = task1.result();
            for (Map.Entry<Double, List<MatchRecord>> entry: task2.result().entrySet()) 
            {
                Double d = entry.getKey();
                List<MatchRecord> thisList = entry.getValue();

                if (_result.containsKey(d))
                    _result.get(d).addAll(thisList);
                else
                    _result.put(d, thisList);
            }
        }
    }

    private final IModel _model;
    private final Map<String, List<Record>> _blocks;
    private final List<Record> _list;
    private TreeMap<Double, List<MatchRecord>> _result;
}
