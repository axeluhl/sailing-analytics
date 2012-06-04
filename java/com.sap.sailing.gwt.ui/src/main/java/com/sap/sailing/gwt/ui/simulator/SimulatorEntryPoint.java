package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;


public class SimulatorEntryPoint extends AbstractEntryPoint {
    private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);
    private int   xRes = 5;
    private int   yRes = 5;

    private static Logger logger = Logger.getLogger("com.sap.sailing");


    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        createUi();
    }

    private void createUi() {
        //initMap();
        String horizontalRes = Window.Location.getParameter("horizontalRes");
        if (horizontalRes == null || horizontalRes.isEmpty()) {
           logger.config("Using default horizontal resolution " + xRes);
        } else {
            xRes = Integer.parseInt(horizontalRes);
        }
        String verticalRes = Window.Location.getParameter("verticalRes");
        if (verticalRes == null || verticalRes.isEmpty()) {
            logger.config("Using default horizontal resolution " + yRes);
        } else {
            yRes = Integer.parseInt(verticalRes);
        }
        SimulatorMainPanel mainPanel = new SimulatorMainPanel(simulatorSvc,stringMessages, this, xRes, yRes);
        RootLayoutPanel.get().add(mainPanel);
    }

   


   
    

 
}
