package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Collections;
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
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
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
    private final WindFieldGenParamsDTO windParams;
    private WindStreamletsCanvasOverlay windStreamletsCanvasOverlay;
    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private WindGridCanvasOverlay windGridCanvasOverlay;
    private WindLineCanvasOverlay windLineCanvasOverlay;
    // private List<PathCanvasOverlay> pathCanvasOverlays;
    private List<PathCanvasOverlay> replayPathCanvasOverlays;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;
    private PathLegendCanvasOverlay legendCanvasOverlay;

    private final List<TimeListenerWithStoppingCriteria> timeListeners;

    private final SimulatorServiceAsync simulatorSvc;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final Timer timer;
    private final SimulatorTimePanel timePanel;
    private final SimpleBusyIndicator busyIndicator;
    private final char mode;

    private final ColorPalette colorPalette;

    private static Logger logger = Logger.getLogger(SimulatorMap.class.getName());

    private final int xRes;
    private final int yRes;

    private boolean warningAlreadyShown = false;
    private boolean firstTime = true;

    private PathDTO gpsPoly = null;
    private PathDTO gpsTrack = null;

    public enum ViewName {
        SUMMARY, REPLAY, WINDDISPLAY
    }

    private class ResultManager implements AsyncCallback<SimulatorResultsDTOAndNotificationMessage> {

        private final boolean summaryView;

        /*
         * private class SortByTimeAsc implements Comparator<PathDTO> {
         * 
         * @Override public int compare(PathDTO o1, PathDTO o2) { return (int) (o1.getPathTime() - o2.getPathTime()); }
         * 
         * }
         */

        public ResultManager(final boolean summaryView) {
            this.summaryView = summaryView;
        }

        @Override
        public void onFailure(final Throwable message) {
            errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
        }

        @Override
        public void onSuccess(final SimulatorResultsDTOAndNotificationMessage result) {

            final String notificationMessage = result.getNotificationMessage();
            if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                errorReporter.reportNotification(notificationMessage);
                warningAlreadyShown = true;
            }

            final SimulatorResultsDTO simulatorResult = result.getSimulatorResultsDTO();
            final PathDTO[] paths = simulatorResult.paths;
            logger.info("Number of Paths : " + paths.length);
            // SortByTimeAsc sorter = new SortByTimeAsc();
            // Arrays.sort(paths, sorter);
            final long startTime = paths[0].getMatrix().get(0).timepoint;
            long maxDurationTime = 0;

            if (mode == SailingSimulatorUtil.measured) {
                PositionDTO pos = simulatorResult.raceCourse.coursePositions.waypointPositions.get(0);
                raceCourseCanvasOverlay.startPoint = LatLng.newInstance(pos.latDeg, pos.lngDeg);
                pos = simulatorResult.raceCourse.coursePositions.waypointPositions.get(1);
                raceCourseCanvasOverlay.endPoint = LatLng.newInstance(pos.latDeg, pos.lngDeg);
            }

            raceCourseCanvasOverlay.redraw(true);
            removeOverlays();
            // pathCanvasOverlays.clear();
            replayPathCanvasOverlays.clear();
            colorPalette.reset();

            String pathName = null;
            PathDTO currentPath = null;
            String color = null;

            for (int index = 0; index < paths.length; ++index) {

                currentPath = paths[index];
                pathName = paths[index].name;
                color = colorPalette.getColor(paths.length - 1 - index);

                if (pathName.equals("GPS Track")) {
                    gpsTrack = currentPath;
                }
                else if (pathName.equals("GPS Poly")) {
                    gpsPoly = currentPath;
                }

                if (firstTime && gpsTrack != null && gpsPoly != null) {
                    createPathPolyline(identifyPath(gpsTrack, gpsPoly));
                    firstTime = false;
                }


                /* TODO Revisit for now creating a WindFieldDTO from the path */
                final WindFieldDTO pathWindDTO = new WindFieldDTO();
                pathWindDTO.setMatrix(currentPath.getMatrix());

                final ReplayPathCanvasOverlay replayPathCanvasOverlay = new ReplayPathCanvasOverlay(pathName, timer);
                replayPathCanvasOverlays.add(replayPathCanvasOverlay);
                replayPathCanvasOverlay.pathColor = color;

                if (this.summaryView) {

                    replayPathCanvasOverlay.displayWindAlongPath = true;
                    timer.removeTimeListener(replayPathCanvasOverlay);
                    replayPathCanvasOverlay.setTimer(null);
                }
                mapw.addOverlay(replayPathCanvasOverlay);
                replayPathCanvasOverlay.setWindField(pathWindDTO);
                replayPathCanvasOverlay.setRaceCourse(raceCourseCanvasOverlay.startPoint, raceCourseCanvasOverlay.endPoint);
                replayPathCanvasOverlay.redraw(true);
                legendCanvasOverlay.setPathOverlays(replayPathCanvasOverlays);

                final long tmpDurationTime = currentPath.getPathTime();
                if (tmpDurationTime > maxDurationTime) {
                    maxDurationTime = tmpDurationTime;
                }
            }

            if (timePanel != null) {
                timePanel.setMinMax(new Date(startTime), new Date(startTime + maxDurationTime), true);
                timePanel.resetTimeSlider();
            }

            /**
             * Now we always get the wind field
             */
            final WindFieldDTO windFieldDTO = simulatorResult.windField;
            logger.info("Number of windDTO : " + windFieldDTO.getMatrix().size());

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

    public SimulatorMap(final SimulatorServiceAsync simulatorSvc, final StringMessages stringMessages, final ErrorReporter errorReporter, final int xRes,
            final int yRes, final Timer timer, final WindFieldGenParamsDTO windParams, final SimpleBusyIndicator busyIndicator, final char mode) {
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
        // pathCanvasOverlays = null;
        this.replayPathCanvasOverlays = null;
        this.raceCourseCanvasOverlay = null;
        this.timeListeners = new LinkedList<TimeListenerWithStoppingCriteria>();
        this.initializeData();
        // createOverlays();
    }

    public SimulatorMap(final SimulatorServiceAsync simulatorSvc, final StringMessages stringMessages, final ErrorReporter errorReporter, final int xRes,
            final int yRes, final Timer timer, final SimulatorTimePanel timePanel, final WindFieldGenParamsDTO windParams,
            final SimpleBusyIndicator busyIndicator, final char mode) {
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
        // pathCanvasOverlays = null;
        this.replayPathCanvasOverlays = null;
        this.raceCourseCanvasOverlay = null;
        this.timeListeners = new LinkedList<TimeListenerWithStoppingCriteria>();
        this.initializeData();
        // createOverlays();
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
                final PositionDTO trave = new PositionDTO(54.007063, 10.838356); // 53.978276,10.880156);//53.968015,10.891331);
                final LatLng position = LatLng.newInstance(trave.latDeg, trave.lngDeg);
                mapw.panTo(position);
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

    private void generateWindField(final WindPatternDisplay windPatternDisplay, final boolean removeOverlays) {
        logger.info("In generateWindField");
        if (windPatternDisplay == null) {
            this.errorReporter.reportError("Please select a valid wind pattern.");
            return;
        }
        final PositionDTO startPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.startPoint.getLatitude(),
                this.raceCourseCanvasOverlay.startPoint.getLongitude());
        final PositionDTO endPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.endPoint.getLatitude(),
                this.raceCourseCanvasOverlay.endPoint.getLongitude());
        logger.info("StartPoint:" + startPointDTO);
        this.windParams.setNorthWest(startPointDTO);
        this.windParams.setSouthEast(endPointDTO);
        this.windParams.setxRes(this.xRes);
        this.windParams.setyRes(this.yRes);
        this.busyIndicator.setBusy(true);
        this.simulatorSvc.getWindField(this.windParams, windPatternDisplay, new AsyncCallback<WindFieldDTO>() {
            @Override
            public void onFailure(final Throwable message) {
                errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
            }

            @Override
            public void onSuccess(final WindFieldDTO wl) {
                if (removeOverlays) {
                    removeOverlays();
                }
                logger.info("Number of windDTO : " + wl.getMatrix().size());
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

    private void refreshWindFieldOverlay(final WindFieldDTO wl) {
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

    private void generatePath(final WindPatternDisplay windPatternDisplay, final boolean summaryView, final int boatClassIndex) {
        logger.info("In generatePath");

        if (windPatternDisplay == null) {
            this.errorReporter.reportError("Please select a valid wind pattern.");
            return;
        }

        if (this.mode != SailingSimulatorUtil.measured) {
            final PositionDTO startPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.startPoint.getLatitude(),
                    this.raceCourseCanvasOverlay.startPoint.getLongitude());
            this.windParams.setNorthWest(startPointDTO);

            final PositionDTO endPointDTO = new PositionDTO(this.raceCourseCanvasOverlay.endPoint.getLatitude(),
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
            logger.info("Removed " + num + " overlays");
        }
    }

    private void refreshSummaryView(final WindPatternDisplay windPatternDisplay, final int boatClassIndex, final boolean force) {
        // removeOverlays();
        if (force) {
            this.generatePath(windPatternDisplay, true, boatClassIndex);
        } else {
            if (this.replayPathCanvasOverlays != null && !this.replayPathCanvasOverlays.isEmpty()) {
                System.out.println("Soft refresh");
                for (final PathCanvasOverlay r : this.replayPathCanvasOverlays) {
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

    private void refreshReplayView(final WindPatternDisplay windPatternDisplay, final int boatClassIndex, final boolean force) {
        // removeOverlays();
        if (force) {
            this.generatePath(windPatternDisplay, false, boatClassIndex);
        } else {

            if (this.replayPathCanvasOverlays != null && !this.replayPathCanvasOverlays.isEmpty()) {
                System.out.println("Soft refresh");
                for (final PathCanvasOverlay r : this.replayPathCanvasOverlays) {
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

    private void refreshWindDisplayView(final WindPatternDisplay windPatternDisplay, final boolean force) {

        if (force) {
            // removeOverlays();
            this.windParams.setDefaultTimeSettings();
            this.generateWindField(windPatternDisplay, true);
            // timeListeners.clear();
            // timeListeners.add(windFieldCanvasOverlay);
        } else {

            if (this.replayPathCanvasOverlays != null && !this.replayPathCanvasOverlays.isEmpty()) {
                System.out.println("Soft refresh");
                for (final PathCanvasOverlay r : this.replayPathCanvasOverlays) {
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

    public void refreshView(final ViewName name, final WindPatternDisplay windPatternDisplay, final int boatClassIndex, final boolean force) {
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
    public void timeChanged(final Date date) {
        if (this.stop() == 0) {
            logger.info("Stopping the timer");
            this.timer.stop();
        }
    }

    @Override
    public int stop() {
        int value = 0;
        for (final TimeListenerWithStoppingCriteria t : this.timeListeners) {
            value += t.stop();
        }
        return value;
    }

    private static List<SimulatorWindDTO> identifyPath(final PathDTO gpsTrack, final PathDTO gpsPoly) {

        final List<SimulatorWindDTO> windDTOs = new ArrayList<SimulatorWindDTO>();

        final List<SimulatorWindDTO> gpsTrackWindDTOs = gpsTrack.getMatrix();
        final List<SimulatorWindDTO> gpsPolyWindDTOs = gpsPoly.getMatrix();

        final int countOfGpsPolyWindDTOs = gpsPolyWindDTOs.size();
        if (countOfGpsPolyWindDTOs == 0 || countOfGpsPolyWindDTOs == 1) {
            return null;
        }

        final SimulatorWindDTO startWindDTO = gpsPolyWindDTOs.get(0);
        final SimulatorWindDTO endWindDTO = gpsPolyWindDTOs.get(countOfGpsPolyWindDTOs - 1);

        final int startIndex = getIndexOfClosest(gpsTrackWindDTOs, startWindDTO);
        // System.err.println("BBBB: startIndex = " + startIndex);
        final int endIndex = getIndexOfClosest(gpsTrackWindDTOs, endWindDTO);
        // System.err.println("BBBB: endIndex = " + endIndex);

        for (int index = startIndex; index <= endIndex; index++) {
            windDTOs.add(gpsTrackWindDTOs.get(index));
        }

        return windDTOs;
    }

    private static int getIndexOfClosest(final List<SimulatorWindDTO> items, final SimulatorWindDTO item) {
        final int count = items.size();

        final List<Double> diff_lat = new ArrayList<Double>();
        final List<Double> diff_lng = new ArrayList<Double>();
        final List<Long> diff_timepoint = new ArrayList<Long>();

        for (int index = 0; index < count; index++) {
            diff_lat.add(Math.abs(items.get(index).position.latDeg - item.position.latDeg));
            diff_lng.add(Math.abs(items.get(index).position.lngDeg - item.position.lngDeg));
            diff_timepoint.add(Math.abs(items.get(index).timepoint - item.timepoint));
        }

        final double min_diff_lat = Collections.min(diff_lat);
        final double min_max_diff_lat = min_diff_lat + Collections.max(diff_lat);

        final double min_diff_lng = Collections.min(diff_lng);
        final double min_max_diff_lng = min_diff_lng + Collections.max(diff_lng);

        final long min_diff_timepoint = Collections.min(diff_timepoint);
        final double min_max_diff_timepoint = min_diff_timepoint + Collections.max(diff_timepoint);

        final List<Double> norm_diff_lat = new ArrayList<Double>();
        final List<Double> norm_diff_lng = new ArrayList<Double>();
        final List<Double> norm_diff_timepoint = new ArrayList<Double>();

        for (int index = 0; index < count; index++) {
            norm_diff_lat.add((diff_lat.get(index) - min_diff_lat) / min_max_diff_lat);
            norm_diff_lng.add((diff_lng.get(index) - min_diff_lng) / min_max_diff_lng);
            norm_diff_timepoint.add((diff_timepoint.get(index) - min_diff_timepoint) / min_max_diff_timepoint);
        }

        final List<Double> deltas = new ArrayList<Double>();

        for (int index = 0; index < count; index++) {
            deltas.add(Math.sqrt(Math.pow(norm_diff_lat.get(index), 2) + Math.pow(norm_diff_lng.get(index), 2) + Math.pow(norm_diff_timepoint.get(index), 2)));
        }

        int result = 0;
        double min = deltas.get(0);

        for (int index = 0; index < count; index++) {
            if (deltas.get(index) < min) {
                result = index;
                min = deltas.get(index);
            }
        }

        return result;
    }

    private PathPolyline createPathPolyline(final List<SimulatorWindDTO> pathPoints) {

        final int noOfPathPoints = pathPoints.size();
        SimulatorWindDTO currentPathPoint = null;
        final List<LatLng> points = new ArrayList<LatLng>();

        for (int index = 0; index < noOfPathPoints; index++) {
            currentPathPoint = pathPoints.get(index);
            if (index == 0 || index == noOfPathPoints - 1 || currentPathPoint.isTurn) {
                points.add(LatLng.newInstance(currentPathPoint.position.latDeg, currentPathPoint.position.lngDeg));
            }
        }

        final int boatClassID = 3; // 49er STG

        return new PathPolyline(points.toArray(new LatLng[0]), boatClassID, this.errorReporter, pathPoints, this.simulatorSvc, this.mapw, this);
    }

    @SuppressWarnings("unused")
    private PathPolyline createPathPolyline(final PathDTO pathDTO) {

        return this.createPathPolyline(pathDTO.getMatrix());
    }

    public void addLegendOverlayForPathPolyline(final long totalTimeMilliseconds) {

        final PathCanvasOverlay overlay = new PathCanvasOverlay("Polyline", totalTimeMilliseconds);
        overlay.pathColor = PathPolyline.DEFAULT_COLOR;

        this.legendCanvasOverlay.addPathOverlay(overlay);
    }

    public void redrawLegendCanvasOverlay() {
        this.legendCanvasOverlay.setVisible(true);
        this.legendCanvasOverlay.redraw(true);
    }

}
