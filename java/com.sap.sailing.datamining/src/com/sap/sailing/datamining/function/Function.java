package com.sap.sailing.datamining.function;

public interface Function {

    public Class<?> getDeclaringClass();
    public Iterable<Class<?>> getParameters();

    public boolean isDimension();

}
