package com.sap.sse.gwt.adminconsole;

import com.google.gwt.user.client.ui.Widget;

public class DefaultRefreshableAdminConsolePanel<W extends Widget> implements RefreshableAdminConsolePanel<W> {
    @Override
    public void refreshAfterBecomingVisible() {
    }

    private final W widget;
    private final AdminConsolePanelSupplier<W> supplier;
    
    public DefaultRefreshableAdminConsolePanel(W widget) {
        this.widget = widget;
        this.supplier = null;
    }
    
    public DefaultRefreshableAdminConsolePanel(AdminConsolePanelSupplier<W> supplier) {
        this.widget = null;
        this.supplier = supplier;
    }

    @Override
    public W getWidget() {
        if (widget != null) {
            return widget;
        }        
        return supplier.get();
    }

    @Override
    public AdminConsolePanelSupplier<W> getAdminConsolePanelSupplier() {
        return (AdminConsolePanelSupplier<W>) supplier;
    }
}
