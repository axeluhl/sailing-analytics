package com.sap.sse.gwt.resources;

import com.google.gwt.core.client.EntryPoint;

public class CommonControlsInjectingEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        CommonControlsCSS.ensureInjected();
    }
}
