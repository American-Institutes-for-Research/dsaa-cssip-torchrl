package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

public class Matcher {

    public Matcher(IModel model, List<Record> list1, List<Record> list2) {
        _model = model;
        _map = Matcher.computeScores(model, list1, list2);
        _scores = _map.keySet().toArray(new Double[0]);
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
                    double score = model.matchScore(rec, otherRec);
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

    /**
     * Write matches in CSV format.
     */
    public void printMatches(Writer writer, double cutoff)
        throws IOException
    {
        // Create a list of matches from highest score to lowest
        int leftIndex = Arrays.binarySearch(_scores, cutoff);
        if (leftIndex == _scores.length)
            return;
        else if (leftIndex < 0)
            leftIndex = -(leftIndex + 1);

        LinkedList<MatchRecord> list = new LinkedList<>();
        for (int i = _scores.length - 1; i >= leftIndex; i--)
            list.addAll(_map.get(_scores[i]));

        CSVWriter<MatchRecord> csvWriter =
            new CSVWriterBuilder<MatchRecord>(writer)
            .entryConverter(new MatchRecordEntryConverter(_model.recordComparator()))
            .strategy(CSVStrategy.UK_DEFAULT)
            .build();

        csvWriter.writeAll(list);
        writer.flush();
    }

    /**
     * Write matches in CSV format to the file with the given name.
     */
    public void printMatches(String name, double cutoff) 
        throws IOException
    {
        printMatches(new FileWriter(name), cutoff);
    }

    /**
     * Write matches in CSV format to STDOUT
     */
    public void printMatches(double cutoff) 
        throws IOException
    {
        printMatches(new OutputStreamWriter(System.out), cutoff);
    }

    /**
     * Print pairs with match scores immediately above and below the given point.
     */
    public void browse(double score) 
        throws IOException
    {
        LinkedList<MatchRecord> list = new LinkedList<>();

        Double higher = _map.higherKey(score);
        if (higher != null)
            list.addAll(_map.get(higher));

        if (_map.containsKey(score))
            list.addAll(_map.get(score));
        
        Double lower = _map.lowerKey(score);
        if (lower != null)
            list.addAll(_map.get(lower));

        printMatchRecords(list);
    }

    protected void printMatchRecords(List<MatchRecord> list) 
        throws IOException
    {
        CSVWriter<MatchRecord> csvWriter =
            new CSVWriterBuilder<MatchRecord>(new OutputStreamWriter(System.out))
            .entryConverter(new MatchRecordEntryConverter(_model.recordComparator()))
            .strategy(CSVStrategy.UK_DEFAULT)
            .build();

        csvWriter.writeAll(list);
        System.out.flush();
    }

    private final IModel _model;
    private final TreeMap<Double, List<MatchRecord>> _map;
    private final Double[] _scores;
}
