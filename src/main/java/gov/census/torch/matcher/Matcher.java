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
     * Write pairs of records with a match score between the given values.
     */
    public void printPairs(Writer writer, double lo, double hi) 
        throws IOException
    {
        // Create a list of matches from highest score to lowest
        int loIndex = Arrays.binarySearch(_scores, lo);
        if (loIndex == _scores.length)
            return;
        else if (loIndex < 0)
            loIndex = -(loIndex + 1);

        int hiIndex = Arrays.binarySearch(_scores, hi);
        if (hiIndex == 0)
            return;
        else if (hiIndex < 0)
            hiIndex = -(hiIndex + 1);

        LinkedList<MatchRecord> list = new LinkedList<>();
        for (int i = hiIndex - 1; i >= loIndex; i--)
            list.addAll(_map.get(_scores[i]));

        printMatchRecords(writer, list);
    }

    public void printPairs(String name, double lo, double hi)
        throws IOException
    {
        printPairs(new FileWriter(name), lo, hi);
    }

    public void printPairs(double lo, double hi)
        throws IOException
    {
        printPairs(new OutputStreamWriter(System.out), lo, hi);
    }

    /**
     * Write matches in CSV format.
     */
    public void printMatches(Writer writer, double cutoff)
        throws IOException
    {
        printPairs(writer, cutoff, Double.MAX_VALUE);
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
     * Write pairs of records with a match score below the low value or above the high value.
     */
    public void printTails(Writer writer, double lo, double hi) 
        throws IOException
    {
        // Create a list of matches from highest score to lowest
        int loIndex = Arrays.binarySearch(_scores, lo);
        if (loIndex < 0)
            loIndex = -(loIndex + 1);

        int hiIndex = Arrays.binarySearch(_scores, hi);
        if (hiIndex < 0)
            hiIndex = -(hiIndex + 1);

        if (loIndex == 0 && hiIndex == _scores.length)
            return;

        LinkedList<MatchRecord> list = new LinkedList<>();
        for (int i = 0; i < loIndex; i++)
            list.addAll(_map.get(_scores[i]));
        for (int i = hiIndex; i < _scores.length; i++)
            list.addAll(_map.get(_scores[i]));

        printMatchRecords(writer, list);
    }

    public void printTails(String name, double lo, double hi)
        throws IOException
    {
        printTails(new FileWriter(name), lo, hi);
    }

    public void printtails(double lo, double hi)
        throws IOException
    {
        printTails(new OutputStreamWriter(System.out), lo, hi);
    }

    protected void printMatchRecords(Writer writer, List<MatchRecord> list) 
        throws IOException
    {
        CSVWriter<MatchRecord> csvWriter =
            new CSVWriterBuilder<MatchRecord>(writer)
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
