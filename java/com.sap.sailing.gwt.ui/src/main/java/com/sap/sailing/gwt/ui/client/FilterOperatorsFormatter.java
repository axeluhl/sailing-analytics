package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.shared.GWT;
import com.sap.sailing.domain.common.filter.FilterOperators;

public class FilterOperatorsFormatter {
    
    private FilterOperatorsFormatter() { }
        
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);
        
    public static String format(FilterOperators operator) {
        switch (operator) {
        case Contains:
            return stringMessages.operatorContains();
        case EndsWith:
            return stringMessages.operatorEndsWith();
        case Equals:
            return stringMessages.operatorEquals();
        case GreaterThan:
            return stringMessages.operatorGreaterThan();
        case GreaterThanEquals:
            return stringMessages.operatorGreaterThanEquals();
        case LessThan:
            return stringMessages.operatorLessThan();
        case LessThanEquals:
            return stringMessages.operatorLessThanEquals();
        case NotContains:
            return stringMessages.operatorNotContains();
        case NotEqualTo:
            return stringMessages.operatorNotEqualTo();
        case StartsWith:
            return stringMessages.operatorStartsWith();
        }
        return null;
    }
}
