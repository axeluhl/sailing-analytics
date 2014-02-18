package com.sap.sailing.selenium.pages.gwt.query.operation.collection;

import java.util.Collection;

import com.sap.sailing.selenium.pages.gwt.query.Expression;
import com.sap.sailing.selenium.pages.gwt.query.operation.PredicateOperation;

public class In extends PredicateOperation {
    public In(Expression<?> left, Expression<Collection<?>> rigth) {
        super(left, rigth);
    }
    
    @Override
    public Boolean evaluate(Object argument) {
        Expression<?> leftExpression = getArg(0);
        Object value = leftExpression.evaluate(argument);
        
        Expression<Collection<?>> rigthExpression = getArg(1);
        Collection<?> collection = rigthExpression.evaluate(argument);
        
        return Boolean.valueOf(collection.contains(value));
    }

}
