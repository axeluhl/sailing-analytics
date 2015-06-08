package com.sap.sailing.gwt.home.mobile;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class MobileEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {

        RootPanel.get().add(new Label("Hello mobile world"));
        
    }
    
}