package com.sap.sse.gwt.adminconsole;

import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public abstract class AdminConsolePanelSupplier<W extends Widget> {
    
    protected W widget;
    private String title;

    public AdminConsolePanelSupplier() {
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public abstract W init();
    
    public abstract void getAsync(RunAsyncCallback callback);

    public W get() {
        return widget;
    }
}
