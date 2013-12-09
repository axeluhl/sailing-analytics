package com.sap.sailing.selenium.pages.gwt.query.operation.bool;

import com.sap.sailing.selenium.pages.gwt.query.Expression;
import com.sap.sailing.selenium.pages.gwt.query.operation.PredicateOperation;

public class Not extends PredicateOperation {
    public Not(Expression<Boolean> argument) {
        super(argument);
    }
    
    @Override
    public Boolean evaluate(Object argument) {
        Expression<Boolean> leftExpression = getArg(0);
        Boolean result = leftExpression.evaluate(argument);
        
        return Boolean.valueOf(!result.booleanValue());
    }

}
