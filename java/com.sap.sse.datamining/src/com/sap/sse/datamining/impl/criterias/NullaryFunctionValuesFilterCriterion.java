package com.sap.sse.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import com.sap.sse.datamining.functions.Function;

public class NullaryFunctionValuesFilterCriterion<ElementType> extends AbstractFilterCriterion<ElementType> {

    private Function<?> function;
    private Collection<?> valuesToMatch;

    public NullaryFunctionValuesFilterCriterion(Class<ElementType> elementType, Function<?> function, Collection<?> valuesToMatch) {
        super(elementType);
        this.function = function;
        this.valuesToMatch = new HashSet<>(valuesToMatch);
    }

    @Override
    public boolean matches(ElementType dataEntry) {
        Object value = function.tryToInvoke(dataEntry);
        
        for (Object valueToMatch : valuesToMatch) {
            if (Objects.equals(value, valueToMatch)) {
                return true;
            }
        }
        return false;
    }

}
