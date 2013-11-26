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

/**
 * A class respresenting the output of a matching algorithm. Use this class to browse results by
 * viewing records pairs with scores above or below given thresholds.
 */
public class Matcher {

    /**
     * Performs matching on the given lists using a single-threaded algorithm.
     */
    public static Matcher match(IModel model, List<Record> list1, List<Record> list2) 
    {
        DefaultMatchingAlgo algo = new DefaultMatchingAlgo(model);
        TreeMap<Double, List<MatchRecord>> map = algo.computeScores(list1, list2);
        printMatchingAlgoFinished(algo);

        return new Matcher(model, map);
    }

    /**
     * Performs matching on the given lists using a multi-threaded parallel algorithm. Individual
     * threads will work on chunks of <code>list2</code>.
     *
     * @param workThreshold The maximum number of records (in <code>list2</code>) that are given to
     * a single thread.
     */
    public static Matcher pmatch(IModel model, int workThreshold, List<Record> list1, List<Record> list2)
    {
        ParallelMatchingAlgo algo = new ParallelMatchingAlgo(model, workThreshold);
        TreeMap<Double, List<MatchRecord>> map = algo.computeScores(list1, list2);
        printMatchingAlgoFinished(algo);

        return new Matcher(model, map);
    }

    /**
     * Constructs a new <code>Matcher</code>.
     *
     * @param map A map from match-scores to the list of record pairs with that score.
     */
    public Matcher(IModel model, TreeMap<Double, List<MatchRecord>> map) {
        _model = model;
        _map = map;
        _scores = _map.keySet().toArray(new Double[0]);
    }

    /**
     * Writes pairs of records with a match score between the given values.
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

    /**
     * Writes pairs of records with a match score between the given values to a file.
     *
     * @param name The name of the file to write to
     */
    public void printPairs(String name, double lo, double hi)
        throws IOException
    {
        printPairs(new FileWriter(name), lo, hi);
    }

    /**
     * Writes pairs of records with a match score between the given values to stdout.
     */
    public void printPairs(double lo, double hi)
        throws IOException
    {
        printPairs(new OutputStreamWriter(System.out), lo, hi);
    }

    /**
     * Writes pairs of records with a match score above the given threshold.
     */
    public void printMatches(Writer writer, double cutoff)
        throws IOException
    {
        printPairs(writer, cutoff, Double.POSITIVE_INFINITY);
    }

    /**
     * Writes pairs of records with a match score above the given threshold to a file.
     *
     * @param name The name of the file to write to
     */
    public void printMatches(String name, double cutoff) 
        throws IOException
    {
        printMatches(new FileWriter(name), cutoff);
    }

    /**
     * Writes paris of records with a match score above the given threshold to stdout.
     */
    public void printMatches(double cutoff) 
        throws IOException
    {
        printMatches(new OutputStreamWriter(System.out), cutoff);
    }

    /**
     * Writes pairs of records with a match score below the low value or above the high value.
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

    /**
     * Writes pairs of records with a match score below the low value or above the high value to a
     * file.
     *
     * @param name The name of the file to write to
     */
    public void printTails(String name, double lo, double hi)
        throws IOException
    {
        printTails(new FileWriter(name), lo, hi);
    }

    /**
     * Writes pairs of records with a match score below the low value or above the high value to
     * stdout.
     */
    public void printTails(double lo, double hi)
        throws IOException
    {
        printTails(new OutputStreamWriter(System.out), lo, hi);
    }

    /**
     * Writes the number of records compared by <code>algo</code> and the elapsed time to stdout.
     */
    protected static void printMatchingAlgoFinished(IMatchingAlgorithm algo) {
        int nComparisons = algo.nComparisons();
        long elapsedTime = algo.elapsedTime();
        double dComparisons = -1.0;
        String unit = "milliseconds";

        if (elapsedTime > 3600000) {
            dComparisons = elapsedTime / 3600000.0; 
            unit = "hours";
        } else if (elapsedTime > 60000) {
            dComparisons = elapsedTime / 60000.0;
            unit = "minutes";
        } else if (elapsedTime > 1000) {
            dComparisons = elapsedTime / 1000.0;
            unit = "seconds";
        }

        StringBuilder b = new StringBuilder();
        b.append(String.format("Performed %,d comparisons in", nComparisons));
        b.append(dComparisons > 0 ? String.format(" %.2f ", dComparisons) : 
                                    String.format(" %d ", nComparisons));
        b.append(unit).append("\n");

        System.out.println(b.toString());
    }

    /**
     * Writes a list of records in CSV format.
     */
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
