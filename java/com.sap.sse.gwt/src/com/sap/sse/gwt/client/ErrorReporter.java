package com.sap.sse.gwt.client;

public interface ErrorReporter {
    void reportError(String message);
    void reportError(String message, boolean silentMode);
    void reportPersistentInformation(String message);
}
