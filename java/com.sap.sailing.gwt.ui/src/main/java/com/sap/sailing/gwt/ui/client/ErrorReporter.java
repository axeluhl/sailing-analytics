package com.sap.sailing.gwt.ui.client;

public interface ErrorReporter {
    void reportError(String message);
    void reportError(String message, boolean silentMode);
    
  //I00788 - Mihai Bogdan Eugen
    void reportNotification(String message);
}
