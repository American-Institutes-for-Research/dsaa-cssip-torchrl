package gov.census.torch;

/**
 * A field backed by a String.
 */
public class Field {

    public Field(String s) {
        this.val = s;
        this.dval = Double.NaN;
        this.ival = Integer.MIN_VALUE;
    }

    public boolean isEmpty() {
        for (int i = 0; i < val.length(); i++) {
            if (!Character.isWhitespace(val.charAt(i)))
                return false;
        }

        return true;
    }

    public String stringValue() {
        return val;
    }

    public int intValue() {
        if (ival == Integer.MIN_VALUE)
            ival = Integer.parseInt(val);

        return ival;
    }

    public double doubleValue() {
        if (Double.isNaN(dval))
            dval = Double.parseDouble(val);

        return dval;
    }

    private String val;
    private double dval;
    private int ival;
}
