package gov.census.torch.examples;

import gov.census.torch.Record;
import gov.census.torch.RecordComparator;
import gov.census.torch.comparators.StandardComparators;
import gov.census.torch.counter.Counter;
import gov.census.torch.io.Files;
import gov.census.torch.io.FixedWidthFileSchema;

import java.io.IOException;
import java.util.*;

public class CounterApp {

    public static void main(String[] args) {
        String filea = "example/testdeck/filea.dat";
        String fileb = "example/testdeck/fileb.dat";

        FixedWidthFileSchema schemaa = 
            new FixedWidthFileSchema.Builder()
            .blockingField(200, 201)
            .blockingField(96, 101)
            .field("first", 174, 187)
            .field("last", 200, 214)
            .field("hsn", 101, 107)
            .build();

        FixedWidthFileSchema schemab =
            new FixedWidthFileSchema.Builder()
            .blockingField(210, 211)
            .blockingField(106, 111)
            .field("first", 184, 197)
            .field("last", 210, 224)
            .field("hsn", 111, 117)
            .build();

        LinkedList<Record> lista = null;
        LinkedList<Record> listb = null;
        
        try {
            lista = Files.loadFixedWidthFile(filea, schemaa);
            listb = Files.loadFixedWidthFile(fileb, schemab);
        }
        catch (IOException ioe) {
            System.out.println("There was a problem reading an input file.");
            System.exit(1);
        }

        System.out.println("Read " + lista.size() + " records from " + filea);
        System.out.println("Read " + listb.size() + " records from " + fileb);

        RecordComparator cmp = 
            new RecordComparator.Builder(schemaa, schemab)
            .comparator("first", StandardComparators.STRING)
            .comparator("last", StandardComparators.STRING)
            .comparator("hsn", StandardComparators.EXACT)
            .build();

        Counter counter = new Counter(cmp);
        counter.countPatterns(lista, listb);

        int[] c = counter.counts();
        for (int i = 0; i < cmp.nPatterns(); i++) {
            if (c[i] > 0)
                System.out.println(i + "\t" + c[i]);
        }
    }
}
