package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RequiresDataInitialization;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListenerWithStoppingCriteria;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.panels.SimpleBusyIndicator;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.simulator.util.ColorPalette;
import com.sap.sailing.gwt.ui.simulator.util.ColorPaletteGenerator;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;

public class SimulatorMap extends AbsolutePanel implements RequiresDataInitialization, TimeListenerWithStoppingCriteria {

    private MapWidget mapw;
    private boolean dataInitialized;
    private boolean overlaysInitialized;
    private WindFieldGenParamsDTO windParams;
    private WindStreamletsCanvasOverlay windStreamletsCanvasOverlay;
    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private WindGridCanvasOverlay windGridCanvasOverlay;
    private WindLineCanvasOverlay windLineCanvasOverlay;
    private List<PathCanvasOverlay> replayPathCanvasOverlays;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;
    private PathLegendCanvasOverlay legendCanvasOverlay;
    private List<TimeListenerWithStoppingCriteria> timeListeners;
    private SimulatorServiceAsync simulatorSvc;
    private StringMessages stringMessages;
    private ErrorReporter errorReporter;
    private Timer timer;
    private SimulatorTimePanel timePanel;
    private SimpleBusyIndicator busyIndicator;
    private char mode;
    private ColorPalette colorPalette;
    private int xRes;
    private int yRes;
    private boolean warningAlreadyShown = false;
    private SimulatorMainPanel parent = null;
    private PathPolyline pathPolyline = null;
    private static Logger LOGGER = Logger.getLogger(SimulatorMap.class.getName());
    private static boolean SHOW_ONLY_PATH_POLYLINE = false;

    public enum ViewName {
        SUMMARY, REPLAY, WINDDISPLAY
    }

    private class ResultManager implements AsyncCallback<SimulatorResultsDTO> {

        private boolean summaryView;

        public ResultManager(boolean summaryView) {
            this.summaryView = summaryView;
        }

        @Override
        public void onFailure(Throwable message) {
            errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
        }

        @Override
        public void onSuccess(SimulatorResultsDTO result) {

            String notificationMessage = result.getNotificationMessage();
            if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                errorReporter.reportNotification(notificationMessage);
                warningAlreadyShown = true;
            }

            PathDTO[] paths = result.getPaths();
            LOGGER.info("Number of Paths : " + paths.length);
            long startTime = paths[0].getPoints().get(0).timepoint;
            long maxDurationTime = 0;

            if (mode == SailingSimulatorUtil.measured) {
                PositionDTO pos = result.getRaceCourse().coursePositions.waypointPositions.get(0);
                raceCourseCanvasOverlay.startPoint = LatLng.newInstance(pos.latDeg, pos.lngDeg);
                pos = result.getRaceCourse().coursePositions.waypointPositions.get(1);
                raceCourseCanvasOverlay.endPoint = LatLng.newInstance(pos.latDeg, pos.lngDeg);
            }

            raceCourseCanvasOverlay.redraw(true);
            removeOverlays();
            // pathCanvasOverlays.clear();
            replayPathCanvasOverlays.clear();
            colorPalette.reset();

            PathDTO currentPath = null;
            String color = null;
            int noOfPaths = paths.length;

            for (int index = 0; index < noOfPaths; ++index) {

                currentPath = paths[index];
                color = colorPalette.getColor(noOfPaths - 1 - index);

                if (currentPath.name.equals("Polyline")) {
                    pathPolyline = createPathPolyline(currentPath);
                }
                else {

                    /* TODO Revisit for now creating a WindFieldDTO from the path */
                    WindFieldDTO pathWindDTO = new WindFieldDTO();
                    pathWindDTO.setMatrix(currentPath.getPoints());

                    ReplayPathCanvasOverlay replayPathCanvasOverlay = new ReplayPathCanvasOverlay(currentPath.name, timer);
                    replayPathCanvasOverlays.add(replayPathCanvasOverlay);
                    replayPathCanvasOverlay.pathColor = color;

                    if (this.summaryView) {

                        replayPathCanvasOverlay.displayWindAlongPath = true;
                        timer.removeTimeListener(replayPathCanvasOverlay);
                        replayPathCanvasOverlay.setTimer(null);
                    }
                    if (SHOW_ONLY_PATH_POLYLINE == false) {
                        mapw.addOverlay(replayPathCanvasOverlay);
                    }
                    replayPathCanvasOverlay.setWindField(pathWindDTO);
                    replayPathCanvasOverlay.setRaceCourse(raceCourseCanvasOverlay.startPoint, raceCourseCanvasOverlay.endPoint);
                    if (index == 0) {
                        replayPathCanvasOverlay.setCurrent(result.getWindField().curSpeed,result.getWindField().curBearing);
                    } else {
                        replayPathCanvasOverlay.setCurrent(-1.0,0.0);
                    }
                    if (SHOW_ONLY_PATH_POLYLINE == false) {
                        replayPathCanvasOverlay.redraw(true);
                    }
                    legendCanvasOverlay.setPathOverlays(replayPathCanvasOverlays);

                    long tmpDurationTime = currentPath.getPathTime();
                    if (tmpDurationTime > maxDurationTime) {
                        maxDurationTime = tmpDurationTime;
                    }
                }
            }

            if (timePanel != null) {
                timePanel.setMinMax(new Date(startTime), new Date(startTime + maxDurationTime), true);
                timePanel.resetTimeSlider();
            }

            /**
             * Now we always get the wind field
             */
            WindFieldDTO windFieldDTO = result.getWindField();
            LOGGER.info("Number of windDTO : " + windFieldDTO.getMatrix().size());

            if (windParams.isShowGrid()) {
                mapw.addOverlay(windGridCanvasOverlay);
            }
            if (windParams.isShowLines()) {
                mapw.addOverlay(windLineCanvasOverlay);
            }
            if (windParams.isShowArrows()) {
                mapw.addOverlay(windFieldCanvasOverlay);
            }
            if (windParams.isShowStreamlets()) {
                mapw.addOverlay(windStreamletsCanvasOverlay);
            }

            refreshWindFieldOverlay(windFieldDTO);

            timeListeners.clear();
            if (windParams.isShowArrows()) {
                timeListeners.add(windFieldCanvasOverlay);
            }
            if (windParams.isShowStreamlets()) {
                timeListeners.add(windStreamletsCanvasOverlay);
            }
            if (windParams.isShowGrid()) {
                timeListeners.add(windGridCanvasOverlay);
            }
            if (windParams.isShowLines()) {
                timeListeners.add(windLineCanvasOverlay);
            }
            for (int i = 0; i < replayPathCanvasOverlays.size(); ++i) {
                timeListeners.add(replayPathCanvasOverlays.get(i));
            }

            if (this.summaryView) {
                if (windFieldCanvasOverlay != null) {
                    windFieldCanvasOverlay.setVisible(false);
                }
                if (windGridCanvasOverlay != null) {
                    windGridCanvasOverlay.setVisible(false);
                }
                if (windLineCanvasOverlay != null) {
                    windLineCanvasOverlay.setVisible(false);
                }
                if (windStreamletsCanvasOverlay != null) {
                    windStreamletsCanvasOverlay.setVisible(false);
                }
            }

            mapw.addOverlay(legendCanvasOverlay);
            legendCanvasOverlay.setVisible(true);
            legendCanvasOverlay.redraw(true);

            busyIndicator.setBusy(false);
        }

    }

    public SimulatorMap(SimulatorServiceAsync simulatorSvc, StringMessages stringMessages, ErrorReporter errorReporter, int xRes, int yRes, Timer timer,
            WindFieldGenParamsDTO windParams, SimpleBusyIndicator busyIndicator, char mode,
            SimulatorMainPanel parent) {
        this.simulatorSvc = simulatorSvc;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.xRes = xRes;
        this.yRes = yRes;
        this.timer = timer;
        this.timePanel = null;
        timer.addTimeListener(this);
        this.windParams = windParams;
        this.busyIndicator = busyIndicator;
        this.mode = mode;
        this.colorPalette = new ColorPaletteGenerator();
        this.dataInitialized = false;
        this.overlaysInitialized = false;
        this.windFieldCanvasOverlay = null;
        this.windGridCanvasOverlay = null;
        this.windLineCanvasOverlay = null;
        this.replayPathCanvasOverlays = null;
        this.raceCourseCanvasOverlay = null;
        this.timeListeners = new LinkedList<TimeListenerWithStoppingCriteria>();
        this.initializeData();
        this.parent = parent;
    }

    public SimulatorMap(SimulatorServiceAsync simulatorSvc, StringMessages stringMessages, ErrorReporter errorReporter, int xRes, int yRes, Timer timer,
            SimulatorTimePanel timePanel, WindFieldGenParamsDTO windParams, SimpleBusyIndicator busyIndicator, char mode, SimulatorMainPanel parent) {
        this.simulatorSvc = simulatorSvc;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.xRes = xRes;
        this.yRes = yRes;
        this.timer = timer;
        this.timePanel = timePanel;
        timer.addTimeListener(this);
        this.windParams = windParams;
        this.busyIndicator = busyIndicator;
        this.mode = mode;
        this.colorPalette = new ColorPaletteGenerator();
        this.dataInitialized = false;
        this.overlaysInitialized = false;
        this.windFieldCanvasOverlay = null;
        this.windGridCanvasOverlay = null;
        this.windLineCanvasOverlay = null;
        this.replayPathCanvasOverlays = null;
        this.raceCourseCanvasOverlay = null;
        this.timeListeners = new LinkedList<TimeListenerWithStoppingCriteria>();
        this.initializeData();
        this.parent = parent;
    }

    private void loadMapsAPI() {
        /*
         * Asynchronously loads the Maps API.
         * 
         * The first parameter should be a valid Maps API Key to deploy this application on a public server, but a blank
         * key will work for an application served from localhost.
         */
        Maps.loadMapsApi("", "2", false, new Runnable() {
            @Override
            public void run() {
                mapw = new MapWidget();
                // mapw.setUI(SimulatorMapOptions.newInstance());
                mapw.setZoomLevel(13);
                // mapw.setSize("100%", "650px");
                // mapw.setSize("100%", "80%");

                mapw.addControl(new LargeMapControl3D(), new ControlPosition(ControlAnchor.TOP_RIGHT, /* offsetX */0, /* offsetY */
                        30));
                mapw.addControl(new MenuMapTypeControl());
                mapw.addControl(new ScaleControl(), new ControlPosition(ControlAnchor.BOTTOM_RIGHT, /* offsetX */10, /* offsetY */
                        20));
                // Add the map to the HTML host page
                mapw.setScrollWheelZoomEnabled(true);
                // mapw.setContinuousZoom(true);
                mapw.setTitle(stringMessages.simulator() + " " + stringMessages.map());
                // PositionDTO kiel = new PositionDTO(54.46195148135232, 10.1513671875);
                PositionDTO trave = new PositionDTO(54.007063, 10.838356); // 53.978276,10.880156);//53.968015,10.891331);
                LatLng position = LatLng.newInstance(trave.latDeg, trave.lngDeg);
                mapw.panTo(position);
                // mapw.panTo(LatLng.newInstance(0, 0));
                add(mapw, 0, 0);
                mapw.setSize("100%", "100%");

                dataInitialized = true;
            }
        });
    }

    private void initializeOverlays() {
        this.raceCourseCanvasOverlay = new RaceCourseCanvasOverlay();
        this.raceCourseCanvasOverlay.getCanvas().getElement().setClassName("raceCourse");
        // System.out.println("RaceCourseCanvasOverlay z-index: " +
        // this.raceCourseCanvasOverlay.getCanvas().getElement().getStyle().getZIndex());
        this.mapw.addOverlay(this.raceCourseCanvasOverlay);

        if (this.windParams.isShowArrows()) {
            this.windFieldCanvasOverlay = new WindFieldCanvasOverlay(this.timer);
        }
        if (this.windParams.isShowStreamlets()) {
            this.windStreamletsCanvasOverlay = new WindStreamletsCanvasOverlay(this.timer);
        }
        if (this.windParams.isShowGrid()) {
            this.windGridCanvasOverlay = new WindGridCanvasOverlay(this.timer, this.xRes, this.yRes);
        }
        if (windParams.isShowLines()) {
            this.windLineCanvasOverlay = new WindLineCanvasOverlay(this.timer);
        }
        // mapw.addOverlay(windFieldCanvasOverlay);
        // pathCanvasOverlays = new ArrayList<PathCanvasOverlay>();
        this.replayPathCanvasOverlays = new ArrayList<PathCanvasOverlay>();
        // timeListeners.add(replayPathCanvasOverlay);
        this.legendCanvasOverlay = new PathLegendCanvasOverlay();

        this.overlaysInitialized = true;
    }

    private void generateWindField(WindPatternDisplay windPatternDisplay, final boolean removeOverlays) {
        LOGGER.info("In generateWindField");
        if (windPatternDisplay == null) {
            this.errorReporter.reportError("Please select a valid wind pattern.");
            return;
        }
        PositionDTO startPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.startPoint.getLatitude(),
                this.raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.endPoint.getLatitude(),
                this.raceCourseCanvasOverlay.endPoint.getLongitude());
        LOGGER.info("StartPoint:" + startPointDTO);
        this.windParams.setNorthWest(startPointDTO);
        this.windParams.setSouthEast(endPointDTO);
        this.windParams.setxRes(this.xRes);
        this.windParams.setyRes(this.yRes);
        this.busyIndicator.setBusy(true);
        this.simulatorSvc.getWindField(this.windParams, windPatternDisplay, new AsyncCallback<WindFieldDTO>() {
            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
            }

            @Override
            public void onSuccess(WindFieldDTO wl) {
                if (removeOverlays) {
                    removeOverlays();
                }
                LOGGER.info("Number of windDTO : " + wl.getMatrix().size());
                // Window.alert("Number of windDTO : " + wl.getMatrix().size());
                if (windParams.isShowGrid()) {
                    mapw.addOverlay(windGridCanvasOverlay);
                }
                if (windParams.isShowLines()) {
                    mapw.addOverlay(windLineCanvasOverlay);
                }
                if (windParams.isShowArrows()) {
                    mapw.addOverlay(windFieldCanvasOverlay);
                }
                if (windParams.isShowStreamlets()) {
                    mapw.addOverlay(windStreamletsCanvasOverlay);
                }
                refreshWindFieldOverlay(wl);
                timeListeners.clear();
                if (windParams.isShowArrows()) {
                    timeListeners.add(windFieldCanvasOverlay);
                }
                if (windParams.isShowStreamlets()) {
                    timeListeners.add(windStreamletsCanvasOverlay);
                }
                if (windParams.isShowGrid()) {
                    timeListeners.add(windGridCanvasOverlay);
                }
                if (windParams.isShowLines()) {
                    timeListeners.add(windLineCanvasOverlay);
                }
                timePanel.setMinMax(windParams.getStartTime(), windParams.getEndTime(), true);
                timePanel.resetTimeSlider();

                busyIndicator.setBusy(false);
            }
        });

    }

    private void refreshWindFieldOverlay(WindFieldDTO wl) {
        if (this.windFieldCanvasOverlay != null) {
            this.windFieldCanvasOverlay.setWindField(wl);
        }
        if (this.windStreamletsCanvasOverlay != null) {
            this.windStreamletsCanvasOverlay.setWindField(wl);
        }
        if (this.windGridCanvasOverlay != null) {
            this.windGridCanvasOverlay.setWindField(wl);
        }

        if (this.windLineCanvasOverlay != null) {
            this.windLineCanvasOverlay.setWindLinesDTO(wl.getWindLinesDTO());
            if (this.windGridCanvasOverlay != null) {
                this.windLineCanvasOverlay.setGridCorners(this.windGridCanvasOverlay.getGridCorners());
            }
        }

        this.timer.setTime(this.windParams.getStartTime().getTime());
        if (this.windParams.isShowArrows()) {
            this.windFieldCanvasOverlay.redraw(true);
        }
        if (this.windParams.isShowStreamlets()) {
            this.windStreamletsCanvasOverlay.redraw(true);
        }
        if (this.windParams.isShowGrid()) {
            this.windGridCanvasOverlay.redraw(true);
        }
        if (this.windParams.isShowLines()) {
            this.windLineCanvasOverlay.redraw(true);
        }
    }

    private void generatePath(WindPatternDisplay windPatternDisplay, boolean summaryView, int boatClassIndex) {
        LOGGER.info("In generatePath");

        if (windPatternDisplay == null) {
            this.errorReporter.reportError("Please select a valid wind pattern.");
            return;
        }

        if (this.mode != SailingSimulatorUtil.measured) {
            PositionDTO startPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.startPoint.getLatitude(),
                    this.raceCourseCanvasOverlay.startPoint.getLongitude());
            this.windParams.setNorthWest(startPointDTO);

            PositionDTO endPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.endPoint.getLatitude(),
                    this.raceCourseCanvasOverlay.endPoint.getLongitude());
            this.windParams.setSouthEast(endPointDTO);
        }

        this.windParams.setxRes(this.xRes);
        this.windParams.setyRes(this.yRes);

        this.busyIndicator.setBusy(true);

        this.simulatorSvc.getSimulatorResults(this.mode, this.windParams, windPatternDisplay, true, boatClassIndex, new ResultManager(summaryView));
    }

    private boolean isCourseSet() {
        return this.raceCourseCanvasOverlay.isCourseSet();
    }

    public void reset() {
        if (!this.overlaysInitialized) {
            this.initializeOverlays();
        } else {
            this.removeOverlays();
        }
        this.mapw.setDoubleClickZoom(false);
        this.raceCourseCanvasOverlay.setSelected(true);
        // raceCourseCanvasOverlay.setVisible(true);
        this.raceCourseCanvasOverlay.reset();
        this.raceCourseCanvasOverlay.redraw(true);
    }

    public void removeOverlays() {
        if (this.overlaysInitialized) {
            int num = 0; // tracking for debugging only
            if (this.windFieldCanvasOverlay != null) {
                this.mapw.removeOverlay(this.windFieldCanvasOverlay);
                num++;
            }
            if (this.windStreamletsCanvasOverlay != null) {
                this.mapw.removeOverlay(this.windStreamletsCanvasOverlay);
                num++;
            }
            if (this.windGridCanvasOverlay != null) {
                this.mapw.removeOverlay(this.windGridCanvasOverlay);
                num++;
            }
            if (this.windLineCanvasOverlay != null) {
                this.mapw.removeOverlay(this.windLineCanvasOverlay);
                num++;
            }

            for (int i = 0; i < this.replayPathCanvasOverlays.size(); ++i) {
                this.mapw.removeOverlay(this.replayPathCanvasOverlays.get(i));
                num++;
            }
            this.mapw.removeOverlay(this.legendCanvasOverlay);
            LOGGER.info("Removed " + num + " overlays");
        }
    }

    private void refreshSummaryView(WindPatternDisplay windPatternDisplay, int boatClassIndex, boolean force) {
        // removeOverlays();
        if (force) {
            this.generatePath(windPatternDisplay, true, boatClassIndex);
        } else {
            if (this.replayPathCanvasOverlays != null && !this.replayPathCanvasOverlays.isEmpty()) {
                System.out.println("Soft refresh");
                for (PathCanvasOverlay r : this.replayPathCanvasOverlays) {
                    r.displayWindAlongPath = true;
                    this.timer.removeTimeListener(r);
                    r.setTimer(null);
                    r.setVisible(true);
                    r.redraw(true);
                }
                this.legendCanvasOverlay.setVisible(true);
                this.legendCanvasOverlay.redraw(true);
                if (this.windFieldCanvasOverlay != null) {
                    this.windFieldCanvasOverlay.setVisible(false);
                }
                if (this.windGridCanvasOverlay != null) {
                    this.windGridCanvasOverlay.setVisible(false);
                }
                if (this.windLineCanvasOverlay != null) {
                    this.windLineCanvasOverlay.setVisible(false);
                }
            } else {
                this.generatePath(windPatternDisplay, true, boatClassIndex);
            }
        }
    }

    private void refreshReplayView(WindPatternDisplay windPatternDisplay, int boatClassIndex, boolean force) {
        // removeOverlays();
        if (force) {
            this.generatePath(windPatternDisplay, false, boatClassIndex);
        } else {

            if (this.replayPathCanvasOverlays != null && !this.replayPathCanvasOverlays.isEmpty()) {
                System.out.println("Soft refresh");
                for (PathCanvasOverlay r : this.replayPathCanvasOverlays) {
                    r.displayWindAlongPath = false;
                    r.setTimer(this.timer);
                    this.timer.addTimeListener(r);
                    r.setVisible(true);
                    r.redraw(true);
                }
                this.legendCanvasOverlay.setVisible(true);
                this.legendCanvasOverlay.redraw(true);
                if (this.windFieldCanvasOverlay != null) {
                    this.windFieldCanvasOverlay.setVisible(true);
                }
                if (this.windGridCanvasOverlay != null) {
                    this.windGridCanvasOverlay.setVisible(true);
                }
                if (this.windLineCanvasOverlay != null) {
                    this.windLineCanvasOverlay.setVisible(true);
                }
            } else {
                this.generatePath(windPatternDisplay, false, boatClassIndex);
            }
        }
    }

    private void refreshWindDisplayView(WindPatternDisplay windPatternDisplay, boolean force) {

        if (force) {
            // removeOverlays();
            this.windParams.setDefaultTimeSettings();
            this.generateWindField(windPatternDisplay, true);
            // timeListeners.clear();
            // timeListeners.add(windFieldCanvasOverlay);
        } else {

            if (this.replayPathCanvasOverlays != null && !this.replayPathCanvasOverlays.isEmpty()) {
                System.out.println("Soft refresh");
                for (PathCanvasOverlay r : this.replayPathCanvasOverlays) {
                    r.setVisible(false);
                }
                this.legendCanvasOverlay.setVisible(false);
                if (this.windFieldCanvasOverlay != null) {
                    this.windFieldCanvasOverlay.setVisible(true);
                    this.windFieldCanvasOverlay.redraw(true);
                }
                if (this.windGridCanvasOverlay != null) {
                    this.windGridCanvasOverlay.setVisible(true);
                    this.windGridCanvasOverlay.redraw(true);
                }
                if (this.windLineCanvasOverlay != null) {
                    this.windLineCanvasOverlay.setVisible(true);
                    this.windLineCanvasOverlay.redraw(true);
                }
            } else {
                this.windParams.setDefaultTimeSettings();
                this.generateWindField(windPatternDisplay, true);
            }
        }
    }

    public void refreshView(ViewName name, WindPatternDisplay windPatternDisplay, int boatClassIndex, boolean force) {
        if (!this.overlaysInitialized) {
            this.initializeOverlays();
        }
        if ((this.isCourseSet()) || (this.mode == SailingSimulatorUtil.measured)) {
            this.mapw.setDoubleClickZoom(true);
            this.raceCourseCanvasOverlay.setSelected(false);
            this.windParams.setKeepState(!force);
            if (force) {
                if (this.replayPathCanvasOverlays != null) {
                    this.removeOverlays();
                    this.timeListeners.clear();
                    this.replayPathCanvasOverlays.clear();
                }
            }
            switch (name) {
            case SUMMARY:
                this.refreshSummaryView(windPatternDisplay, boatClassIndex, force);
                break;
            case REPLAY:
                this.refreshReplayView(windPatternDisplay, boatClassIndex, force);
                break;
            case WINDDISPLAY:
                this.refreshWindDisplayView(windPatternDisplay, force);
                break;
            default:
                break;
            }

            if (this.mode == SailingSimulatorUtil.measured && this.pathPolyline != null) {
                this.pathPolyline.setBoatClassID(boatClassIndex);
            }

        } else {
            Window.alert("No course set, please initialize the course with Start-End input");
        }
    }

    @Override
    public void initializeData() {
        this.loadMapsAPI();
    }

    @Override
    public boolean isDataInitialized() {
        return this.dataInitialized;
    }

    @Override
    public void timeChanged(Date date) {
        if (this.stop() == 0) {
            LOGGER.info("Stopping the timer");
            this.timer.stop();
        }
    }

    @Override
    public int stop() {
        int value = 0;
        for (TimeListenerWithStoppingCriteria t : this.timeListeners) {
            value += t.stop();
        }
        return value;
    }

    private PathPolyline createPathPolyline(PathDTO pathDTO) {

        int boatClassID = parent.getBoatClassID();

        // List<SimulatorWindDTO> path = new ArrayList<SimulatorWindDTO>();
        // path.add(new SimulatorWindDTO(4.166666, 1.5, 4, 350, 1000, true));
        // path.add(new SimulatorWindDTO(3.166666, 0.666666, 4, 350, 1000, true));
        // path.add(new SimulatorWindDTO(2.666666, 3.666666, 4, 350, 1000, true));
        // path.add(new SimulatorWindDTO(1, 3, 4, 350, 1000, true));
        // path.add(new SimulatorWindDTO(0.333333, 3.333333, 4, 350, 1000, true));
        // path.add(new SimulatorWindDTO(0.5, 4, 4, 350, 1000, true));

        return PathPolyline.createPathPolyline(
                // path,
                pathDTO.getPoints(),
                this.errorReporter, this.simulatorSvc, this.mapw, this, boatClassID);
    }

    public void addLegendOverlayForPathPolyline(long totalTimeMilliseconds) {

        PathCanvasOverlay overlay = new PathCanvasOverlay("Polyline", totalTimeMilliseconds);
        overlay.pathColor = PathPolyline.DEFAULT_COLOR;

        this.legendCanvasOverlay.addPathOverlay(overlay);
    }

    public void redrawLegendCanvasOverlay() {
        this.legendCanvasOverlay.setVisible(true);
        if (SHOW_ONLY_PATH_POLYLINE == false) {
            this.legendCanvasOverlay.redraw(true);
        }
    }

}
