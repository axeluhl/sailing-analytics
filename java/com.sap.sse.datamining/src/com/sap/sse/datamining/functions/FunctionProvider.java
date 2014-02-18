package com.sap.sse.datamining.functions;

import java.util.Collection;

import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface FunctionProvider {

    public Collection<Function<?>> getDimensionsFor(Class<?> dataType);

    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);

    /**
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionFor(FunctionDTO functionDTO);

}
