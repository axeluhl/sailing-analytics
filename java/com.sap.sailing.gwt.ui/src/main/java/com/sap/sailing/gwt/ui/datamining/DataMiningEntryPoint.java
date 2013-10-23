package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private static final String PARAM_BENCHMARK = "benchmark";

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();

        String benchmarkParameter = Window.Location.getParameter(PARAM_BENCHMARK);
        boolean showBenchmark = benchmarkParameter != null && benchmarkParameter.equals("true");
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.dataMining(), stringMessages, this);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(stringMessages, true, null, null);
        logoAndTitlePanel.add(globalNavigationPanel);
        rootPanel.add(logoAndTitlePanel);
        
        DataMiningPanel dataMiningPanel = new DataMiningPanel(stringMessages, sailingService, this, showBenchmark);
		rootPanel.add(dataMiningPanel);
    }

}