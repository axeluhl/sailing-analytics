package com.sap.sse.datamining.functions;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.Unit;

public interface Function<ReturnType> {

    public Class<?> getDeclaringType();
    public Iterable<Class<?>> getParameters();
    public Class<ReturnType> getReturnType();

    public boolean isDimension();
    
    public String getSimpleName();
    
    /**
     * @return <code>true</code>, if the method {@link #getLocalizedName(Locale, DataMiningStringMessages) getLocalizedName} would return something
     * other than the method {@link #getSimpleName()}.
     */
    public boolean isLocalizable();
    
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    /**
     * Tries to invoke the function for the given <code>instance</code> without parameters.
     * 
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException},
     *         {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance);
    
    /**
     * Tries to invoke the function for the given <code>instance</code> and the <code>parameters</code> provided
     * by the given {@link ParameterProvider}.
     * 
     * @return The result of the function or <code>null</code>, if an {@link InvocationTargetException},
     *         {@link IllegalAccessException} or {@link IllegalArgumentException} was thrown.
     */
    public ReturnType tryToInvoke(Object instance, ParameterProvider parameterProvider);
    
    public Unit getResultUnit();
    
    public int getResultDecimals();
    
    public int getOrdinal();

}
