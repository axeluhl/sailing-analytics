package com.sap.sse.datamining.functions;

import java.util.Collection;

import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface FunctionProvider {

    public Collection<Function<?>> getDimensionsFor(Class<?> dataType);

    /**
     * Collects all dimensions for the given data type, where transitive dimensions will also be
     * collected. A transitive dimension is a dimension, that can be reached with functions of the
     * given data type.<br />
     * The depth defines the maximum amount of functions that can be between the data type and the
     * collected dimensions. A depth of 0 would collect only the dimensions of the given data type.
     * @param dataType
     * @param depth
     * @return
     */
    public Collection<Function<?>> getTransitiveDimensionsFor(Class<?> dataType, int depth);

    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);

    /**
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionFor(FunctionDTO functionDTO);

}
