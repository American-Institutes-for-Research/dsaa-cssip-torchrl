package gov.census.torch.util;

public interface IAccumulate<SingleValue, AggregateValue> {
    public AggregateValue newAggregateValue(SingleValue init);
    public AggregateValue accumulate(AggregateValue agg, SingleValue val);
}
