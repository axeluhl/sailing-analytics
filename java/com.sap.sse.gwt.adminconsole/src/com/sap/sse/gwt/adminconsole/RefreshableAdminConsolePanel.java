package com.sap.sse.gwt.adminconsole;

import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

public interface RefreshableAdminConsolePanel {
    /**
     * Invoked when the {@link #getWidget widget} has become visible, e.g., by the user
     * selecting the tab holding the panel.
     */
    void refreshAfterBecomingVisible();
    
    /**
     * The widget representing the panel to place in the administration console
     */
    Widget getWidget();

    /**
     * Invoked when the we handle selected tab by name {@link HandleTabSelectable} to set up a tab with parameters.
     */
    void setupWidgetByParams(Map<String, String> params);
}
