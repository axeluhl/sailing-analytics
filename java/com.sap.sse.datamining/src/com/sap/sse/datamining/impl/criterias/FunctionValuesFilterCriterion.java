package com.sap.sse.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;

public class FunctionValuesFilterCriterion<ElementType> extends AbstractFilterCriterion<ElementType> {

    private final Function<?> function;
    private final ParameterProvider parameterProvider;
    private final Collection<?> valuesToMatch;
    
    public FunctionValuesFilterCriterion(Class<ElementType> elementType, Function<?> function, Collection<?> valuesToMatch) {
        this(elementType, function, ParameterProvider.NULL, valuesToMatch);
    }

    public FunctionValuesFilterCriterion(Class<ElementType> elementType, Function<?> function, ParameterProvider parameterProvider, Collection<?> valuesToMatch) {
        super(elementType);
        this.function = function;
        this.parameterProvider = parameterProvider;
        this.valuesToMatch = new HashSet<>(valuesToMatch);
    }

    @Override
    public boolean matches(ElementType dataEntry) {
        Object value = function.tryToInvoke(dataEntry, parameterProvider);
        
        for (Object valueToMatch : valuesToMatch) {
            if (Objects.equals(value, valueToMatch)) {
                return true;
            }
        }
        return false;
    }

}
