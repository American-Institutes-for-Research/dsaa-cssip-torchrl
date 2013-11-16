package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;

public class Matcher {

    public Matcher(IModel model, List<Record> list1, List<Record> list2) {
        _model = model;
        _map = Matcher.computeScores(model, list1, list2);
    }

    protected static TreeMap<Double, List<MatchRecord>>
        computeScores(IModel model, List<Record> list1, List<Record> list2) 
    {
        TreeMap<Double, List<MatchRecord>> map = new TreeMap<>();
        HashMap<String, List<Record>> blocks = Record.block(list1);

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                for (Record otherRec: blocks.get(key)) {
                    Double score = model.matchScore(rec, otherRec);
                    MatchRecord matchRec = new MatchRecord(rec, otherRec, score);

                    if (map.containsKey(score)) {
                        map.get(score).add(matchRec);
                    } else {
                        LinkedList<MatchRecord> list = new LinkedList<>();
                        list.add(matchRec);
                        map.put(score, list);
                    }
                }
            }
        }

        return map;
    }

    private final IModel _model;
    private final TreeMap<Double, List<MatchRecord>> _map;
}
