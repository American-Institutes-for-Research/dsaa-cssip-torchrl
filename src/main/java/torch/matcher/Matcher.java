package torch.matcher;

import torch.IModel;
import torch.Record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

/**
 * A class respresenting the output of a matching algorithm. Use this class to browse results by
 * viewing records pairs with scores above or below given thresholds.
 */
public class Matcher {

    /**
     * Performs matching on the given lists using a single-threaded algorithm.
     */
    public static void match(String filename, IModel model, 
                             Iterable<Record> list1, Iterable<Record> list2,
                             double cutoff) 
        throws java.io.IOException, torch.FormatterException
    {
        FileWriter writer = new FileWriter(filename);
        DefaultFormatter formatter = 
            new DefaultFormatter(writer, model.recordComparator(), cutoff);

        DefaultMatchingAlgo algo = new DefaultMatchingAlgo(model, formatter);
        algo.computeScores(list1, list2);
        printMatchingAlgoFinished(algo);
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
}
