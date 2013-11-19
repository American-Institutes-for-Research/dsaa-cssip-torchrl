package gov.census.torch.counter;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * An object that counts occurences of record comparison patterns.
 */
public class Counter {

    /**
     * Count the comparison patterns for blocked pairs in the two lists.
     */
    public static Tally tally(RecordComparator cmp, List<Record> list1, List<Record> list2) 
    {

        IncrementalTally inc = new IncrementalTally(cmp);
        HashMap<String, List<Record>> blocks = Record.block(list1);

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                List<Record> thisBlock = blocks.get(key);
                for (Record otherRec: thisBlock)
                    inc.add(rec, otherRec);
            }
        }

        return inc.tally();
    }

    /**
     * Counts truth patterns. This method uses blocking to bring together pairs, and so may not
     * count every true match.
     *
     * @return a length 2 Tally array, in which the first element represents the observed patterns
     * among match pairs, and the second element represents observed patterns amont nonmatch pairs.
     */
    public Tally[] tallyTruth(RecordComparator cmp, List<Record> list1, List<Record> list2) {
        IncrementalTally trueMatch = new IncrementalTally(cmp);
        IncrementalTally trueNonmatch = new IncrementalTally(cmp);

        if (list1.size() > 0 && list2.size() > 0) {
            if (!(list1.get(0).schema().hasId() && list2.get(0).schema().hasId()))
                throw new IllegalArgumentException("Records must have ID fields");
        }

        HashMap<String, List<Record>> blocks = Record.block(list1);

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                List<Record> thisBlock = blocks.get(key);
                for (Record otherRec: thisBlock) {
                    if (rec.id().equals(otherRec.id()))
                        trueMatch.add(rec, otherRec);
                    else
                        trueNonmatch.add(rec, otherRec);
                }
            }
        }

        return new Tally[] {trueMatch.tally(), trueNonmatch.tally()};
    }
}
