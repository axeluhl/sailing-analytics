package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;

public class SimulatorEntryPoint2 extends AbstractEntryPoint {

    private String titleName;
    //private String rightLabelName;
    private SimulatorViewModes viewMode;

    private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);
    private int xRes = 5;
    private int yRes = 5;
    private boolean autoUpdate = false;
    private char mode = 'f';
    private boolean showGrid = false;
   
    private static Logger logger = Logger.getLogger(SimulatorEntryPoint.class.getName());

    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        titleName = "Strategy Simulator";
        //rightLabelName = "My Race";
        viewMode = SimulatorViewModes.ONESCREEN;
        
        checkUrlParameters();

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
        }
        String showGridStr = Window.Location.getParameter("showGrid");
        if (showGridStr == null || showGridStr.isEmpty()) {
            logger.config("Using default showGrid " + showGrid);
        } else {
            showGrid = Boolean.parseBoolean(showGridStr);
        }
        SimulatorMainPanel2 mainPanel = new SimulatorMainPanel2(simulatorSvc, stringMessages, this, xRes, yRes,
                autoUpdate, mode, showGrid);

        switch (viewMode) {
        case ONESCREEN:
            createRaceBoardInOneScreenMode(mainPanel);
            break;
        }
        
    }

    private FlowPanel createTimePanel(SimulatorMainPanel2 simulatorPanel) {

        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(simulatorPanel.getTimeWidget());

        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");

        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");

        return timelinePanel;
    }

    private FlowPanel createLogoAndTitlePanel(SimulatorMainPanel2 simulatorPanel) {

        //LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(titleName, rightLabelName, stringMessages);
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(titleName, null, stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");

        return logoAndTitlePanel;
    }

    private void createRaceBoardInOneScreenMode(SimulatorMainPanel2 simulatorPanel) {

        DockLayoutPanel p = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(p);

        // FlowPanel toolbarPanel = new FlowPanel();
        // toolbarPanel.add(simulatorPanel.getNavigationWidget());
        // p.addNorth(toolbarPanel, 40);

        FlowPanel logoAndTitlePanel = createLogoAndTitlePanel(simulatorPanel);
        FlowPanel timePanel = createTimePanel(simulatorPanel);

        p.addNorth(logoAndTitlePanel, 68);
        p.addSouth(timePanel, 90);
        p.add(simulatorPanel);
        p.addStyleName("dockLayoutPanel");

    }

}
