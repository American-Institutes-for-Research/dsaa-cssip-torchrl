package gov.census.torch.test;

import gov.census.torch.RecordComparator;
import gov.census.torch.comparators.StandardComparators;
import gov.census.torch.io.FixedWidthFileSchema;
import gov.census.torch.model.ConditionalIndependenceModel;

import java.util.Arrays;
import java.util.Random;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ConditionalIndependenceModelTest {

    private FixedWidthFileSchema schema1, schema2;
    private RecordComparator cmp;
    private ConditionalIndependenceModel model;

    @Before
    public void setUp() {
        schema1 = 
            new FixedWidthFileSchema.Builder()
            .blockingField(1, 5)
            .field("first", 5, 15)
            .field("last", 15, 25)
            .field("age", 25, 25)
            .build();

        schema2 = 
            new FixedWidthFileSchema.Builder()
            .blockingField(1, 5)
            .field("first", 5, 15)
            .field("last", 15, 25)
            .field("age", 25, 25)
            .build();

        cmp =
            new RecordComparator.Builder(schema1, schema2, false)
            .comparator("last", StandardComparators.STRING)
            .comparator("age", StandardComparators.YEAR)
            .comparator("first", StandardComparators.STRING)
            .build();

        // model = new ConditionalIndependenceModel(cmp, 3);
    }

    @Test
    public void testPartitionOne() {
        Random rng = new Random();
        double[] ary = new double[10];
        ConditionalIndependenceModel.partitionOne(rng, ary);

        double total = 0.0;
        for (int i = 0; i < ary.length; i++) {
            assertThat(ary[i] >= 0.0, is(true));
            assertThat(ary[i] <= 1.0, is(true));
            total += ary[i];
        }

        assertThat(total, is(1.0));
    }
}
