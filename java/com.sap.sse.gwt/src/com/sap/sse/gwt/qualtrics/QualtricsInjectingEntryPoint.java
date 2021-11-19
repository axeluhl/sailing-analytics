package com.sap.sse.gwt.qualtrics;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class QualtricsInjectingEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        if (ClientConfiguration.getInstance().isBrandingActive()) {
            GWT.log("Inject Qualtrics");
            Qualtrics.ensureInjected(/* project ID */ "ZN_7WmsxxHQyCeUivX");
        } else {
            GWT.log("Qualtrics not injected because debranding is active");
        }
    }
}
