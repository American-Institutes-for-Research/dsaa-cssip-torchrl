package torch;

import java.util.Iterator;

/**
 * A class that wraps <code>Iterable</code> with the {@link IRecordIterator} interface.
 */
public class RecordIterator
    implements IRecordIterator
{
    public RecordIterator(Iterable<Record> it) {
        _it = it.iterator();
    }

    public Record next() 
        throws RecordIteratorException
    {
        if (_it.hasNext()) {
            return _it.next();
        }

        return null;
    }

    private final Iterator<Record> _it;
}
