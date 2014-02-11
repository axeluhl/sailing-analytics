package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import com.sap.sailing.datamining.function.Function;
import com.sap.sse.datamining.components.FilterCriteria;

public class NullaryFunctionFilterCriteria<DataType, ValueType> implements FilterCriteria<DataType> {

    private Function<ValueType> function;
    private Collection<ValueType> valuesToMatch;

    public NullaryFunctionFilterCriteria(Function<ValueType> function, Collection<ValueType> valuesToMatch) {
        this.function = function;
        this.valuesToMatch = new HashSet<>(valuesToMatch);
    }

    @Override
    public boolean matches(DataType dataEntry) {
        ValueType value = function.tryToInvoke(dataEntry);
        
        for (ValueType valueToMatch : valuesToMatch) {
            if (Objects.equals(value, valueToMatch)) {
                return true;
            }
        }
        return false;
    }

}
