package com.sap.sse.common.filter;

import java.util.Comparator;

public class BinaryOperator<T extends Number & Comparable<T>> implements FilterOperator<T> {
    private Operators operator;

    private NumberComparator<T> numberComparator = new NumberComparator<T>();

    /** TODO: Replace with concrete implementations of BinaryOperator */
    public enum Operators {
        Equals, /** Returns only records with the specified value */
        NotEqualTo, /** Returns only records that do not include the specified value */
        LessThan, /** Returns only records that are less than the specified value */
        LessThanEquals, /** Returns only records that are less than or equal to the specified value */
        GreaterThan, /** Returns only records that are more than the specified value */
        GreaterThanEquals, /** Returns only records that are more than or equal to the specified value */
    }

    public BinaryOperator(Operators operator) {
        this.operator = operator;
    }
    
    public Operators getOperator() {
        return operator;
    }

    public void setOperator(Operators operator) {
        if (operator != null) {
            this.operator = operator;
        }
    }

    @Override
    public boolean matchValues(T filterValue, T valueToMatch) {
        boolean result = false;
        if (operator != null) {
            int compareResult = numberComparator.compare(valueToMatch, filterValue);
            switch (operator) {
            case LessThanEquals:
                result = compareResult < 0 || compareResult == 0;
                break;
            case Equals:
                result = compareResult == 0;
                break;
            case GreaterThanEquals:
                result = compareResult > 0 || compareResult == 0;
                break;
            case LessThan:
                result = compareResult < 0;
                break;
            case GreaterThan:
                result = compareResult > 0;
                break;
            case NotEqualTo:
                result = compareResult < 0 || compareResult > 0;
                break;
            }
        }
        return result;
    }
    
    class NumberComparator<S extends Number & Comparable<S>> implements Comparator<S> {
        public int compare( S a, S b ) throws ClassCastException {
            return a.compareTo( b );
        }
    }
    
    @Override
    public String getName() {
        return operator.name();        
    }
}
