package com.sap.sailing.gwt.home.entrypoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class MobileDelegatingEntryPoint extends DelegatingEntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.runAsync(MobileDelegatingEntryPoint.class, new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                GWT.log("Proceed with desktop entrypoint");
                RootPanel.get().add(new Label("Mobile!"));
            }

            @Override
            public void onFailure(Throwable reason) {
                GWT.log("Could not load Desktop entry point");
            }
        });
    }
}
