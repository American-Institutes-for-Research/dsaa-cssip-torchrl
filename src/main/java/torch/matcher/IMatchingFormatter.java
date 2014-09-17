package torch.matcher;

import torch.Record;

public interface IMatchingFormatter {
    public void format(Record rec1, Record rec2, double score)
        throws torch.FormatterException;
}
