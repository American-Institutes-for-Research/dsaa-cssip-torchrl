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
import java.util.TreeMap;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

public class Matcher {

    public static Matcher match(IModel model, List<Record> list1, List<Record> list2) 
    {
        DefaultMatchingAlgo algo = new DefaultMatchingAlgo(model);
        TreeMap<Double, List<MatchRecord>> map = algo.computeScores(list1, list2);
        printMatchingAlgoFinished(algo);

        return new Matcher(model, map);
    }

    public static Matcher pmatch(IModel model, int workThreshold, List<Record> list1, List<Record> list2)
    {
        ParallelMatchingAlgo algo = new ParallelMatchingAlgo(model, workThreshold);
        TreeMap<Double, List<MatchRecord>> map = algo.computeScores(list1, list2);
        printMatchingAlgoFinished(algo);

        return new Matcher(model, map);
    }

    public Matcher(IModel model, TreeMap<Double, List<MatchRecord>> map) {
        _model = model;
        _map = map;
        _scores = _map.keySet().toArray(new Double[0]);
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

    public void printTails(double lo, double hi)
        throws IOException
    {
        printTails(new OutputStreamWriter(System.out), lo, hi);
    }

    protected static void printMatchingAlgoFinished(IMatchingAlgorithm algo) {
        int nComparisons = algo.nComparisons();
        long elapsedTime = algo.elapsedTime();
        double d = -1.0;
        String unit = "milliseconds";

        if (elapsedTime > 3600000) {
            d = elapsedTime / 3600000.0; 
            unit = "hours";
        } else if (elapsedTime > 60000) {
            d = elapsedTime / 60000.0;
            unit = "minutes";
        } else if (elapsedTime > 1000) {
            d = elapsedTime / 1000.0;
            unit = "seconds";
        }

        StringBuilder b = new StringBuilder();
        b.append(String.format("Performed %,d comparisons in", nComparisons));
        b.append(d > 0 ? String.format(" %.2f ", d) : 
                         String.format(" %d ", nComparisons));
        b.append(unit).append("\n");

        System.out.println(b.toString());
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
