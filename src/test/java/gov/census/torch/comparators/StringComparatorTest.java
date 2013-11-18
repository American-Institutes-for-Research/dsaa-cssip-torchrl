package gov.census.torch.comparators;

import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class StringComparatorTest {

    private String s1, s2;
    private boolean[] flag1, flag2;
    private int maxLen, minLen;

    private void setStrings(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
        flag1 = new boolean[s1.length()];
        flag2 = new boolean[s2.length()];

        if (s1.length() < s2.length()) {
            minLen = s1.length();
            maxLen = s2.length();
        } else {
            minLen = s2.length();
            maxLen = s1.length();
        }
    }

    @Test
    public void testCountCommon() {
        setStrings("shackleford", "shackelford");
        int n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(11));
        assertThat(flag1, is(new boolean[] {true, true, true, true, true, true, true, true, true, true, true}));
        assertThat(flag2, is(new boolean[] {true, true, true, true, true, true, true, true, true, true, true}));

        setStrings("dunningham", "cunningham");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(9));
        assertThat(flag1, is(new boolean[] {false, true, true, true, true, true, true, true, true, true}));
        assertThat(flag2, is(new boolean[] {false, true, true, true, true, true, true, true, true, true}));

        setStrings("nichleson", "nichulson");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(8));
        assertThat(flag1, is(new boolean[] {true, true, true, true, true, false, true, true, true}));
        assertThat(flag2, is(new boolean[] {true, true, true, true, false, true, true, true, true}));

        setStrings("jones", "johnson");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(4));
        assertThat(flag1, is(new boolean[] {true, true, true, false, true}));
        assertThat(flag2, is(new boolean[] {true, true, false, true, true, false, false})); 

        setStrings("massey", "massie");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(5));
        assertThat(flag1, is(new boolean[] {true, true, true, true, true, false}));
        assertThat(flag2, is(new boolean[] {true, true, true, true, false, true}));

        setStrings("abroms", "abrams");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(5));
        assertThat(flag1, is(new boolean[] {true, true, true, false, true, true}));
        assertThat(flag2, is(new boolean[] {true, true, true, false, true, true}));

        setStrings("hardin", "martinez");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(4));
        assertThat(flag1, is(new boolean[] {false, true, true, false, true, true}));
        assertThat(flag2, is(new boolean[] {false, true, true, false, true, true, false, false}));

        setStrings("itman", "smith");
        n = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        assertThat(n, is(1));
        assertThat(flag1, is(new boolean[] {false, false, true, false, false}));
        assertThat(flag2, is(new boolean[] {false, true, false, false, false}));
    }

    @Test
    public void testCountTranspositions() {
        setStrings("shackleford", "shackelford");
        StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        int n = StringComparator.countTranspositions(s1, s2, flag1, flag2);
        assertThat(n, is(1));

        setStrings("dunningham", "cunningham");
        StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        n = StringComparator.countTranspositions(s1, s2, flag1, flag2);
        assertThat(n, is(0));

        setStrings("nichleson", "nichulson");
        StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        n = StringComparator.countTranspositions(s1, s2, flag1, flag2);
        assertThat(n, is(0));

        setStrings("bertoin", "ebert");
        StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        n = StringComparator.countTranspositions(s1, s2, flag1, flag2);
        assertThat(n, is(1));
    }

    @Test
    public void testCountSimilar() {
        int nCommon, n;

        setStrings("JONES", "J0HNSON");
        nCommon = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        n = StringComparator.countSimilar(s1, s2, flag1, flag2);
        assertThat(n, is(2));

        setStrings("MASSEY", "MOSSIE");
        nCommon = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        n = StringComparator.countSimilar(s1, s2, flag1, flag2);
        assertThat(n, is(2));

        setStrings("EBROM5", "ABRAMS");
        nCommon = StringComparator.countCommon(s1, s2, flag1, flag2, maxLen);
        n = StringComparator.countSimilar(s1, s2, flag1, flag2);
        assertThat(n, is(3));
    }
}
