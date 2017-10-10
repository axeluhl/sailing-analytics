package com.sap.sailing.server.trackfiles.impl.doublefix;

@FunctionalInterface
public interface DoubleFixProcessor {
    void accept(DoubleVectorFixData fix);
    default void finish() {}
}