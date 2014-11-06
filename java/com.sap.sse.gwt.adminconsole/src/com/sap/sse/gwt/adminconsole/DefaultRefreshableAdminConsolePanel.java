package com.sap.sse.gwt.adminconsole;

import com.google.gwt.user.client.ui.Widget;

public class DefaultRefreshableAdminConsolePanel<W extends Widget> implements RefreshableAdminConsolePanel {
    @Override
    public void refreshAfterBecomingVisible() {
    }

    private final W widget;
    
    public DefaultRefreshableAdminConsolePanel(W widget) {
        this.widget = widget;
    }

    @Override
    public W getWidget() {
        return widget;
    }
}
