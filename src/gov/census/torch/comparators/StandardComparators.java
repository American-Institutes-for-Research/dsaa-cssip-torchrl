package gov.census.torch.comparators;

import gov.census.torch.IFieldComparator;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 9:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class StandardComparators {

    public final static IFieldComparator EXACT = ExactComparator.INSTANCE;

    public final static double[] PRO_SLOPE = {0.1, 0.2, 0.4};
    public final static double[] PRO_INTERCEPT = {1.1, 1.0, 1.0};
    public final static IFieldComparator PRORATED = new ProratedComparator(PRO_SLOPE, PRO_INTERCEPT);

    public final static IFieldComparator YEAR =
            new YearComparator(Calendar.getInstance().get(Calendar.YEAR), PRO_SLOPE, PRO_INTERCEPT);

    public final static double[] STR_LEVELS = {0.92, 0.86, 0.81};
    public final static IFieldComparator STRING = new StringComparator(STR_LEVELS);

}
