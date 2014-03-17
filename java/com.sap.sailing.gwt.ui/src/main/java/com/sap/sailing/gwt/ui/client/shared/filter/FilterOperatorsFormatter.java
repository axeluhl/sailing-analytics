package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.core.shared.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.TextOperator;

public class FilterOperatorsFormatter {
    
    private FilterOperatorsFormatter() { }
        
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);

    public static String format(BinaryOperator.Operators operator) {
        switch (operator) {
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
        case NotEqualTo:
            return stringMessages.operatorNotEqualTo();
        }
        return null;
    }

    public static String format(TextOperator.Operators operator) {
        switch (operator) {
        case Contains:
            return stringMessages.operatorContains();
        case EndsWith:
            return stringMessages.operatorEndsWith();
        case Equals:
            return stringMessages.operatorEquals();
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
