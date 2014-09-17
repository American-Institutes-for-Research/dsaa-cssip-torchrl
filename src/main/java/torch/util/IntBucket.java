package torch.util;

public class IntBucket
    implements IBucket<Integer, P<Integer>>
{

    public final static IntBucket INSTANCE = new IntBucket();

    @Override
    public P<Integer> create(Integer i) {
        P<Integer> ptr = new P<>();
        ptr.value = i;
        return ptr;
    }

    @Override
    public void accumulate(P<Integer> ptr, Integer j) {
        ptr.value += j;
    }
}
