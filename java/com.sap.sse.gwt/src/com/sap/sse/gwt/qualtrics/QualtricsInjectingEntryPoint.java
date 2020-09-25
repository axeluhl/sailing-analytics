package com.sap.sse.gwt.qualtrics;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class QualtricsInjectingEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.log("Inject Qualtrics");
        Qualtrics.ensureInjected();
        // we'd like to trigger the "count-down" intercepts always when loading the page
        Qualtrics.triggerIntercepts();
    }
}
