package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.raceboard.GlobalNavigationPanel;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private static final String PARAM_BENCHMARK = "benchmark";

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();

        String benchmarkParameter = Window.Location.getParameter(PARAM_BENCHMARK);
        boolean showBenchmark = benchmarkParameter != null && benchmarkParameter.equals("true");
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel("Data Mining", stringMessages, this);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(stringMessages, true, null, null);
        logoAndTitlePanel.add(globalNavigationPanel);

        rootPanel.add(logoAndTitlePanel);
        
        GPSFixSelectionPanel selectionPanel = new GPSFixSelectionPanel(stringMessages, sailingService, this);
        rootPanel.add(selectionPanel);
        
        GPSFixQueryPanel queryPanel = new GPSFixQueryPanel(stringMessages, sailingService, this, selectionPanel);
        rootPanel.add(queryPanel);
        
        if (showBenchmark) {
            GPSFixQueryBenchmarkPanel benchmarkPanel = new GPSFixQueryBenchmarkPanel(stringMessages, sailingService, this, selectionPanel);
            rootPanel.add(benchmarkPanel);
        }
    }

}