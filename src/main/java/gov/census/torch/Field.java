package gov.census.torch;

/**
 * A <code>Field</code> is a <code>String</code> value with methods for
 * interpreting the value as an int or double.
 */
public class Field {

    /**
     * Constructs a new <code>Field</code> wrapping the given
     * <code>String</code>.
     */
    public Field(String s) {
        _val = s;
        _dval = Double.NaN;
        _ival = Integer.MIN_VALUE;
        _empty = s.trim().isEmpty();
    }

    /**
     * Returns true if the <code>Field</code> is all whitespace characters.
     */
    public boolean empty() {
        return _empty;
    }

    /**
     * Returns the <code>Field</code> value as a <code>String</code>. This is
     * the same <code>String</code> value originally used to construct the
     * <code>Field</code>.
     */
    public String stringValue() {
        return _val;
    }

    /**
     * Returns the <code>Field</code> value as an <code>int</code>. The first
     * time this method is invoked, it attempts to parse the value using
     * <code>Integer.parseInt(String)</code>. As such, it can fail with an
     * exception.
     */
    public int intValue() {
        if (_ival == Integer.MIN_VALUE)
            _ival = Integer.parseInt(_val);

        return _ival;
    }

    /**
     * Returns the <code>Field</code> value as a <code>double</code>. The first
     * time this method is invoked, it attempts to parse the value using
     * <code>Double.parseDouble(String)</code>. As such it can fail with an
     * exception.
     */
    public double doubleValue() {
        if (Double.isNaN(_dval))
            _dval = Double.parseDouble(_val);

        return _dval;
    }

    /**
     * @see #stringValue
     */
    @Override
    public String toString() {
        return _val;
    }

    /**
     * Two fields are equal if they store the same <code>String</code> value.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Field && _val.equals(obj.toString()));
    }

    private final String _val;
    private final boolean _empty;
    private double _dval;
    private int _ival;
}
