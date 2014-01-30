package com.sap.sailing.datamining.function;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.sap.sailing.datamining.DataMiningStringMessages;
import com.sap.sailing.datamining.shared.dto.FunctionDTO;

public interface Function<ReturnType> {

    public Class<?> getDeclaringClass();
    public Iterable<Class<?>> getParameters();

    public boolean isDimension();
    
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    /**
     * Tries to invoke the function for the given <code>instance</code> without parameters.
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException}, {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance);
    
    /**
     * Tries to invoke the function for the given <code>instance</code> and the given <code>parameters</code>.
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException}, {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance, Object... parameters);
    
    public FunctionDTO asDTO(Locale locale, DataMiningStringMessages stringMessages);

}
