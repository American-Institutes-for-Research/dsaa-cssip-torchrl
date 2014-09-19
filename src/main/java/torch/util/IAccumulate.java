package torch.util;

public interface IAccumulate<SingleValue, AggregateValue> {
    public AggregateValue create(SingleValue init);
    public void accumulate(AggregateValue agg, SingleValue val);
}
