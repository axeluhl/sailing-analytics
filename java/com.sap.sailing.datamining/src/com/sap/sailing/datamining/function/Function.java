package com.sap.sailing.datamining.function;

import java.util.Locale;

import com.sap.sailing.datamining.DataMiningStringMessages;

public interface Function<ReturnType> {

    public Class<?> getDeclaringClass();
    public Iterable<Class<?>> getParameters();

    public boolean isDimension();
    
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages);
    
    public ReturnType invoke(Object instance);

}
