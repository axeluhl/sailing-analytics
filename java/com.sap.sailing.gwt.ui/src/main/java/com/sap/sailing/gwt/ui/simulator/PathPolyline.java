package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.event.PolylineLineUpdatedHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.PolyEditingOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.Request1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.Response1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDPoint;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDSegment;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDVector;

public class PathPolyline {
    private final static boolean DRAW_DISTANCED_DASHLINES = false;
    private final static boolean DRAW_BISECTOR_DASHLINES = false;
    private final static boolean DRAW_VERTICAL_DASHLINES = false;

    public final static String DEFAULT_COLOR = "#8B0000";
    private final static int DEFAULT_WEIGHT = 3;
    private final static double DEFAULT_OPACITY = 1.0;
    private final static double DEFAULT_DISTANCE_PX = 25;

    private final static int DEFAULT_DASHLINE_WEIGHT = 1;
    private final static String DEFAULT_DASHLINE_DISTANCED_COLOR = "Red";
    private final static String DEFAULT_DASHLINE_BISECTOR_COLOR = "Green";
    private final static String DEFAULT_DASHLINE_VERTICAL_COLOR = "Blue";

    private Polyline polyline = null;
    private LatLng[] turnPoints = null;

    private List<Polyline> dashedLines = null;

    private String color = "";
    private int weight = 0;
    private double opacity = 0.0;
    private Map<TwoDPoint, List<TwoDPoint>> originAndHeads = null;
    private int boatClassID = 0;
    private List<SimulatorWindDTO> allPoints = null;
    private SimulatorServiceAsync simulatorService = null;
    private MapWidget map = null;
    private ErrorReporter errorReporter = null;
    private boolean warningAlreadyShown = false;
    private SimulatorMap simulatorMap = null;

    private static int STEP_DURATION_MILLISECONDS = 2000;
    private static boolean USE_REAL_AVERAGE_WIND = true;

    public static PathPolyline createPathPolyline(List<SimulatorWindDTO> pathPoints, ErrorReporter errorReporter, SimulatorServiceAsync simulatorService,
            MapWidget map, SimulatorMap simulatorMap, int boatClassID) {

        List<LatLng> points = new ArrayList<LatLng>();

        for (SimulatorWindDTO pathPoint : pathPoints) {
            if (pathPoint.isTurn) {
                points.add(LatLng.newInstance(pathPoint.position.latDeg, pathPoint.position.lngDeg));
            }
        }

        return new PathPolyline(points.toArray(new LatLng[0]), boatClassID, errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline() {
    }

    private PathPolyline(LatLng[] points, int boatClassID, ErrorReporter errorReporter, List<SimulatorWindDTO> pathPoints,
            SimulatorServiceAsync simulatorService, MapWidget map, SimulatorMap simulatorMap) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, boatClassID, errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline(LatLng[] points, String color, int weight, double opacity, int boatClassID, ErrorReporter errorReporter,
            List<SimulatorWindDTO> pathPoints, SimulatorServiceAsync simulatorService, MapWidget map, SimulatorMap simulatorMap) {
        this.turnPoints = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;

        this.dashedLines = new ArrayList<Polyline>();
        this.originAndHeads = new HashMap<TwoDPoint, List<TwoDPoint>>();

        this.allPoints = pathPoints;
        this.simulatorService = simulatorService;
        this.map = map;
        this.boatClassID = boatClassID;
        this.errorReporter = errorReporter;

        this.simulatorMap = simulatorMap;

        this.drawPolylineOnMap();
        this.drawDashLinesOnMap(this.map.getZoomLevel());

        this.map.addMapZoomEndHandler(new MapZoomEndHandler() {
            @Override
            public void onZoomEnd(MapZoomEndEvent event) {
                drawDashLinesOnMap(event.getNewZoomLevel());
            }
        });
    }

    private void drawPolylineOnMap() {
        if (this.polyline != null) {
            this.map.removeOverlay(this.polyline);
        }

        this.polyline = new Polyline(this.turnPoints, this.color, this.weight, this.opacity);

        this.polyline.addPolylineLineUpdatedHandler(new PolylineLineUpdatedHandler() {
            @Override
            public void onUpdate(PolylineLineUpdatedEvent event) {
                final int indexOfMovedPoint = getIndexOfMovedPoint();
                final int noOfPoints = turnPoints.length;

                final LatLng originalOriginLng = turnPoints[indexOfMovedPoint];

                if (indexOfMovedPoint == 0 || indexOfMovedPoint == noOfPoints - 1) {
                    // start and end points cannot be moved!
                } else {
                    // System.err.println("INDEX OF MOVED POINT = " + indexOfMovedPoint);

                    TwoDPoint newOrigin = computeNewOrigin(indexOfMovedPoint);
                    final boolean projectionOnBeforeLine = projectedToBeforeLine(indexOfMovedPoint);

                    if (indexOfMovedPoint == 1) {
                        turnPoints[indexOfMovedPoint + 1] = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin));
                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        turnPoints[indexOfMovedPoint - 1] = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin));
                    } else {
                        turnPoints[indexOfMovedPoint - 1] = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin));
                        turnPoints[indexOfMovedPoint + 1] = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin));
                    }

                    turnPoints[indexOfMovedPoint] = toLatLng(newOrigin);

                    LatLng newOriginLng = turnPoints[indexOfMovedPoint];

                    // start of "cut the triangles" fix
                    if (noOfPoints > 3) {
                        LatLng first = null;
                        LatLng second = null;
                        LatLng third = null;
                        LatLng fourth = null;

                        for (int index = 0; (index + 3) < noOfPoints; index++) {
                            first = turnPoints[index];
                            second = turnPoints[index + 1];
                            third = turnPoints[index + 2];
                            fourth = turnPoints[index + 3];

                            if (TwoDPoint.areIntersecting(toTwoDPoint(first), toTwoDPoint(second), toTwoDPoint(third), toTwoDPoint(fourth))) {

                                TwoDSegment firstSegment = new TwoDSegment(toTwoDPoint(first), toTwoDPoint(second));
                                TwoDSegment secondSegment = new TwoDSegment(toTwoDPoint(third), toTwoDPoint(fourth));
                                List<LatLng> newTurnPoints = new ArrayList<LatLng>();

                                for (int index2 = 0; index2 < noOfPoints; index2++) {
                                    if (index2 == index + 1) {
                                        continue;
                                    } else if (index2 == index + 2) {
                                        newTurnPoints.add(toLatLng(firstSegment.intersectionPointWith(secondSegment)));
                                    } else {
                                        newTurnPoints.add(LatLng.newInstance(turnPoints[index2].getLatitude(), turnPoints[index2].getLongitude()));
                                    }
                                }

                                turnPoints = null;
                                turnPoints = newTurnPoints.toArray(new LatLng[0]);

                                break;
                            }
                        }
                    }
                    // end of "cut the triangles" fix

                    // start of "optimal towards wind" fix
                    SimulatorWindDTO firstPoint = toSimulatorWindDTO(originalOriginLng);
                    PositionDTO secondPoint = toPositionDTO(newOriginLng);

                    Request1TurnerDTO requestData = new Request1TurnerDTO(boatClassID, allPoints, firstPoint, secondPoint, USE_REAL_AVERAGE_WIND,
                            STEP_DURATION_MILLISECONDS, !projectionOnBeforeLine);

                    simulatorService.get1Turner(requestData, new AsyncCallback<Response1TurnerDTO>() {

                        @Override
                        public void onFailure(Throwable error) {
                            System.out.println("eroare");
                            errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());

                        }

                        @Override
                        public void onSuccess(Response1TurnerDTO receiveData) {
                            System.out.println("succes");

                            SimulatorWindDTO oneTurner = receiveData.oneTurner;
                            List<LatLng> newTurnPoints = new ArrayList<LatLng>();

                            for (int index2 = 0; index2 < noOfPoints; index2++) {
                                if (index2 == indexOfMovedPoint) {
                                    if (projectionOnBeforeLine) {
                                        newTurnPoints.add(originalOriginLng);
                                        newTurnPoints.add(LatLng.newInstance(oneTurner.position.latDeg, oneTurner.position.lngDeg));
                                        newTurnPoints.add(turnPoints[index2]);
                                    } else {
                                        newTurnPoints.add(turnPoints[index2]);
                                        newTurnPoints.add(LatLng.newInstance(oneTurner.position.latDeg, oneTurner.position.lngDeg));
                                        newTurnPoints.add(originalOriginLng);
                                    }
                                } else {
                                    newTurnPoints.add(turnPoints[index2]);
                                }
                            }

                            turnPoints = null;
                            turnPoints = newTurnPoints.toArray(new LatLng[0]);
                        }
                    });
                    // end of "optimal towards wind" fix
                }

                drawPolylineOnMap();
                drawDashLinesOnMap(map.getZoomLevel());
            }
        });

        this.map.addOverlay(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.turnPoints.length - 1));

        // this.getTotalTime_old();
        this.getTotalTime_new();
    }

    private SimulatorWindDTO toSimulatorWindDTO(LatLng latLng) {

        int index = 0;
        SimulatorWindDTO temp = null;
        for (; index < this.allPoints.size(); index++) {

            temp = this.allPoints.get(index);
            if (temp.isTurn && temp.position.latDeg == latLng.getLatitude() && temp.position.lngDeg == latLng.getLongitude()) {
                System.out.println("toSimulatorWindDTO, index = " + index);
                break;
            }
        }

        return temp;
    }

    private TwoDPoint computeNewOrigin(int indexOfMovedPoint) {

        double distance = (map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

        TwoDPoint beforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 1]);
        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint]);
        TwoDPoint afterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 1]);

        TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance, beforeOrigin);
        TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance, afterOrigin);

        return TwoDPoint.getCorrectProjection(origin, head1, head2, toTwoDPoint(this.polyline.getVertex(indexOfMovedPoint)));
    }

    private boolean projectedToBeforeLine(int indexOfMovedPoint) {
        double distance = (map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint]);
        TwoDPoint newOrigin = toTwoDPoint(this.polyline.getVertex(indexOfMovedPoint));

        TwoDPoint beforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 1]);
        TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance, beforeOrigin);
        TwoDSegment oh1 = new TwoDSegment(origin, head1);
        TwoDPoint p1 = oh1.projectionOfPointOnLine(newOrigin);
        double d1 = newOrigin.distanceBetween(p1);

        TwoDPoint afterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 1]);
        TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance, afterOrigin);
        TwoDSegment oh2 = new TwoDSegment(origin, head2);
        TwoDPoint p2 = oh2.projectionOfPointOnLine(newOrigin);
        double d2 = newOrigin.distanceBetween(p2);

        return (d1 > d2);
    }

    private TwoDPoint computeAfterNewOrigin(int indexOfMovedPoint, TwoDPoint newOrigin) {

        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint]);
        TwoDPoint afterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 1]);
        TwoDPoint afterAfterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 2]);

        TwoDSegment afterLine = new TwoDSegment(afterOrigin, afterAfterOrigin);
        TwoDVector afterVector = new TwoDVector(origin, afterOrigin);
        return TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);
    }

    private TwoDPoint computeBeforeNewOrigin(int indexOfMovedPoint, TwoDPoint newOrigin) {

        TwoDPoint beforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 1]);
        TwoDPoint beforeBeforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 2]);
        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint]);

        TwoDSegment beforeLine = new TwoDSegment(beforeBeforeOrigin, beforeOrigin);
        TwoDVector beforeVector = new TwoDVector(beforeOrigin, origin);

        return TwoDPoint.projectToLineByVector(newOrigin, beforeLine, beforeVector);
    }

    private void drawDashLinesOnMap(int zoomLevel) {
        int noOfPathPoints = this.turnPoints.length;
        if (noOfPathPoints == 0 || noOfPathPoints == 1 || noOfPathPoints == 2) {
            return;
        }

        for (Polyline line : this.dashedLines) {
            this.map.removeOverlay(line);
        }
        this.dashedLines.clear();

        TwoDPoint distancedFromPoint = null;
        TwoDPoint startPoint = null;
        TwoDPoint distancedPoint = null;
        Point temp = null;

        this.originAndHeads.clear();

        double distance = (zoomLevel - 11) * DEFAULT_DISTANCE_PX;

        for (int index = 0; index < noOfPathPoints - 2; index++) {
            temp = this.map.convertLatLngToContainerPixel(this.turnPoints[index + 1]);
            distancedFromPoint = new TwoDPoint(temp.getX(), temp.getY());

            temp = this.map.convertLatLngToContainerPixel(this.turnPoints[index]);
            startPoint = new TwoDPoint(temp.getX(), temp.getY());

            distancedPoint = TwoDPoint.getDistancedPoint(distancedFromPoint, distance, startPoint);

            if (this.originAndHeads.containsKey(distancedFromPoint) == false) {
                this.originAndHeads.put(distancedFromPoint, new ArrayList<TwoDPoint>());
            }
            this.originAndHeads.get(distancedFromPoint).add(distancedPoint);

            if (DRAW_DISTANCED_DASHLINES) {
                this.dashedLines.add(this.createPolyline(distancedFromPoint, distancedPoint, DEFAULT_DASHLINE_DISTANCED_COLOR, DEFAULT_DASHLINE_WEIGHT));
            }
        }

        for (int index = noOfPathPoints - 1; index > 1; index--) {
            temp = this.map.convertLatLngToContainerPixel(this.turnPoints[index - 1]);
            distancedFromPoint = new TwoDPoint(temp.getX(), temp.getY());

            temp = this.map.convertLatLngToContainerPixel(this.turnPoints[index]);
            startPoint = new TwoDPoint(temp.getX(), temp.getY());

            distancedPoint = TwoDPoint.getDistancedPoint(distancedFromPoint, distance, startPoint);

            if (this.originAndHeads.containsKey(distancedFromPoint) == false) {
                this.originAndHeads.put(distancedFromPoint, new ArrayList<TwoDPoint>());
            }
            this.originAndHeads.get(distancedFromPoint).add(distancedPoint);

            if (DRAW_DISTANCED_DASHLINES) {
                this.dashedLines.add(this.createPolyline(distancedFromPoint, distancedPoint, DEFAULT_DASHLINE_DISTANCED_COLOR, DEFAULT_DASHLINE_WEIGHT));
            }
        }

        TwoDPoint origin = null;
        TwoDPoint head1 = null;
        TwoDPoint head2 = null;
        TwoDPoint bisectorHead = null;
        TwoDPoint rotated90Head = null;
        TwoDPoint rotated270Head = null;

        for (Entry<TwoDPoint, List<TwoDPoint>> entry : this.originAndHeads.entrySet()) {
            origin = entry.getKey();
            head1 = entry.getValue().get(0);
            head2 = entry.getValue().get(1);
            bisectorHead = TwoDPoint.getBisectingPoint(origin, head1, head2, distance);
            rotated90Head = TwoDPoint.get90RotatedPoint(origin, bisectorHead);
            rotated270Head = TwoDPoint.getDistancedPoint(origin, distance, rotated90Head);
            // rotated270Head = TwoDPoint.get270RotatedPoint(origin, bisectorHead);

            if (DRAW_BISECTOR_DASHLINES) {
                this.dashedLines.add(this.createPolyline(origin, bisectorHead, DEFAULT_DASHLINE_BISECTOR_COLOR, DEFAULT_DASHLINE_WEIGHT));
            }

            if (DRAW_VERTICAL_DASHLINES) {
                this.dashedLines.add(this.createPolyline(origin, rotated90Head, DEFAULT_DASHLINE_VERTICAL_COLOR, DEFAULT_DASHLINE_WEIGHT));
                this.dashedLines.add(this.createPolyline(origin, rotated270Head, DEFAULT_DASHLINE_VERTICAL_COLOR, DEFAULT_DASHLINE_WEIGHT));
            }
        }

        for (Polyline line : this.dashedLines) {
            this.map.addOverlay(line);
        }
    }

    private int getIndexOfMovedPoint() {
        int index = 0;
        LatLng oldPointPosition = null;
        LatLng newPointPosition = null;

        for (; index < this.polyline.getVertexCount(); index++) {
            oldPointPosition = this.turnPoints[index];
            newPointPosition = this.polyline.getVertex(index);

            if (equals(oldPointPosition, newPointPosition) == false) {
                break;
            }
        }

        return index;
    }

    private TwoDPoint toTwoDPoint(LatLng latLng) {
        Point point = this.map.convertLatLngToContainerPixel(latLng);
        return new TwoDPoint(point.getX(), point.getY());
    }

    private LatLng toLatLng(TwoDPoint point) {
        return this.map.convertContainerPixelToLatLng(Point.newInstance((int) point.getX(), (int) point.getY()));
    }

    private static PositionDTO toPositionDTO(LatLng position) {
        return new PositionDTO(position.getLatitude(), position.getLongitude());
    }

    private Polyline createPolyline(TwoDPoint first, TwoDPoint second, String color, int weight) {
        LatLng[] points = new LatLng[2];

        points[0] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) first.getX(), (int) first.getY()));
        points[1] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) second.getX(), (int) second.getY()));

        return new Polyline(points, color, weight);
    }

    private static boolean equals(LatLng first, LatLng second) {
        return (first.getLatitude() == second.getLatitude() && first.getLongitude() == second.getLongitude());
    }

    @SuppressWarnings("unused")
    private void getTotalTime_old() {
        List<PositionDTO> turnPointsAsPositionDTO = new ArrayList<PositionDTO>();

        for (LatLng point : this.turnPoints) {
            turnPointsAsPositionDTO.add(toPositionDTO(point));
        }

        RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(this.boatClassID, this.allPoints, turnPointsAsPositionDTO, USE_REAL_AVERAGE_WIND,
                STEP_DURATION_MILLISECONDS, false);

        this.simulatorService.getTotalTime_old(requestData, new AsyncCallback<ResponseTotalTimeDTO>() {

            @Override
            public void onFailure(Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(ResponseTotalTimeDTO receiveData) {
                String notificationMessage = receiveData.notificationMessage;
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    errorReporter.reportNotification(notificationMessage);
                    warningAlreadyShown = true;
                }

                long totalTime = receiveData.totalTimeSeconds;

                // System.err.println("==================================================");
                // System.err.println("total time (old way) = " + totalTime + " seconds!");
                // System.err.println("==================================================");

                simulatorMap.addLegendOverlayForPathPolyline(totalTime * 1000);
                simulatorMap.redrawLegendCanvasOverlay();
            }
        });
    }

    private void getTotalTime_new() {
        List<PositionDTO> turnPointsAsPositionDTO = new ArrayList<PositionDTO>();

        for (LatLng point : this.turnPoints) {
            turnPointsAsPositionDTO.add(toPositionDTO(point));
        }

        RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(this.boatClassID, this.allPoints, turnPointsAsPositionDTO, USE_REAL_AVERAGE_WIND,
                STEP_DURATION_MILLISECONDS, false);

        this.simulatorService.getTotalTime_new(requestData, new AsyncCallback<ResponseTotalTimeDTO>() {

            @Override
            public void onFailure(Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(ResponseTotalTimeDTO receiveData) {
                String notificationMessage = receiveData.notificationMessage;
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    errorReporter.reportNotification(notificationMessage);
                    warningAlreadyShown = true;
                }

                long totalTime = receiveData.totalTimeSeconds;

                // System.err.println("==================================================");
                // System.err.println("total time (new way) = " + totalTime + " seconds!");
                // System.err.println("==================================================");

                simulatorMap.addLegendOverlayForPathPolyline(totalTime * 1000);
                simulatorMap.redrawLegendCanvasOverlay();
            }
        });
    }

    private static double FACTOR_DEG2RAD = 0.0174532925;
    private static double FACTOR_RAD2DEG = 57.2957795;
    private static double FACTOR_KN2MPS = 0.514444;
    private static double FACTOR_MPS2KN = 1.94384;

    /**
     * Converts degress to radians
     */
    public static double degreesToRadians(double degrees) {
        return (degrees * FACTOR_DEG2RAD);
    }

    /**
     * Converts radians to degrees
     */
    public static double radiansToDegrees(double radians) {
        return (radians * FACTOR_RAD2DEG);
    }

    /**
     * Converts knots to meters per second
     */
    public static double knotsToMetersPerSecond(double knots) {
        return knots * FACTOR_KN2MPS;
    }

    /**
     * Converts meters per second to knots
     */
    public static double metersPerSecondToKnots(double metersPerSecond) {
        return metersPerSecond * FACTOR_MPS2KN;
    }

    public void setBoatClassID(int boatClassIndex) {
        this.boatClassID = boatClassIndex;
    }
}
