package com.sap.sailing.server.trackfiles.impl.doublefix;


public interface DoubleFixProcessor {
    void accept(DoubleVectorFixData fix);
    void finish();
}