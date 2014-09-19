package torch.util;

public class IntAccumulator
    implements IAccumulate<Integer, P<Integer>>
{

    public final static IntAccumulator INSTANCE = new IntAccumulator();

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
