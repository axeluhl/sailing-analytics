package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private static final String PARAM_BENCHMARK = "benchmark";

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        GPSFixSelectionPanel selectionPanel = new GPSFixSelectionPanel(stringMessages, sailingService, this);
        dataMiningElementsPanel.add(selectionPanel);
        
        GPSFixQueryPanel queryPanel = new GPSFixQueryPanel(stringMessages, sailingService, this, selectionPanel);
        dataMiningElementsPanel.add(queryPanel);
        
        String benchmarkParameter = Window.Location.getParameter(PARAM_BENCHMARK);
        if (benchmarkParameter != null && benchmarkParameter.equals("true")) {
            GPSFixQueryBenchmarkPanel benchmarkPanel = new GPSFixQueryBenchmarkPanel(stringMessages, sailingService, this, selectionPanel);
            dataMiningElementsPanel.add(benchmarkPanel);
        }
    }

}