package com.sap.sse.datamining.functions;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.SideEffectFreeValue;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface Function<ReturnType> {

    public Class<?> getDeclaringClass();
    public Iterable<Class<?>> getParameters();

    public boolean isDimension();
    
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    /**
     * Tries to invoke the function for the given <code>instance</code> without parameters.
     * 
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException},
     *         {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance);
    
    /**
     * Tries to invoke the function for the given <code>instance</code> and the given <code>parameters</code>.
     * 
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException},
     *         {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance, Object... parameters);

    /**
     * Creates the corresponding DTO for this function, with the function name as display name.
     */
    public FunctionDTO asDTO();
    
    /**
     * Creates the corresponding DTO for this function, with the retrieved string message for the given locale and the
     * contained message key. The message key is provided with the {@link Dimension} or {@link SideEffectFreeValue}
     * annotation.<br>
     * If the function has no message key, the function name is used as display name.
     */
    public FunctionDTO asDTO(Locale locale, DataMiningStringMessages stringMessages);

}
