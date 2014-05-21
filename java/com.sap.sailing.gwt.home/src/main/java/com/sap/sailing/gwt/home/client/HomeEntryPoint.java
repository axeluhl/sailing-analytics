package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.ApplicationController;

public class HomeEntryPoint implements EntryPoint {
    private static final ApplicationController controller = GWT.create(ApplicationController.class);

    public void onModuleLoad() {
        controller.init();
    }
}