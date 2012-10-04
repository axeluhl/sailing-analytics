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

public class SimulatorMap extends AbsolutePanel implements RequiresDataInitialization, TimeListenerWithStoppingCriteria {

    private MapWidget mapw;
    private boolean dataInitialized;
    private boolean overlaysInitialized;
    private WindFieldGenParamsDTO windParams;
    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private List<PathCanvasOverlay> pathCanvasOverlays;
    private List<PathCanvasOverlay> replayPathCanvasOverlays;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;
    private PathLegendCanvasOverlay legendCanvasOverlay;

    private List<TimeListenerWithStoppingCriteria> timeListeners;

    private final SimulatorServiceAsync simulatorSvc;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final Timer timer;
    private final SimulatorTimePanel timePanel;
    private final SimpleBusyIndicator busyIndicator;

    private ColorPalette colorPalette;

    private static Logger logger = Logger.getLogger(SimulatorMap.class.getName());

    private final int xRes;
    private final int yRes;

    public enum ViewName {
        SUMMARY, REPLAY, WINDDISPLAY
    }

    private class ResultManager implements AsyncCallback<SimulatorResultsDTO> {

        /*
         * private class SortByTimeAsc implements Comparator<PathDTO> {
         * 
         * @Override public int compare(PathDTO o1, PathDTO o2) { return (int) (o1.getPathTime() - o2.getPathTime()); }
         * 
         * }
         */

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
            PathDTO[] paths = result.paths;
            logger.info("Number of Paths : " + paths.length);
            // SortByTimeAsc sorter = new SortByTimeAsc();
            // Arrays.sort(paths, sorter);
            long startTime = paths[0].getMatrix().get(0).timepoint;
            long maxDurationTime = 0;
            
            removeOverlays();
            pathCanvasOverlays.clear();
            replayPathCanvasOverlays.clear();
            colorPalette.reset();
            for (int i = 0; i < paths.length; ++i) {
                /* TODO Revisit for now creating a WindFieldDTO from the path */
                WindFieldDTO pathWindDTO = new WindFieldDTO();
                pathWindDTO.setMatrix(paths[i].getMatrix());
                if (summaryView) {

                    PathCanvasOverlay pathCanvasOverlay = new PathCanvasOverlay(paths[i].name);
                    pathCanvasOverlays.add(pathCanvasOverlay);
                    pathCanvasOverlay.pathColor = colorPalette.getColor(paths.length-1-i);
                    mapw.addOverlay(pathCanvasOverlay);
                    pathCanvasOverlay.setWindField(pathWindDTO);
                    pathCanvasOverlay.setRaceCourse(raceCourseCanvasOverlay.startPoint, raceCourseCanvasOverlay.endPoint);
                    pathCanvasOverlay.redraw(true);
                    legendCanvasOverlay.setPathOverlays(pathCanvasOverlays);

                } else {
                    ReplayPathCanvasOverlay replayPathCanvasOverlay = new ReplayPathCanvasOverlay(paths[i].name, timer);
                    replayPathCanvasOverlays.add(replayPathCanvasOverlay);
                    replayPathCanvasOverlay.pathColor = colorPalette.getColor(paths.length-1-i);
                    mapw.addOverlay(replayPathCanvasOverlay);
                    replayPathCanvasOverlay.setWindField(pathWindDTO);
                    replayPathCanvasOverlay.setRaceCourse(raceCourseCanvasOverlay.startPoint, raceCourseCanvasOverlay.endPoint);
                    legendCanvasOverlay.setPathOverlays(replayPathCanvasOverlays);

                }
                
                long tmpDurationTime = paths[i].getPathTime();
                if (tmpDurationTime > maxDurationTime) {
                    maxDurationTime = tmpDurationTime;
                }
            }

            if (timePanel != null) {
                timePanel.setMinMax(new Date(startTime), new Date(startTime+maxDurationTime), true);
                timePanel.resetTimeSlider();
            }
            
            if (!summaryView) {
                WindFieldDTO windFieldDTO = result.windField;
                logger.info("Number of windDTO : " + windFieldDTO.getMatrix().size());
                mapw.addOverlay(windFieldCanvasOverlay);
                refreshWindFieldOverlay(windFieldDTO);

                timeListeners.clear();
                timeListeners.add(windFieldCanvasOverlay);
                for (int i = 0; i < replayPathCanvasOverlays.size(); ++i) {
                    timeListeners.add(replayPathCanvasOverlays.get(i));
                }
            }
            
            mapw.addOverlay(legendCanvasOverlay);
            //legendCanvasOverlay.redraw(true);
            
            busyIndicator.setBusy(false);
        }

    }

    public SimulatorMap(SimulatorServiceAsync simulatorSvc, StringMessages stringMessages, ErrorReporter errorReporter,
            int xRes, int yRes, Timer timer, WindFieldGenParamsDTO windParams, SimpleBusyIndicator busyIndicator) {
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
        colorPalette = new ColorPaletteGenerator();

        dataInitialized = false;
        overlaysInitialized = false;

        windFieldCanvasOverlay = null;
        pathCanvasOverlays = null;
        replayPathCanvasOverlays = null;
        raceCourseCanvasOverlay = null;
        timeListeners = new LinkedList<TimeListenerWithStoppingCriteria>();
        initializeData();
        // createOverlays();
    }
    
    public SimulatorMap(SimulatorServiceAsync simulatorSvc, StringMessages stringMessages, ErrorReporter errorReporter,
            int xRes, int yRes, Timer timer, SimulatorTimePanel timePanel, WindFieldGenParamsDTO windParams, SimpleBusyIndicator busyIndicator) {
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
        colorPalette = new ColorPaletteGenerator();

        dataInitialized = false;
        overlaysInitialized = false;

        windFieldCanvasOverlay = null;
        pathCanvasOverlays = null;
        replayPathCanvasOverlays = null;
        raceCourseCanvasOverlay = null;
        timeListeners = new LinkedList<TimeListenerWithStoppingCriteria>();
        initializeData();
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
                //PositionDTO kiel = new PositionDTO(54.46195148135232, 10.1513671875);
                PositionDTO trave = new PositionDTO(54.007063,10.838356); //53.978276,10.880156);//53.968015,10.891331);
                LatLng position = LatLng.newInstance(trave.latDeg, trave.lngDeg);
                mapw.panTo(position);
                SimulatorMap.this.add(mapw, 0, 0);
                mapw.setSize("100%", "100%");

                dataInitialized = true;
            }
        });
    }

    private void initializeOverlays() {
        raceCourseCanvasOverlay = new RaceCourseCanvasOverlay();
        mapw.addOverlay(raceCourseCanvasOverlay);

        windFieldCanvasOverlay = new WindFieldCanvasOverlay(timer);
        // mapw.addOverlay(windFieldCanvasOverlay);
        pathCanvasOverlays = new ArrayList<PathCanvasOverlay>();
        replayPathCanvasOverlays = new ArrayList<PathCanvasOverlay>();
        // timeListeners.add(replayPathCanvasOverlay);
        legendCanvasOverlay = new PathLegendCanvasOverlay();

        overlaysInitialized = true;
    }

    private void generateWindField(WindPatternDisplay windPatternDisplay, final boolean removeOverlays) {
        logger.info("In generateWindField");
        if (windPatternDisplay == null) {
            errorReporter.reportError("Please select a valid wind pattern.");
            return;
        }
        PositionDTO startPointDTO = new PositionDTO(raceCourseCanvasOverlay.startPoint.getLatitude(),
                raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO = new PositionDTO(raceCourseCanvasOverlay.endPoint.getLatitude(),
                raceCourseCanvasOverlay.endPoint.getLongitude());
        logger.info("StartPoint:" + startPointDTO);
        windParams.setNorthWest(startPointDTO);
        windParams.setSouthEast(endPointDTO);
        windParams.setxRes(xRes);
        windParams.setyRes(yRes);
        busyIndicator.setBusy(true);
        simulatorSvc.getWindField(windParams, windPatternDisplay, new AsyncCallback<WindFieldDTO>() {
            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
            }

            @Override
            public void onSuccess(WindFieldDTO wl) {
                if (removeOverlays) {
                    removeOverlays();
                }
                logger.info("Number of windDTO : " + wl.getMatrix().size());
                // Window.alert("Number of windDTO : " + wl.getMatrix().size());
                mapw.addOverlay(windFieldCanvasOverlay);
                refreshWindFieldOverlay(wl);
                timeListeners.clear();
                timeListeners.add(windFieldCanvasOverlay);
                
                timePanel.setMinMax(windParams.getStartTime(), windParams.getEndTime(), true);
                timePanel.resetTimeSlider();
                
                busyIndicator.setBusy(false);
            }
        });

    }

    private void refreshWindFieldOverlay(final WindFieldDTO wl) {
        windFieldCanvasOverlay.setWindField(wl);
        timer.setTime(windParams.getStartTime().getTime());
        windFieldCanvasOverlay.redraw(true);
    }

    private void generatePath(WindPatternDisplay windPatternDisplay, final boolean summaryView) {
        logger.info("In generatePath");
        if (windPatternDisplay == null) {
            errorReporter.reportError("Please select a valid wind pattern.");
            return;
        }
        PositionDTO startPointDTO = new PositionDTO(raceCourseCanvasOverlay.startPoint.getLatitude(),
                raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO = new PositionDTO(raceCourseCanvasOverlay.endPoint.getLatitude(),
                raceCourseCanvasOverlay.endPoint.getLongitude());

        windParams.setNorthWest(startPointDTO);
        windParams.setSouthEast(endPointDTO);
        windParams.setxRes(xRes);
        windParams.setyRes(yRes);
        busyIndicator.setBusy(true);
        // simulatorSvc.getPaths(windParams, windPatternDisplay, new PathManager(windPatternDisplay, summaryView));
        simulatorSvc.getSimulatorResults(windParams, windPatternDisplay, !summaryView, new ResultManager(summaryView));

    }

    private boolean isCourseSet() {
        return raceCourseCanvasOverlay.isCourseSet();
    }

    public void reset() {
        if (!overlaysInitialized) {
            initializeOverlays();
        } else {
            removeOverlays();
        }
        mapw.setDoubleClickZoom(false);
        raceCourseCanvasOverlay.setSelected(true);
        // raceCourseCanvasOverlay.setVisible(true);
        raceCourseCanvasOverlay.reset();
        raceCourseCanvasOverlay.redraw(true);
    }

    public void removeOverlays() {
        if (overlaysInitialized) {
            int num = 0;
            mapw.removeOverlay(windFieldCanvasOverlay);
            num++;
            for (int i = 0; i < pathCanvasOverlays.size(); ++i) {
                mapw.removeOverlay(pathCanvasOverlays.get(i));
                num++;
            }
            for (int i = 0; i < replayPathCanvasOverlays.size(); ++i) {
                mapw.removeOverlay(replayPathCanvasOverlays.get(i));
                num++;
            }
            mapw.removeOverlay(legendCanvasOverlay);
            logger.info("Removed " + num + " overlays");
        }
    }

    private void refreshSummaryView(WindPatternDisplay windPatternDisplay) {
        // removeOverlays();
        generatePath(windPatternDisplay, true);
    }

    private void refreshReplayView(WindPatternDisplay windPatternDisplay) {
        // removeOverlays();
        generatePath(windPatternDisplay, false);
    }

    private void refreshWindDisplayView(WindPatternDisplay windPatternDisplay) {
        // removeOverlays();
        windParams.setDefaultTimeSettings();
        generateWindField(windPatternDisplay, true);
        // timeListeners.clear();
        // timeListeners.add(windFieldCanvasOverlay);
    }

    public void refreshView(ViewName name, WindPatternDisplay windPatternDisplay) {
        if (!overlaysInitialized) {
            initializeOverlays();
        }
        if (isCourseSet()) {
            mapw.setDoubleClickZoom(true);
            raceCourseCanvasOverlay.setSelected(false);
            switch (name) {
            case SUMMARY:
                refreshSummaryView(windPatternDisplay);
                break;
            case REPLAY:
                refreshReplayView(windPatternDisplay);
                break;
            case WINDDISPLAY:
                refreshWindDisplayView(windPatternDisplay);
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
        loadMapsAPI();
    }

    @Override
    public boolean isDataInitialized() {
        return dataInitialized;
    }

    @Override
    public void timeChanged(Date date) {
        if (stop() == 0) {
            logger.info("Stopping the timer");
            timer.stop();
        }
    }

    @Override
    public int stop() {

        int value = 0;
        for (TimeListenerWithStoppingCriteria t : timeListeners) {
            value += t.stop();
        }
        return value;
    }

}
