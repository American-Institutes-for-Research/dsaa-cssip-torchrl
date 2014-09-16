package torch.util;

public class IntBucket
    implements IBucket<Integer, Integer>
{

    public final static IntBucket INSTANCE = new IntBucket();

    @Override
    public Integer create(Integer i) {
        return i;
    }

    @Override
    public Integer accumulate(Integer i, Integer j) {
        return i + j;
    }
}
