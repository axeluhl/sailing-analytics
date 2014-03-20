package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.JavaScriptInjector;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;

public class SimulatorEntryPoint extends AbstractEntryPoint {

    private final String titleName = "Strategy Simulator";

    private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);
    private int xRes = 40;
    private int yRes = 20;
    private boolean autoUpdate = false;
    private char mode = SailingSimulatorConstants.ModeEvent;  // default mode: 'e'vent
    private char event = SailingSimulatorConstants.EventKielerWoche; // default event: 'k'ieler woche

    private boolean showArrows = false; // show the wind arrows in wind display and replay modes.    
    private boolean showGrid = true;   // show the "heat map" in the wind display and replay modes.
    private boolean showLines = false;  // show the wind lines in the wind display and replay modes.
    private char seedLines = 'b';  // seed lines at: 'b'ack, 'f'ront
    private boolean showStreamlets = true; // show the wind streamlets in the wind display and replay modes.
    private boolean showStreamlets2 = false; // show animated wind streamlets in the wind display and replay modes.
    private boolean injectWindDataJS = false;
    
    private static Logger logger = Logger.getLogger(SimulatorEntryPoint.class.getName());

    @Override
    protected void doOnModuleLoad() {
        
    	super.doOnModuleLoad();
        
    	checkUrlParameters();

    	if (this.injectWindDataJS) {
    		SimulatorJSBundle bundle = GWT.create(SimulatorJSBundle.class);
    		JavaScriptInjector.inject(bundle.windStreamletsDataJS().getText());
    	}
    	
        createSimulatorPanel();
    }

    private void checkUrlParameters() {
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
        String modeStr = Window.Location.getParameter("mode");
        if (modeStr == null || modeStr.isEmpty()) {
            logger.config("Using default mode " + mode);
        } else {
            mode = modeStr.charAt(0);
            if (mode == SailingSimulatorConstants.ModeMeasured) {
                showArrows = true; // show the wind arrows in wind display and replay modes.    
                showGrid = false;   // show the "heat map" in the wind display and replay modes.
                showLines = false;  // show the wind lines in the wind display and replay modes.
                showStreamlets = false; // show the wind streamlets in the wind display and replay modes.
            }
        }
        String eventStr = Window.Location.getParameter("event");
        if (eventStr == null || eventStr.isEmpty()) {
            logger.config("Using default event: " + event);
        } else {
            event = eventStr.charAt(0);
        }
        String windDisplayStr = Window.Location.getParameter("windDisplay");
        if (windDisplayStr == null || windDisplayStr.isEmpty()) {
            logger.config("Using default showGrid " + showGrid + " & default showArrows " + showArrows);
        } else {
            if (windDisplayStr.contains("g")) {
                showGrid = true;
            } else {
                showGrid = false;
            }
            if (windDisplayStr.contains("l")) {
                showLines = true;
            } else {
                showLines = false;
            }
            if (windDisplayStr.contains("a")) {
                showArrows = true;
            } else {
                showArrows = false;
            }
            if (windDisplayStr.contains("s")) {
                showStreamlets = true;
            } else {
                showStreamlets = false;
            }
            if (windDisplayStr.contains("z")) {
                showStreamlets2 = true;
            } else {
                showStreamlets2 = false;
            }
            if (windDisplayStr.contains("y")) {
                showStreamlets2 = true;
                injectWindDataJS = true;
            }
            if (windDisplayStr.contains("b")) {
                seedLines = 'b';
            }
            if (windDisplayStr.contains("f")) {
                seedLines = 'f';
            }
        }
    }

    private FlowPanel createLogoAndTitlePanel(SimulatorMainPanel simulatorPanel) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(titleName, null, stringMessages, this);
        /*{
            @Override
            public void onResize() {
                super.onResize();
                if (isSmallWidth()) {
                    remove(globalNavigationPanel);
                } else {
                    add(globalNavigationPanel);
                }
            }
        };*/
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");

        return logoAndTitlePanel;
    }

    private void createSimulatorPanel() {
        SimulatorMainPanel simulatorPanel = new SimulatorMainPanel(simulatorSvc, stringMessages, this, xRes, yRes,
                autoUpdate, mode, event, showGrid, showLines, seedLines, showArrows, showStreamlets, showStreamlets2, injectWindDataJS);

        DockLayoutPanel p = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(p);

        // FlowPanel toolbarPanel = new FlowPanel();
        // toolbarPanel.add(simulatorPanel.getNavigationWidget());
        // p.addNorth(toolbarPanel, 40);

        FlowPanel logoAndTitlePanel = createLogoAndTitlePanel(simulatorPanel);

        p.addNorth(logoAndTitlePanel, 68);
        p.add(simulatorPanel);
        p.addStyleName("dockLayoutPanel");
    }

}
