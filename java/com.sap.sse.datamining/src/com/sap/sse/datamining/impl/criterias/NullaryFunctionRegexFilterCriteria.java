package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.functions.Function;

public class NullaryFunctionRegexFilterCriteria<DataType> extends RegexFilterCriteria<DataType> {

    private Function<String> function;

    public NullaryFunctionRegexFilterCriteria(String regex, Function<String> function) {
        super(regex);
        this.function = function;
    }

    @Override
    protected String getValueToMatch(DataType dataEntry) {
        return function.tryToInvoke(dataEntry);
    }

}
