package com.sap.sse.gwt.client;

import com.google.gwt.user.client.ui.Widget;

public interface ErrorReporter {
    void reportError(String message);
    void reportError(String title, String message);
    void reportError(String message, boolean silentMode);

    void reportPersistentInformation(String message);

    Widget getPersistentInformationWidget();

}
