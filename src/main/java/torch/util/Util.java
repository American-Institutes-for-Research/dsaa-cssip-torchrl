package torch.util;

import java.util.Random;

public class Util {
    
    public static int sampleMultinomial(Random rng, double[] p) {
        int i = 0;

        for (double u = rng.nextDouble(); u > 0 && (i + 1) < p.length; u -= p[i++])
            ;

        return i;
    }
}
