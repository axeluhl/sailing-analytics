package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        SelectionPanel selectionPanel = new SelectionPanel(stringMessages, sailingService, this);
        dataMiningElementsPanel.add(selectionPanel);
        
        QueryBenchmarkPanel benchmarkPanel = new QueryBenchmarkPanel(stringMessages, sailingService, this, selectionPanel);
        dataMiningElementsPanel.add(benchmarkPanel);
    }

}