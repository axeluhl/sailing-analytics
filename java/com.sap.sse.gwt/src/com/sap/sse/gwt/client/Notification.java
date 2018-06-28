package com.sap.sse.gwt.client;

import com.google.gwt.user.client.Window;

/**
 * Utility class to show non obstuive warning / info messages using toast/snackbar
 */
public class Notification {
    private Notification() {
    }

    public static void error(String message) {
        Window.alert("replace me: " + message);
    }

    public static void info(String message) {
        Window.alert("replace me: " + message);
    }
}
