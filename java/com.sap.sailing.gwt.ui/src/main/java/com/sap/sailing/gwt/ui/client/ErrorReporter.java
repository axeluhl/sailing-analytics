package com.sap.sailing.gwt.ui.client;

public interface ErrorReporter {
    void reportError(String message);
    void reportWarning(String message);
}
