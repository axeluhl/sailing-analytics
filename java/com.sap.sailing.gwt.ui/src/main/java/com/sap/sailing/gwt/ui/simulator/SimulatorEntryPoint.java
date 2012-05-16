package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;


public class SimulatorEntryPoint extends AbstractEntryPoint {
    private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);
    

    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        createUi();
    }

    private void createUi() {
        //initMap();

        SimulatorMainPanel mainPanel = new SimulatorMainPanel(simulatorSvc,stringMessages, this);
        RootLayoutPanel.get().add(mainPanel);
    }

   


   
    

 
}
