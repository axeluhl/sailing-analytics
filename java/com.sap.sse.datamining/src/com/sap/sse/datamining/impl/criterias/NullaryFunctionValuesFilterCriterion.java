package com.sap.sse.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.functions.Function;

public class NullaryFunctionValuesFilterCriterion<DataType> implements FilterCriterion<DataType> {

    private Function<?> function;
    private Collection<?> valuesToMatch;

    public NullaryFunctionValuesFilterCriterion(Function<?> function, Collection<?> valuesToMatch) {
        this.function = function;
        this.valuesToMatch = new HashSet<>(valuesToMatch);
    }

    @Override
    public boolean matches(DataType dataEntry) {
        Object value = function.tryToInvoke(dataEntry);
        
        for (Object valueToMatch : valuesToMatch) {
            if (Objects.equals(value, valueToMatch)) {
                return true;
            }
        }
        return false;
    }

}
