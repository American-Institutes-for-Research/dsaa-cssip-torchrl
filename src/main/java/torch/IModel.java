package torch;

/**
 * An object representing a record linkage model. A (Fellegi-Sunter) record
 * linkage model assigns a match score to pairs of records which can be used to
 * distinguish matches from nonmatches.
 */
public interface IModel {
    /**
     * Computes the match score for the given pair of records.
     */
    public double matchScore(Record rec1, Record rec2);

    /**
     * Returns the underlying record comparator. The interface assumes that the
     * match score is actually a function of the comparison pattern returned by
     * this record comparator.
     */
    public RecordComparator recordComparator();
}
