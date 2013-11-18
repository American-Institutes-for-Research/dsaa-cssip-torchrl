package gov.census.torch;

/**
 * A field is a String value with methods for interpreting the value as an int or double.
 */
public class Field {

    /**
     * Construct a new Field wrapping the given String.
     */
    public Field(String s) {
        _val = s;
        _dval = Double.NaN;
        _ival = Integer.MIN_VALUE;
        _empty = s.trim().isEmpty();
    }

    /**
     * Returns true if the Field is all whitespace characters.
     */
    public boolean empty() {
        return _empty;
    }

    public String stringValue() {
        return _val;
    }

    public int intValue() {
        if (_ival == Integer.MIN_VALUE)
            _ival = Integer.parseInt(_val);

        return _ival;
    }

    public double doubleValue() {
        if (Double.isNaN(_dval))
            _dval = Double.parseDouble(_val);

        return _dval;
    }

    @Override
    public String toString() {
        return _val;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Field && _val.equals(obj.toString()));
    }

    private final String _val;
    private final boolean _empty;
    private double _dval;
    private int _ival;
}
