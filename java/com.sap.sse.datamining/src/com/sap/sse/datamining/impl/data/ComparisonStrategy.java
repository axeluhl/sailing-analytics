package com.sap.sse.datamining.impl.data;

import java.util.Comparator;

public enum ComparisonStrategy {
    
    LOWER_THAN("[") {
        @Override
        public boolean validateComparisonResult(int comparisonResult) {
            return comparisonResult <= -1;
        }
    },
    
    LOWER_EQUALS_THAN("]") {
        @Override
        public boolean validateComparisonResult(int comparisonResult) {
            return comparisonResult == 0 || comparisonResult <= -1;
        }
    },
    
    GREATER_THAN("]") {
        @Override
        public boolean validateComparisonResult(int comparisonResult) {
            return comparisonResult >= 1;
        }
    },
    
    GREATER_EQUALS_THAN("[") {
        @Override
        public boolean validateComparisonResult(int comparisonResult) {
            return comparisonResult == 0 || comparisonResult >= 1;
        }
    };
    
    private String signifier = null;
    
    private ComparisonStrategy(String signifier) {
        this.signifier = signifier;
    }

    public String getSignifier() {
        return signifier;
    }

    /**
     * Validates the return value of {@link Comparable#compareTo(Object)} or
     * {@link Comparator#compare(Object, Object)} and returns <code>true</code>, if the
     * object on which <code>compareTo</code> was called or the first argument of <code>compare</code>
     * matches the <code>ComparisonStrategy</code> for the argument of <code>compareTo</code> or the
     * second argument of <code>compare</code>.<br>
     * <br>
     * For example, if we use <code>GREATER_THAN</code> and have <code>value1 = 15</code> and
     * <code>value1 = 10</code>. This method would return <code>true</code> for <code>value1.compareTo(value2)</code>
     * and <code>Comparator.compare(value1, value2)</code>.
     *  
     * 
     * @param comparisonResult The return value of {@link Comparable#compareTo(Object)} or
     *                         {@link Comparator#compare(Object, Object)}
     * @return <code>true</code>, if the <code>comparisonResult</code> matches the <code>ComparisonStrategy</code>
     */
    public abstract boolean validateComparisonResult(int comparisonResult);
}