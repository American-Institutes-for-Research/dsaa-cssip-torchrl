package gov.census.torch.util;

public class IntegerAccumulator
    implements IAccumulate<Integer, Integer>
{

    public final static IntegerAccumulator INSTANCE = new IntegerAccumulator();

    @Override
    public Integer newAggregateValue(Integer i) {
        return i;
    }

    @Override
    public Integer accumulate(Integer i, Integer j) {
        return i + j;
    }
}
