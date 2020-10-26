package com.sap.sse.gwt.adminconsole;

import java.util.logging.Logger;

import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public abstract class AdminConsolePanelSupplier<W extends Widget> {
    
    private Logger logger = Logger.getLogger(AdminConsolePanelSupplier.class.toString());
    protected W widget;
    private String title;

    public AdminConsolePanelSupplier() {
        logger.info("Create AdminConsolePanelSupplier");
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }

    public abstract void getAsync(RunAsyncCallback callback);

    public W get() {
        return widget;
    }
}
