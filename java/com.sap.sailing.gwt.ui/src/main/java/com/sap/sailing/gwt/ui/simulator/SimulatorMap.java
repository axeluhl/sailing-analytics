package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
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
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RequiresDataInitialization;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;

public class SimulatorMap extends AbsolutePanel implements RequiresDataInitialization {

    private MapWidget mapw;
    private boolean dataInitialized;
    private boolean overlaysInitialized;
    private WindFieldGenParamsDTO windParams;
    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private PathCanvasOverlay pathCanvasOverlay;
    private ReplayPathCanvasOverlay replayPathCanvasOverlay;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;

    private final SimulatorServiceAsync simulatorSvc;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final Timer timer;
   
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    private final int xRes;
    private final int yRes;

    public enum ViewName {
        SUMMARY, REPLAY, WINDDISPLAY
    }

    public SimulatorMap(SimulatorServiceAsync simulatorSvc, StringMessages stringMessages, ErrorReporter errorReporter,
            int xRes, int yRes, Timer timer) {
        this.simulatorSvc = simulatorSvc;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.xRes = xRes;
        this.yRes = yRes;
        this.timer = timer;

        dataInitialized = false;
        overlaysInitialized = false;
        windParams = new WindFieldGenParamsDTO();
        windFieldCanvasOverlay = null;
        pathCanvasOverlay = null;
        replayPathCanvasOverlay = null;
        raceCourseCanvasOverlay = null;

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
                PositionDTO kiel = new PositionDTO(54.46195148135232, 10.1513671875);

                LatLng position = LatLng.newInstance(kiel.latDeg, kiel.lngDeg);
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
        pathCanvasOverlay = new PathCanvasOverlay();
        replayPathCanvasOverlay = new ReplayPathCanvasOverlay(timer);
        overlaysInitialized = true;
    }

    private void generateWindField(WindPatternDisplay windPatternDisplay) {
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

        simulatorSvc.getWindField(windParams, windPatternDisplay, new AsyncCallback<WindFieldDTO>() {
            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
            }

            @Override
            public void onSuccess(WindFieldDTO wl) {
                logger.info("Number of windDTO : " + wl.getMatrix().size());
                Window.alert("Number of windDTO : " + wl.getMatrix().size());
                refreshWindFieldOverlay(wl);
            }
        });

    }

    private void refreshWindFieldOverlay(final WindFieldDTO wl) {
        windFieldCanvasOverlay.setWindField(wl);
        windFieldCanvasOverlay.redraw(true);
    }

    private void generatePath(WindPatternDisplay windPatternDisplay, final boolean display) {
        logger.info("In generatePath");

        PositionDTO startPointDTO = new PositionDTO(raceCourseCanvasOverlay.startPoint.getLatitude(),
                raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO = new PositionDTO(raceCourseCanvasOverlay.endPoint.getLatitude(),
                raceCourseCanvasOverlay.endPoint.getLongitude());

        windParams.setNorthWest(startPointDTO);
        windParams.setSouthEast(endPointDTO);
        windParams.setxRes(xRes);
        windParams.setyRes(yRes);
       

        simulatorSvc.getPaths(windParams, windPatternDisplay, new AsyncCallback<PathDTO[]>() {
            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed servlet call to SimulatorService\n" + message.getMessage());
            }

            @Override
            public void onSuccess(PathDTO[] paths) {
                logger.info("Number of Paths : " + paths.length);
                /* TODO Revisit for now creating a WindFieldDTO from the path */
                WindFieldDTO pathWindDTO = new WindFieldDTO();
                pathWindDTO.setMatrix(paths[0].getMatrix());
                if (display) {
                    pathCanvasOverlay.setWindField(pathWindDTO);
                    pathCanvasOverlay.redraw(display);
                } else {
                    replayPathCanvasOverlay.setWindField(pathWindDTO);
                    
                    List<WindDTO> path = pathWindDTO.getMatrix();
                    if (path != null) {
                        int length = path.size();
                        Date endTime =  new Date(path.get(length-1).timepoint);
                        windParams.setEndTime(endTime);
                    }
                }
            }
        });

    }

    private boolean isCourseSet() {
        return raceCourseCanvasOverlay.isCourseSet();
    }

    public void reset() {
        if (!overlaysInitialized) {
            initializeOverlays();
        }
        mapw.setDoubleClickZoom(false);
        mapw.removeOverlay(windFieldCanvasOverlay);
        mapw.removeOverlay(pathCanvasOverlay);
        mapw.removeOverlay(replayPathCanvasOverlay);
        raceCourseCanvasOverlay.setSelected(true);
        // raceCourseCanvasOverlay.setVisible(true);
        raceCourseCanvasOverlay.reset();
        raceCourseCanvasOverlay.redraw(true);
    }

    private void refreshSummaryView(WindPatternDisplay windPatternDisplay) {
        mapw.removeOverlay(windFieldCanvasOverlay);
        mapw.removeOverlay(replayPathCanvasOverlay);
        pathCanvasOverlay.displayWindAlongPath = true;
        mapw.addOverlay(pathCanvasOverlay);
        generatePath(windPatternDisplay, true);
        // pathCanvasOverlay.redraw(true);
    }

    private void refreshReplayView(WindPatternDisplay windPatternDisplay) {
        mapw.removeOverlay(pathCanvasOverlay);
        mapw.addOverlay(windFieldCanvasOverlay);
        replayPathCanvasOverlay.displayWindAlongPath = false;
        mapw.addOverlay(replayPathCanvasOverlay);
        //Get the path first so we know how many windfields to generate
        generatePath(windPatternDisplay, false);
        generateWindField(windPatternDisplay);
    }

    private void refreshWindDisplayView(WindPatternDisplay windPatternDisplay) {
        mapw.removeOverlay(pathCanvasOverlay);
        mapw.removeOverlay(replayPathCanvasOverlay);
        mapw.addOverlay(windFieldCanvasOverlay);
        windParams.setDefaultTimeSettings();
        generateWindField(windPatternDisplay);
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
}
