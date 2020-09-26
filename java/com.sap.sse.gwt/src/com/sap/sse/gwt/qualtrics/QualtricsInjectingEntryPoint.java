package com.sap.sse.gwt.qualtrics;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.sap.sse.common.Duration;

public class QualtricsInjectingEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.log("Inject Qualtrics");
        Qualtrics.ensureInjected();
        // we'd like to trigger the "count-down" intercepts always when loading the page
        new Timer() {
            @Override
            public void run() {
                Qualtrics.triggerIntercepts();
            }
        }.schedule((int) Duration.ONE_MINUTE.asMillis());
    }
}
