package com.sap.sailing.gwt.home.desktop;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.gwt.resources.Highcharts;

public class HighChartInjector {
    public static void loadHighCharts(Runnable afterLoad){
        GWT.runAsync(new RunAsyncCallback() {
            
            @Override
            public void onSuccess() {
                CommonControlsCSS.ensureInjected();
                Highcharts.ensureInjected();
                afterLoad.run();
            }
            
            @Override
            public void onFailure(Throwable reason) {
                Window.alert(reason.getMessage());                
            }
        });
    }
}
