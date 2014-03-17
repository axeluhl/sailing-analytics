package com.sap.sse.common.filter;

public class TextOperator implements FilterOperator<String> {
    private Operators operator;
    
    /** TODO: Replace with concrete implementations of TextOperator */
    public enum Operators {
        Equals, /** Returns only records with the specified value */
        NotEqualTo, /** Returns only records that do not include the specified value */
        Contains, /** Returns only records that contain the specified value */
        NotContains, /** Returns only records that do not contain the specified value */
        StartsWith, /** Returns only records that start with the specified value */
        EndsWith, /** Returns only records that end with the specified value */
    }
    
    public TextOperator(Operators operator) {
        this.operator = operator;
    }

    public Operators getOperator() {
        return operator;
    }
    
    public void setOperator(Operators operator) {
        if(operator != null) {
            this.operator = operator;
        }
    }
    
    @Override
    public boolean matchValues(String filterValue, String valueToMatch) {
        switch(operator) {
            case Contains:
                return valueToMatch.indexOf(filterValue) >= 0;
            case Equals:
                return valueToMatch.equals(filterValue);
            case NotContains:
                return valueToMatch.indexOf(filterValue) < 0;
            case NotEqualTo:
                return !valueToMatch.equals(filterValue);
            case EndsWith:
                return valueToMatch.endsWith(filterValue);
            case StartsWith:
                return valueToMatch.startsWith(filterValue);
        }   
        return false;
    }
    
    @Override
    public String getName() {
        return operator.name();        
    }
}
