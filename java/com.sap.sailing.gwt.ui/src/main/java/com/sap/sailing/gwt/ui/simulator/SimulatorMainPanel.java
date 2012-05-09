package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO.WindPattern;

public class SimulatorMainPanel extends SplitLayoutPanel {

    private FlowPanel leftPanel = new FlowPanel();
    private FlowPanel rightPanel = new FlowPanel();

    Button updateButton;
    Button courseInputButton;

    RadioButton summaryButton;
    RadioButton replayButton;
    RadioButton windDisplayButton;

    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private PathCanvasOverlay pathCanvasOverlay;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;

    private final StringMessages stringMessages;
    private MapWidget mapw;
    private SimulatorServiceAsync simulatorSvc;

    private ListBox patternSelector = new ListBox();
    private ListBox boatSelector = new ListBox();

    WindFieldGenParamsDTO windParams = new WindFieldGenParamsDTO();

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    /**
     * Temporary place holders for parameters
     */
    private final int xRes = 5;
    private final int yRes = 5;
    private final double windSpeed = 7.2;
    private final double windBearing = 45;

    public SimulatorMainPanel(MapWidget mapw, StringMessages stringMessages, SimulatorServiceAsync svc) {
        // splitPanel = new SplitLayoutPanel();
        super();
        this.mapw = mapw;
        this.stringMessages = stringMessages;
        this.simulatorSvc = svc;

        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.simulator(), stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        this.addNorth(logoAndTitlePanel, 68);

        // leftPanel.getElement().getStyle().setBackgroundColor("#4f4f4f");
        createOptionsPanelTop();
        createOptionsPanel();
        createMapOptionsPanel();

        this.addWest(leftPanel, 400);
        // leftPanel.getElement().getStyle().setFloat(Style.Float.LEFT);
        rightPanel.getElement().getStyle().setBackgroundColor("#e0e0e0");
        this.add(rightPanel);
        // rightPanel.getElement().getStyle().setFloat(Style.Float.RIGHT);
        addOverlays();

    }

    private void createOptionsPanelTop() {
        HorizontalPanel optionsPanel = new HorizontalPanel();
        optionsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        optionsPanel.setTitle("Optionsbar");
        Label options = new Label(stringMessages.optionsBar());
        optionsPanel.setSize("100%", "7%");
        optionsPanel.add(options);
        optionsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        initUpdateButton();
        // updateButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        optionsPanel.add(updateButton);

        leftPanel.add(optionsPanel);
    }

    private void createOptionsPanel() {
        FlowPanel controlPanel = new FlowPanel();
        controlPanel.setSize("100%", "92%");
        controlPanel.setTitle("Control Settings");
        controlPanel.getElement().getStyle().setBackgroundColor(" #e0e0e0");
        String windSetup = stringMessages.wind() + " " + stringMessages.setup();
        Label windSetupLabel = new Label(windSetup);

        controlPanel.add(windSetupLabel);

        Label pattern = new Label(stringMessages.pattern());
        controlPanel.add(pattern);

        simulatorSvc.getWindPatterns(new AsyncCallback<WindPattern[]>() {

            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed to initialize wind patterns\n" + message);

            }

            @Override
            public void onSuccess(WindPattern[] patterns) {
                for (int i = 0; i < patterns.length; ++i) {
                    patternSelector.addItem(patterns[i].toString());
                }
            }

        });
        controlPanel.add(patternSelector);
        leftPanel.add(controlPanel);

    }

    private void createSailingSetup(Panel controlPanel) {

        String sailingSetup = stringMessages.sailing() + " " + stringMessages.setup();
        Label sailingSetupLabel = new Label(sailingSetup);

        controlPanel.add(sailingSetupLabel);

        Label boatClassLabel = new Label(stringMessages.boatClass());
        controlPanel.add(boatClassLabel);

        simulatorSvc.getWindPatterns(new AsyncCallback<WindPattern[]>() {

            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed to initialize wind patterns\n" + message);

            }

            @Override
            public void onSuccess(WindPattern[] patterns) {
                for (int i = 0; i < patterns.length; ++i) {
                    patternSelector.addItem(patterns[i].toString());
                }
            }

        });
        controlPanel.add(patternSelector);
        leftPanel.add(controlPanel);

    }

    private void createMapOptionsPanel() {
        HorizontalPanel mapOptions = new HorizontalPanel();
        mapOptions.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        mapOptions.setSize("100%", "7%");
        mapOptions.setTitle("Maps");
        Label mapsLabel = new Label(stringMessages.maps());
        mapOptions.add(mapsLabel);

        mapOptions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        initCourseInputButton();

        mapOptions.add(courseInputButton);
        rightPanel.add(mapOptions);

        initDisplayOptions(mapOptions);

        FlowPanel mapPanel = new FlowPanel();
        mapPanel.setTitle("Map");

        mapPanel.add(mapw);
        // mapPanel.setSize("100%", "80%");
        rightPanel.add(mapPanel);
    }

    private void addOverlays() {
        raceCourseCanvasOverlay = new RaceCourseCanvasOverlay();
        mapw.addOverlay(raceCourseCanvasOverlay);

        windFieldCanvasOverlay = new WindFieldCanvasOverlay();
        // mapw.addOverlay(windFieldCanvasOverlay);
        pathCanvasOverlay = new PathCanvasOverlay();
    }

    private void generateWindField() {
        logger.info("In generateWindField");

        PositionDTO startPointDTO = new PositionDTO(raceCourseCanvasOverlay.startPoint.getLatitude(),
                raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO = new PositionDTO(raceCourseCanvasOverlay.endPoint.getLatitude(),
                raceCourseCanvasOverlay.endPoint.getLongitude());
        logger.info("StartPoint:" + startPointDTO);
        windParams.setNorthWest(startPointDTO);
        windParams.setSouthEast(endPointDTO);
        windParams.setxRes(xRes);
        windParams.setyRes(yRes);
        windParams.setWindBearing(windBearing);
        windParams.setWindSpeed(windSpeed);

        simulatorSvc.getWindField(windParams, new AsyncCallback<WindFieldDTO>() {
            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed servlet call to SimulatorService\n" + message);
            }

            @Override
            public void onSuccess(WindFieldDTO wl) {
                logger.info("Number of windDTO : " + wl.getMatrix().size());
                refreshWindFieldOverlay(wl);
            }
        });

    }

    private void refreshWindFieldOverlay(WindFieldDTO wl) {
        windFieldCanvasOverlay.setWindField(wl);
        windFieldCanvasOverlay.redraw(true);
    }

    private void generatePath() {
        logger.info("In generatePath");

        PositionDTO startPointDTO = new PositionDTO(raceCourseCanvasOverlay.startPoint.getLatitude(),
                raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO = new PositionDTO(raceCourseCanvasOverlay.endPoint.getLatitude(),
                raceCourseCanvasOverlay.endPoint.getLongitude());

        windParams.setNorthWest(startPointDTO);
        windParams.setSouthEast(endPointDTO);
        windParams.setxRes(xRes);
        windParams.setyRes(yRes);
        windParams.setWindBearing(windBearing);
        windParams.setWindSpeed(windSpeed);

        simulatorSvc.getPaths(windParams, new AsyncCallback<PathDTO[]>() {
            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed servlet call to SimulatorService\n" + message);
            }

            @Override
            public void onSuccess(PathDTO[] paths) {
                logger.info("Number of Paths : " + paths.length);
                /* TODO Revisit for now creating a WindFieldDTO from the path */
                WindFieldDTO pathWindDTO = new WindFieldDTO();
                pathWindDTO.setMatrix(paths[0].getMatrix());
                pathCanvasOverlay.setWindField(pathWindDTO);
                pathCanvasOverlay.redraw(true);

            }
        });

    }

    private void initUpdateButton() {
        updateButton = new Button(stringMessages.update());
        updateButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if (raceCourseCanvasOverlay.isCourseSet()) {
                    if (windDisplayButton.getValue()) {
                        refreshWindDisplayView();
                    } else if (summaryButton.getValue()) {
                        refreshSummaryView();
                    } else if (replayButton.getValue()) {
                        refreshReplayView();
                    }
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }

            }
        });

    }

    private void initCourseInputButton() {
        courseInputButton = new Button(stringMessages.startEnd());

        courseInputButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {

                mapw.removeOverlay(windFieldCanvasOverlay);
                mapw.removeOverlay(pathCanvasOverlay);
                // raceCourseCanvasOverlay.setSelected(true);
                // raceCourseCanvasOverlay.setVisible(true);
                raceCourseCanvasOverlay.reset();
                raceCourseCanvasOverlay.redraw(true);

            }
        });

    }

    private void initDisplayOptions(Panel mapOptions) {
        summaryButton = new RadioButton("Map Display Options", stringMessages.summary());

        summaryButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (raceCourseCanvasOverlay.isCourseSet()) {
                    refreshSummaryView();
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }
            }

        });

        replayButton = new RadioButton("Map Display Options", stringMessages.replay());

        replayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (raceCourseCanvasOverlay.isCourseSet()) {
                    refreshReplayView();
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }
            }

        });

        windDisplayButton = new RadioButton("Map Display Options", stringMessages.wind() + " "
                + stringMessages.display());
        windDisplayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if (raceCourseCanvasOverlay.isCourseSet()) {
                    refreshWindDisplayView();
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }
            }

        });

        HorizontalPanel p = new HorizontalPanel();

        DecoratorPanel d = new DecoratorPanel();
        p.add(summaryButton);
        p.add(replayButton);
        p.add(windDisplayButton);
        // windDisplayButton.setValue(true);
        d.add(p);
        mapOptions.add(d);

    }

    private void refreshSummaryView() {
        mapw.removeOverlay(windFieldCanvasOverlay);
        pathCanvasOverlay.displayWindAlongPath = true;
        mapw.addOverlay(pathCanvasOverlay);
        generatePath();
    }

    private void refreshReplayView() {
        mapw.addOverlay(windFieldCanvasOverlay);
        pathCanvasOverlay.displayWindAlongPath = false;
        mapw.addOverlay(pathCanvasOverlay);
        generateWindField();
        generatePath();
    }

    private void refreshWindDisplayView() {
        mapw.removeOverlay(pathCanvasOverlay);
        mapw.addOverlay(windFieldCanvasOverlay);
        generateWindField();
    }
}
