package com.sap.sse.gwt.adminconsole;

import java.util.Map;

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
    public void setupWidgetByParams(Map<String, String> params){
    }

    @Override
    public W getWidget() {
        return widget;
    }
}
