package com.sap.sse.datamining.functions;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.i18n.ServerStringMessages;

public interface Function<ReturnType> {

    public Class<?> getDeclaringType();
    public Iterable<Class<?>> getParameters();
    public Class<ReturnType> getReturnType();

    public boolean isDimension();
    
    public String getSimpleName();
    
    /**
     * @return <code>true</code>, if the method {@link #getLocalizedName(Locale, ServerStringMessages) getLocalizedName} would return something
     * other than the method {@link #getSimpleName()}.
     */
    public boolean isLocatable();
    
    public String getLocalizedName(Locale locale, ServerStringMessages stringMessages);
    
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
    
    public Unit getResultUnit();
    
    public int getResultDecimals();
    
    public int getOrdinal();

}
