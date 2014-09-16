package torch.util;

public interface IBucket<SingleValue, AggregateValue> {
    public AggregateValue create(SingleValue init);
    public AggregateValue accumulate(AggregateValue agg, SingleValue val);
}
