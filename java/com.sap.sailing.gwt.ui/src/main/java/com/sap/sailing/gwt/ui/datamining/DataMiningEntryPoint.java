package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        Button runQueryButton = new Button("Run");
        rootPanel.add(runQueryButton);
    }

}