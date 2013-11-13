package gov.census.torch.comparators;

import gov.census.torch.Field;
import gov.census.torch.IFieldComparator;

public class StringComparator implements IFieldComparator {

    public final static int ADJ_SIMILAR_CHARACTER = 1;
    public final static int ADJ_WINKLER_PREFIX = 2;
    public final static int ADJ_LONG_STRINGS = 4;

    public final static char[][] simiChars = {
            {'A','E'}, {'A','I'}, {'A','O'},
            {'A','U'}, {'B','V'}, {'E','I'},
            {'E','O'}, {'E','U'}, {'I','O'},
            {'I','U'}, {'O','U'}, {'I','Y'},
            {'E','Y'}, {'C','G'}, {'E','F'},
            {'W','U'}, {'W','V'}, {'X','K'},
            {'S','Z'}, {'X','S'}, {'Q','C'},
            {'U','V'}, {'M','N'}, {'L','I'},
            {'Q','O'}, {'P','R'}, {'I','J'},
            {'2','Z'}, {'5','S'}, {'8','B'},
            {'1','I'}, {'1','L'}, {'0','O'},
            {'0','Q'}, {'C','K'}, {'G','J'},
            {'E',' '}, {'Y',' '}, {'S',' '}
    };

    // A table of weights used by the countSimi function.
    private static int[][] adjWeight = null;

    // Initialize adjWeight from the simiChars table.
    static {
        adjWeight = new int[91][91];

        for (char[] simiChar : simiChars) {
            adjWeight[simiChar[0]][simiChar[1]] = 3;
        }
    }

    /**
     * Count the number of characters in common between strings a and b. A
     * character is counted if the difference between its position in a and b
     * is no greater than max(len(a), len(b)) / 2 - 1. In addition to
     * computing the count, this function returns flag arrays the same length
     * as a and b. These arrays will have a value of 1 in the entries
     * corresponding to common characters, and a value of 0 otherwise.
     */
    private static int countCommon(String s1, String s2,
                                   boolean[] flag1, boolean[] flag2, int maxLen)
    {
        int searchRange = maxLen / 2 - 1;
        int nCommon = 0;

        int lo, hi;
        for (int i = 0; i < s1.length(); i++) {
            if (i > searchRange)
                lo = i - searchRange;
            else
                lo = 0;

            if (i + searchRange < s2.length())
                hi = i + searchRange;
            else
                hi = s2.length();

            for (int j = lo; j < hi; j++) {
                if (!flag2[j] && s1.charAt(i) == s2.charAt(j)) {
                    flag1[i] = true;
                    flag2[j] = true;
                    nCommon++;
                    break;
                }
            }

        }

        return nCommon;
    }

    /**
     * Count the number of transposed characters between s1 and s2.
     */
    private static int countTranspositions(String s1, String s2, boolean[] flag1, boolean[] flag2) {
        int nTrans2 = 0;
        int k = 0;

        for (int i = 0; i < s1.length(); i++) {
            if (flag1[i]) {
                int j;
                for (j = k; j < s2.length(); j++) {
                    if (flag2[j]) {
                        k = j + 1;
                        break;
                    }
                }

                if (s1.charAt(i) != s2.charAt(j))
                    nTrans2++;
            }
        }

        return nTrans2 / 2;
    }

    /**
     * The number of similar characters is the number of common characters plus
     * 0.3 for each pair of characters that appears in simiChars. A letter in
     * s1 can match multiple letters in s2.
     */
    private static int countSimilar(String s1, String s2, int minLen, int nCommon,
                                    boolean[] flag1, boolean[] flag2)
    {
        int nSimi = 0;
        if (minLen > nCommon) {
            for (int i = 0; i < s1.length(); i++) {
                if (!flag1[i]) {
                    for (int j = 0; j < s2.length(); j++) {
                        if (!flag2[j]) {
                            if (adjWeight[s1.charAt(i)][s2.charAt(j)] > 0) {
                                nSimi++;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // TODO change code below so that this can return nSimi
        return nSimi;
    }

    public StringComparator(double[] levels, int flags) {
        this.adjSimilarCharacter = (flags | ADJ_SIMILAR_CHARACTER) > 0;
        this.adjWinklerPrefix = (flags | ADJ_WINKLER_PREFIX) > 0;
        this.adjLongStrings = (flags | ADJ_LONG_STRINGS) > 0;
        this.levels = levels;
    }

    public StringComparator(double[] levels) {
        this(levels, 0);
    }

    /*
     * There are a lot of magic numbers in this function, try to replace these.
     */
    public double fuzzyMatch(String s1, String s2) {
        // strip whitespace and capitalize strings
        s1 = s1.trim().toUpperCase();
        s2 = s2.trim().toUpperCase();

        // identify the larger and smaller length
        int maxLen = s1.length();
        int minLen = s2.length();
        if (minLen > maxLen) {
            int temp = minLen;
            minLen = maxLen;
            maxLen = temp;
        }

        // if either string is blank, return
        if (minLen == 0)
            return 0.0;

        // count the number of characters in common and set flags arrays
        // indicating their positions.
        boolean[] flag1 = new boolean[s1.length()];
        boolean[] flag2 = new boolean[s2.length()];
        int nCommon = countCommon(s1, s2, flag1, flag2, maxLen);

        // if no characters in common, return
        if (nCommon == 0)
            return 0.0;

        // count the number of transpositions
        int nTrans = countTranspositions(s1, s2, flag1, flag2);

        // adjust for similarities in nonmatched characters
        double nSimi = (double)nCommon;
        if (this.adjSimilarCharacter) {
            nSimi += 0.3 * countSimilar(s1, s2, minLen, nCommon, flag1, flag2);
        }

        // main weight computation
        double weight = nSimi / s1.length() + nSimi / s2.length() +
                (nCommon - nTrans + 0.0) / nCommon;
        weight /= 3.0;

        // continue to boost the weight if the strings are similar
        if (weight > 0.7) {
            // adjust for having up to the first 4 characters in common
            int i = 0;
            if (this.adjWinklerPrefix) {
                int hi = (minLen < 4) ? minLen : 4;
                while (i < hi && s1.charAt(i) == s2.charAt(i) && Character.isLetter(s2.charAt(i)))
                    i++;
                if (i > 0)
                    weight += i * 0.1 * (1.0 - weight);
            }

            // Adjust for long strings: after agreeing beginning chars, at least two
            // more must agree and the number of agreements must be > 0.5 of
            // remaining characters.
            if (this.adjLongStrings && minLen > 4 && nCommon > i + 1 && 2 * nCommon > minLen + i)
            {
                if (Character.isLetter(s1.charAt(0)))
                    weight += (1.0 - weight) * (nCommon - i - 1.0) /
                            (s1.length() + s2.length() - i * 2.0 + 2.0);
            }
        }

        return weight;
    }

    /**
     * Compare two fields using the fuzzy string comparator, and return a match
     * level according to the level breaks that were passed to the constructor.
     * Currently using a linear search to find the match level, since I don't
     * expect there to be more than 4 or 5 levels.
     */
    @Override
    public int compare(Field field1, Field field2) {
        double weight = fuzzyMatch(field1.stringValue(), field2.stringValue());

        int matchLevel = 0;
        for (int i = 0; i < levels.length; i++) {
            if (weight > levels[i]) {
                matchLevel = levels.length - i;
                break;
            }
        }

        return matchLevel;
    }

    @Override
    public int nLevels() {
        return this.levels.length + 1;
    }

    private final double[] levels;
    private final boolean adjSimilarCharacter, adjWinklerPrefix, adjLongStrings;
}
