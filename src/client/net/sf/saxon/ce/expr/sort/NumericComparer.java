package client.net.sf.saxon.ce.expr.sort;
import client.net.sf.saxon.ce.expr.XPathContext;
import client.net.sf.saxon.ce.lib.StringCollator;
import client.net.sf.saxon.ce.om.StandardNames;
import client.net.sf.saxon.ce.value.*;
import client.net.sf.saxon.ce.type.StringToDouble;

/**
 * A Comparer used for comparing sort keys when data-type="number". The items to be
 * compared are converted to numbers, and the numbers are then compared directly. NaN values
 * compare equal to each other, and equal to an empty sequence, but less than anything else.
 * <p/>
 * This class is used in XSLT only, so there is no need to handle XQuery's "empty least" vs
 * "empty greatest" options.
 *
 * @author Michael H. Kay
 *
 */

public class NumericComparer implements AtomicComparer {

    private static NumericComparer THE_INSTANCE = new NumericComparer();

    public static NumericComparer getInstance() {
        return THE_INSTANCE;
    }

    protected NumericComparer() {
    }

    public StringCollator getCollator() {
        return null;
    }

    /**
     * Supply the dynamic context in case this is needed for the comparison
     * @param context the dynamic evaluation context
     * @return either the original AtomicComparer, or a new AtomicComparer in which the context
     * is known. The original AtomicComparer is not modified
     */

    public AtomicComparer provideContext(XPathContext context) {
        return this;
    }

    /**
    * Compare two Items by converting them to numbers and comparing the numeric values. If either
    * value cannot be converted to a number, it is treated as NaN, and compares less that the other
    * (two NaN values compare equal).
    * @param a the first Item to be compared.
    * @param b the second Item to be compared.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are not Items
    */

    public int compareAtomicValues(AtomicValue a, AtomicValue b) {
        double d1, d2;

        if (a instanceof NumericValue) {
            d1 = ((NumericValue)a).getDoubleValue();
        } else if (a == null) {
            d1 = Double.NaN;
        } else {
            try {
                d1 = StringToDouble.stringToNumber(a.getStringValueCS());
            } catch (NumberFormatException err) {
                d1 = Double.NaN;
            }
        }

        if (b instanceof NumericValue) {
            d2 = ((NumericValue)b).getDoubleValue();
        } else if (b == null) {
            d2 = Double.NaN;
        } else {
            try {
                d2 = StringToDouble.stringToNumber(b.getStringValueCS());
            } catch (NumberFormatException err) {
                d2 = Double.NaN;
            }
        }

        if (Double.isNaN(d1)) {
            if (Double.isNaN(d2)) {
                return 0;
            } else {
                return -1;
            }
        }
        if (Double.isNaN(d2)) {
            return +1;
        }
        if (d1 < d2) return -1;
        if (d1 > d2) return +1;
        return 0;

    }

    /**
     * Compare two AtomicValue objects for equality according to the rules for their data type. UntypedAtomic
     * values are compared by converting to the type of the other operand.
     *
     * @param a the first object to be compared.
     * @param b the second object to be compared.
     * @return true if the values are equal, false if not
     * @throws ClassCastException if the objects are not comparable
     */

    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return compareAtomicValues(a, b) == 0;
    }

    /**
     * Get a comparison key for an object. This must satisfy the rule that if two objects are equal
     * according to the XPath eq operator, then their comparison keys are equal according to the Java
     * equals() method, and vice versa. There is no requirement that the
     * comparison keys should reflect the ordering of the underlying objects.
     */

    public ComparisonKey getComparisonKey(AtomicValue a) {
        if (a instanceof NumericValue) {
            return new ComparisonKey(StandardNames.XS_NUMERIC, toDoubleValue(((NumericValue)a)));
        } else if (a == null) {
            return new ComparisonKey(StandardNames.XS_NUMERIC, "NaN");
        } else {
            try {
                double d = StringToDouble.stringToNumber(a.getStringValueCS());
                return new ComparisonKey(StandardNames.XS_NUMERIC, new DoubleValue(d));
            } catch (NumberFormatException err) {
                return new ComparisonKey(StandardNames.XS_NUMERIC, "NaN");
            }
        }
    }

    private static DoubleValue toDoubleValue(NumericValue nv) {
        if (nv instanceof DoubleValue) {
            return ((DoubleValue)nv);
        } else {
            return new DoubleValue(nv.getDoubleValue());
        }
    }

}

// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is “Incompatible With Secondary Licenses”, as defined by the Mozilla Public License, v. 2.0.