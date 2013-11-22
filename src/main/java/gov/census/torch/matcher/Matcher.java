package gov.census.torch.matcher;

import gov.census.torch.IModel;
import gov.census.torch.Record;
import gov.census.torch.util.BucketMap;
import gov.census.torch.util.ListBucket;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

public class Matcher {

    public static Matcher match(IModel model, List<Record> list1, List<Record> list2) 
    {
        TreeMap<Double, List<MatchRecord>> map = 
            Matcher.computeScores(model, list1, list2);

        return new Matcher(model, map);
    }

    public static Matcher pmatch(int workThreshold, IModel model, List<Record> list1, List<Record> list2)
    {
        PMatcher pm = new PMatcher(workThreshold, model, list1, list2);
        return new Matcher(model, pm.scores());
    }

    public Matcher(IModel model, TreeMap<Double, List<MatchRecord>> map) {
        _model = model;
        _map = map;
        _scores = _map.keySet().toArray(new Double[0]);
    }

    protected static TreeMap<Double, List<MatchRecord>>
        computeScores(IModel model, List<Record> list1, List<Record> list2) 
    {
        Map<String, List<Record>> blocks = Record.block(list1);
        return computeScores(model, blocks, list2);
    }

    protected static TreeMap<Double, List<MatchRecord>> 
        computeScores(IModel model, Map<String, List<Record>> blocks, List<Record> list) 
    {
        BucketMap<Double, MatchRecord, List<MatchRecord>> bmap =
            new BucketMap<>(new TreeMap<Double, List<MatchRecord>>(),
                            new ListBucket<MatchRecord>());

        for (Record rec: list) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                for (Record otherRec: blocks.get(key)) {
                    double score = model.matchScore(rec, otherRec);
                    MatchRecord matchRec = new MatchRecord(rec, otherRec, score);
                    bmap.add(score, matchRec);
                }
            }
        }

        return (TreeMap<Double, List<MatchRecord>>)bmap.map();
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
