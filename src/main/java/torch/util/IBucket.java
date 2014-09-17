package torch.util;

public interface IBucket<SingleValue, AggregateValue> {
    public AggregateValue create(SingleValue init);
    public void accumulate(AggregateValue agg, SingleValue val);
}
