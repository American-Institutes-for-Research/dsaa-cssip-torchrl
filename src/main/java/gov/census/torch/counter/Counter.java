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

        TreeMap<Integer, Integer> countMap = new TreeMap<>();
        HashMap<String, List<Record>> blocks = Record.block(list1);

        // Compare records in list2 to records in list1

        for (Record rec: list2) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                List<Record> thisBlock = blocks.get(key);
                for (Record otherRec: thisBlock) {
                    Integer pattern = cmp.compareIndex(rec, otherRec);
                    if (countMap.containsKey(pattern)) {
                        countMap.put(pattern, countMap.get(pattern) + 1);
                    } else {
                        countMap.put(pattern, 1);
                    }
                }
            }
        }

        return new Tally(cmp, countMap);
    }
}
