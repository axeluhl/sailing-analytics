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
    private boolean autoUpdate = true;
    
    private static Logger logger = Logger.getLogger(SimulatorEntryPoint.class.getName());

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
        String autoUpdateStr = Window.Location.getParameter("autoUpdate");
        if (autoUpdateStr == null || autoUpdateStr.isEmpty()) {
            logger.config("Using default auto update " + autoUpdate);
        } else {
            autoUpdate = Boolean.parseBoolean(autoUpdateStr);
        }
        
        SimulatorMainPanel mainPanel = new SimulatorMainPanel(simulatorSvc,stringMessages, this, xRes, yRes, autoUpdate);
        RootLayoutPanel.get().add(mainPanel);
    }

   


   
    

 
}
