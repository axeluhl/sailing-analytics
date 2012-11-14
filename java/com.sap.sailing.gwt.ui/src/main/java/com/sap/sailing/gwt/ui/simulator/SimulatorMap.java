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
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.WindDTO;
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

    private PathPolyline pathPolyline;
    private boolean warningAlreadyShown = false;

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
            SimulatorMap.this.errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
        }

        @Override
        public void onSuccess(final SimulatorResultsDTOAndNotificationMessage result) {

            final String notificationMessage = result.getNotificationMessage();
            if (notificationMessage != "" && notificationMessage.length() != 0 && SimulatorMap.this.warningAlreadyShown == false) {
                SimulatorMap.this.errorReporter.reportNotification(notificationMessage);
                SimulatorMap.this.warningAlreadyShown = true;
            }

            final SimulatorResultsDTO simulatorResult = result.getSimulatorResultsDTO();
            final PathDTO[] paths = simulatorResult.paths;
            logger.info("Number of Paths : " + paths.length);
            // SortByTimeAsc sorter = new SortByTimeAsc();
            // Arrays.sort(paths, sorter);
            final long startTime = paths[0].getMatrix().get(0).timepoint;
            long maxDurationTime = 0;

            if (SimulatorMap.this.mode == SailingSimulatorUtil.measured) {
                PositionDTO pos = simulatorResult.raceCourse.coursePositions.waypointPositions.get(0);
                SimulatorMap.this.raceCourseCanvasOverlay.startPoint = LatLng.newInstance(pos.latDeg, pos.lngDeg);
                pos = simulatorResult.raceCourse.coursePositions.waypointPositions.get(1);
                SimulatorMap.this.raceCourseCanvasOverlay.endPoint = LatLng.newInstance(pos.latDeg, pos.lngDeg);
            }

            SimulatorMap.this.raceCourseCanvasOverlay.redraw(true);
            SimulatorMap.this.removeOverlays();
            // pathCanvasOverlays.clear();
            SimulatorMap.this.replayPathCanvasOverlays.clear();
            SimulatorMap.this.colorPalette.reset();

            String pathName = null;
            PathDTO currentPath = null;
            String color = null;

            for (int index = 0; index < paths.length; ++index) {

                currentPath = paths[index];
                pathName = paths[index].name;
                color = SimulatorMap.this.colorPalette.getColor(paths.length - 1 - index);

                if (pathName.equals("GPS Poly")) {
                    SimulatorMap.this.pathPolyline = createPathPolyline(currentPath, SimulatorMap.this.mapw);
                }

                /* TODO Revisit for now creating a WindFieldDTO from the path */
                final WindFieldDTO pathWindDTO = new WindFieldDTO();
                pathWindDTO.setMatrix(currentPath.getMatrix());

                final ReplayPathCanvasOverlay replayPathCanvasOverlay = new ReplayPathCanvasOverlay(pathName, SimulatorMap.this.timer);
                SimulatorMap.this.replayPathCanvasOverlays.add(replayPathCanvasOverlay);
                replayPathCanvasOverlay.pathColor = color;

                if (this.summaryView) {

                    replayPathCanvasOverlay.displayWindAlongPath = true;
                    SimulatorMap.this.timer.removeTimeListener(replayPathCanvasOverlay);
                    replayPathCanvasOverlay.setTimer(null);
                }
                SimulatorMap.this.mapw.addOverlay(replayPathCanvasOverlay);
                replayPathCanvasOverlay.setWindField(pathWindDTO);
                replayPathCanvasOverlay.setRaceCourse(SimulatorMap.this.raceCourseCanvasOverlay.startPoint, SimulatorMap.this.raceCourseCanvasOverlay.endPoint);
                replayPathCanvasOverlay.redraw(true);
                SimulatorMap.this.legendCanvasOverlay.setPathOverlays(SimulatorMap.this.replayPathCanvasOverlays);

                final long tmpDurationTime = currentPath.getPathTime();
                if (tmpDurationTime > maxDurationTime) {
                    maxDurationTime = tmpDurationTime;
                }
            }

            if (SimulatorMap.this.timePanel != null) {
                SimulatorMap.this.timePanel.setMinMax(new Date(startTime), new Date(startTime + maxDurationTime), true);
                SimulatorMap.this.timePanel.resetTimeSlider();
            }

            /**
             * Now we always get the wind field
             */
            final WindFieldDTO windFieldDTO = simulatorResult.windField;
            logger.info("Number of windDTO : " + windFieldDTO.getMatrix().size());

            if (SimulatorMap.this.windParams.isShowGrid()) {
                SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.windGridCanvasOverlay);
                SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.windLineCanvasOverlay);
            }
            if (SimulatorMap.this.windParams.isShowArrows()) {
                SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.windFieldCanvasOverlay);
            }

            SimulatorMap.this.refreshWindFieldOverlay(windFieldDTO);

            SimulatorMap.this.timeListeners.clear();
            if (SimulatorMap.this.windParams.isShowArrows()) {
                SimulatorMap.this.timeListeners.add(SimulatorMap.this.windFieldCanvasOverlay);
            }
            if (SimulatorMap.this.windParams.isShowGrid()) {
                SimulatorMap.this.timeListeners.add(SimulatorMap.this.windGridCanvasOverlay);
                SimulatorMap.this.timeListeners.add(SimulatorMap.this.windLineCanvasOverlay);
            }
            for (int i = 0; i < SimulatorMap.this.replayPathCanvasOverlays.size(); ++i) {
                SimulatorMap.this.timeListeners.add(SimulatorMap.this.replayPathCanvasOverlays.get(i));
            }

            if (this.summaryView) {
                if (SimulatorMap.this.windFieldCanvasOverlay != null) {
                    SimulatorMap.this.windFieldCanvasOverlay.setVisible(false);
                }
                if (SimulatorMap.this.windGridCanvasOverlay != null) {
                    SimulatorMap.this.windGridCanvasOverlay.setVisible(false);
                }
                if (SimulatorMap.this.windLineCanvasOverlay != null) {
                    SimulatorMap.this.windLineCanvasOverlay.setVisible(false);
                }
            }

            SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.legendCanvasOverlay);
            SimulatorMap.this.legendCanvasOverlay.setVisible(true);
            SimulatorMap.this.legendCanvasOverlay.redraw(true);

            SimulatorMap.this.busyIndicator.setBusy(false);
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
                SimulatorMap.this.mapw = new MapWidget();
                // mapw.setUI(SimulatorMapOptions.newInstance());
                SimulatorMap.this.mapw.setZoomLevel(13);
                // mapw.setSize("100%", "650px");
                // mapw.setSize("100%", "80%");

                SimulatorMap.this.mapw.addControl(new LargeMapControl3D(), new ControlPosition(ControlAnchor.TOP_RIGHT, /* offsetX */0, /* offsetY */
                        30));
                SimulatorMap.this.mapw.addControl(new MenuMapTypeControl());
                SimulatorMap.this.mapw.addControl(new ScaleControl(), new ControlPosition(ControlAnchor.BOTTOM_RIGHT, /* offsetX */10, /* offsetY */
                        20));
                // Add the map to the HTML host page
                SimulatorMap.this.mapw.setScrollWheelZoomEnabled(true);
                // mapw.setContinuousZoom(true);
                SimulatorMap.this.mapw.setTitle(SimulatorMap.this.stringMessages.simulator() + " " + SimulatorMap.this.stringMessages.map());
                // PositionDTO kiel = new PositionDTO(54.46195148135232, 10.1513671875);
                final PositionDTO trave = new PositionDTO(54.007063, 10.838356); // 53.978276,10.880156);//53.968015,10.891331);
                final LatLng position = LatLng.newInstance(trave.latDeg, trave.lngDeg);
                SimulatorMap.this.mapw.panTo(position);
                SimulatorMap.this.add(SimulatorMap.this.mapw, 0, 0);
                SimulatorMap.this.mapw.setSize("100%", "100%");

                SimulatorMap.this.dataInitialized = true;
            }
        });
    }

    private void initializeOverlays() {
        this.raceCourseCanvasOverlay = new RaceCourseCanvasOverlay();
        this.raceCourseCanvasOverlay.getCanvas().getElement().setClassName("raceCourse");
        System.out.println("RaceCourseCanvasOverlay z-index: " + this.raceCourseCanvasOverlay.getCanvas().getElement().getStyle().getZIndex());
        this.mapw.addOverlay(this.raceCourseCanvasOverlay);

        if (this.windParams.isShowArrows()) {
            this.windFieldCanvasOverlay = new WindFieldCanvasOverlay(this.timer);
        }
        if (this.windParams.isShowGrid()) {
            this.windGridCanvasOverlay = new WindGridCanvasOverlay(this.timer, this.xRes, this.yRes);
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
                SimulatorMap.this.errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
            }

            @Override
            public void onSuccess(final WindFieldDTO wl) {
                if (removeOverlays) {
                    SimulatorMap.this.removeOverlays();
                }
                logger.info("Number of windDTO : " + wl.getMatrix().size());
                // Window.alert("Number of windDTO : " + wl.getMatrix().size());
                if (SimulatorMap.this.windParams.isShowGrid()) {
                    SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.windGridCanvasOverlay);
                    SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.windLineCanvasOverlay);
                }
                if (SimulatorMap.this.windParams.isShowArrows()) {
                    SimulatorMap.this.mapw.addOverlay(SimulatorMap.this.windFieldCanvasOverlay);
                }
                SimulatorMap.this.refreshWindFieldOverlay(wl);
                SimulatorMap.this.timeListeners.clear();
                if (SimulatorMap.this.windParams.isShowArrows()) {
                    SimulatorMap.this.timeListeners.add(SimulatorMap.this.windFieldCanvasOverlay);
                }
                if (SimulatorMap.this.windParams.isShowGrid()) {
                    SimulatorMap.this.timeListeners.add(SimulatorMap.this.windGridCanvasOverlay);
                    SimulatorMap.this.timeListeners.add(SimulatorMap.this.windLineCanvasOverlay);
                }
                SimulatorMap.this.timePanel.setMinMax(SimulatorMap.this.windParams.getStartTime(), SimulatorMap.this.windParams.getEndTime(), true);
                SimulatorMap.this.timePanel.resetTimeSlider();

                SimulatorMap.this.busyIndicator.setBusy(false);
            }
        });

    }

    private void refreshWindFieldOverlay(final WindFieldDTO wl) {
        if (this.windFieldCanvasOverlay != null) {
            this.windFieldCanvasOverlay.setWindField(wl);
        }
        if (this.windGridCanvasOverlay != null) {
            this.windGridCanvasOverlay.setWindField(wl);
        }

        if (windLineCanvasOverlay != null) {
            windLineCanvasOverlay.setWindLinesDTO(wl.getWindLinesDTO());
            if (windGridCanvasOverlay != null) {
                windLineCanvasOverlay.setGridCorners(windGridCanvasOverlay.getGridCorners());
            }
        }
        
        this.timer.setTime(this.windParams.getStartTime().getTime());
        if (this.windParams.isShowArrows()) {
            this.windFieldCanvasOverlay.redraw(true);
        }
        if (this.windParams.isShowGrid()) {
            this.windGridCanvasOverlay.redraw(true);
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

    private static PathPolyline createPathPolyline(final PathDTO pathDTO, final MapWidget map) {
        final List<WindDTO> pathPoints = pathDTO.getMatrix();

        final int noOfPathPoints = pathPoints.size();
        WindDTO currentPathPoint = null;
        final List<LatLng> points = new ArrayList<LatLng>();

        for (int index = 0; index < noOfPathPoints; index++) {
            currentPathPoint = pathPoints.get(index);
            if (index == 0 || index == noOfPathPoints - 1 || currentPathPoint.isTurn) {
                points.add(LatLng.newInstance(currentPathPoint.position.latDeg, currentPathPoint.position.lngDeg));
            }
        }

        final double averageWindSpeed = computeAverageWindSpeed(pathPoints);

        return new PathPolyline(points.toArray(new LatLng[0]), averageWindSpeed, map);
    }

    public static double computeAverageWindSpeed(final List<WindDTO> windDTOs) {
        double result = 0.0;
        for (final WindDTO windDTO : windDTOs) {
            result += windDTO.trueWindSpeedInMetersPerSecond;
        }

        result /= windDTOs.size();
        // new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(180));
        return result;
    }

    public double getPathPolylineAverageSpeed() {
        return this.pathPolyline.getAverageWindSpeed();
    }

}
