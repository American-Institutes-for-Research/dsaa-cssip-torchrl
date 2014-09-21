package torch.matcher;

import torch.IModel;
import torch.IRecordIterator;
import torch.Record;

import java.util.List;
import java.util.Map;

/**
 * The standard scoring algoirthm.
 */
public class DefaultMatchingAlgo 
    implements IMatchingAlgorithm
{

    /**
     * Constructs a matching algorithm that will compare records using the given
     * <code>model</code> and output results to the formatter.
     */
    public DefaultMatchingAlgo(IModel model, IMatchingFormatter formatter) {
        _model = model;
        _formatter = formatter;
    }

    /**
     * Computes match scores for the two lists. First <code>list1</code> is blocked, then each
     * record in <code>list2</code> is compared to all records in the corresponding block.
     */
    @Override
    public void computeScores(IRecordIterator list1, IRecordIterator list2)
        throws torch.FormatterException, torch.RecordIteratorException
    {
        computeScores(Record.block(list1), list2);
    }

    public void computeScores(Map<String, List<Record>> blocks, IRecordIterator list)
        throws torch.FormatterException, torch.RecordIteratorException
    {
        _startTime = System.currentTimeMillis();
        _nComparisons = 0;

        Record rec;
        while ((rec = list.next()) != null) {
            String key = rec.blockingKey();

            if (!blocks.containsKey(key)) {
                continue;
            } else {
                for (Record otherRec: blocks.get(key)) {
                    double score = _model.matchScore(otherRec, rec);
                    _formatter.format(otherRec, rec, score);
                    _nComparisons++;
                }
            }
        }

        _endTime = System.currentTimeMillis();
    }

    /**
     * Returns the number of record comparisons that were performed.
     */
    @Override
    public int nComparisons() {
        return _nComparisons;
    }

    /**
     * Returns the elapsed time in milliseconds.
     */
    @Override
    public long elapsedTime() {
        return _endTime - _startTime;
    }

    private final IModel _model;
    private final IMatchingFormatter _formatter;
    private int _nComparisons;
    private long _startTime, _endTime;
}
