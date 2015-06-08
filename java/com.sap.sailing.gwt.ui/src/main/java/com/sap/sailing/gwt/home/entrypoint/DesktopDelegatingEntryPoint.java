package com.sap.sailing.gwt.home.entrypoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.home.client.HomeEntryPoint;

public class DesktopDelegatingEntryPoint extends DelegatingEntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.runAsync(DesktopDelegatingEntryPoint.class, new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                GWT.log("Proceed with desktop entrypoint");
                HomeEntryPoint ep = new HomeEntryPoint();
                ep.onModuleLoad();
            }

            @Override
            public void onFailure(Throwable reason) {
                GWT.log("Could not load Desktop entry point");
            }
        });
    }
}
