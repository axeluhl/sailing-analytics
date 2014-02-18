package com.sap.sailing.selenium.pages.gwt.query.operation.string;

import com.sap.sailing.selenium.pages.gwt.query.Expression;
import com.sap.sailing.selenium.pages.gwt.query.operation.SimpleOperation;

public class Matches extends SimpleOperation<Boolean> {

    public Matches(Expression<String> argument, Expression<String> regex) {
        super(Boolean.class, argument, regex);
    }

    @Override
    public Boolean evaluate(Object argument) {
        Expression<String> argumentExpression = getArg(0);
        String argumentValue = argumentExpression.evaluate(argument);
        
        Expression<String> regexExpression = getArg(1);
        String regexValue = regexExpression.evaluate(argument);
        
        return Boolean.valueOf(argumentValue.matches(regexValue));
    }

}
