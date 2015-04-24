package com.sap.sse.datamining.functions;

import java.lang.reflect.InvocationTargetException;

public interface ParameterizedFunction<ReturnType> {
    
    public Function<ReturnType> getFunction();
    public ParameterProvider getParameterProvider();
    
    /**
     * Tries to invoke the function for the given <code>instance</code> with parameters of the {@link ParameterProvider}.
     * 
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException},
     *         {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance);

}
