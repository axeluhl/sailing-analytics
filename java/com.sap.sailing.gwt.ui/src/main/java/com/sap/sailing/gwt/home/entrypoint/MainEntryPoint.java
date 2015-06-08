package com.sap.sailing.gwt.home.entrypoint;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class MainEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {

        DelegatingEntryPoint ep = GWT.create(DelegatingEntryPoint.class);
        ep.onModuleLoad();
        
    }
    
}