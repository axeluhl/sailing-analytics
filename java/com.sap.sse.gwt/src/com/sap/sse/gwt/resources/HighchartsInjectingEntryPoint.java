package com.sap.sse.gwt.resources;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class HighchartsInjectingEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.log("Inject highcharts");
        Highcharts.ensureInjected();
    }
}
