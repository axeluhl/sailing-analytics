package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import com.sap.sailing.datamining.DataMiningStringMessages;
import com.sap.sailing.datamining.annotations.Dimension;
import com.sap.sailing.datamining.annotations.SideEffectFreeValue;
import com.sap.sailing.datamining.function.Function;

public class MethodWrappingFunction extends AbstractFunction implements Function {

    private Method method;
    
    private String messageKey;
    
    public MethodWrappingFunction(Method method) {
        super(isMethodADimension(method));
        this.method = method;
        initializeMessageKey();
    }

    private static boolean isMethodADimension(Method method) {
        return method.getAnnotation(Dimension.class) != null;
    }

    private void initializeMessageKey() {
        if (method.getAnnotation(Dimension.class) != null) {
            setMessageKey(method.getAnnotation(Dimension.class).value());
        }
        if (method.getAnnotation(SideEffectFreeValue.class) != null) {
            setMessageKey(method.getAnnotation(SideEffectFreeValue.class).value());
        }
    }

    private void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }
    
    @Override
    public Iterable<Class<?>> getParameters() {
        return Arrays.asList(method.getParameterTypes());
    }
    
    @Override
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        if (getMessageKey() == null || getMessageKey().isEmpty()) {
            return getShortMethodName();
        }
        return stringMessages.get(locale, getMessageKey());
    }
    
    private String getShortMethodName() {
        return method.getName();
    }

    private String getMessageKey() {
        return messageKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodWrappingFunction other = (MethodWrappingFunction) obj;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getDeclaringClass().getSimpleName() + "." + method.getName();
    }

}
