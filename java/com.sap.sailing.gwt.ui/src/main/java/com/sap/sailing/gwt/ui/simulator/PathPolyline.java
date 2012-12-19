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
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDPoint;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDSegment;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDVector;

public class PathPolyline {
    private static final boolean DRAW_DISTANCED_DASHLINES = false;
    private static final boolean DRAW_BISECTOR_DASHLINES = false;
    private static final boolean DRAW_VERTICAL_DASHLINES = false;
    private static final boolean DRAW_SHADOW_DASHLINES = false;

    public static final String DEFAULT_COLOR = "#8B0000";
    private static final int DEFAULT_WEIGHT = 3;
    private static final double DEFAULT_OPACITY = 1.0;
    private static final double DEFAULT_DISTANCE_PX = 25;

    private static final int DEFAULT_DASHLINE_WEIGHT = 1;
    private static final String DEFAULT_DASHLINE_DISTANCED_COLOR = "Red";
    private static final String DEFAULT_DASHLINE_BISECTOR_COLOR = "Green";
    private static final String DEFAULT_DASHLINE_VERTICAL_COLOR = "Blue";

    private Polyline polyline = null;
    private LatLng[] turnPoints = null;

    private Polyline shadowPolyline = null;
    private List<LatLng> shadowPoints = null;
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

    public static PathPolyline createPathPolyline(final List<SimulatorWindDTO> pathPoints, final ErrorReporter errorReporter,
            final SimulatorServiceAsync simulatorService, final MapWidget map, final SimulatorMap simulatorMap, final int boatClassID) {

        final List<LatLng> points = new ArrayList<LatLng>();

        for (final SimulatorWindDTO pathPoint : pathPoints) {
            if (pathPoint.isTurn) {
                points.add(LatLng.newInstance(pathPoint.position.latDeg, pathPoint.position.lngDeg));
            }
        }

        return new PathPolyline(points.toArray(new LatLng[0]), boatClassID, errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline() {
    }

    private PathPolyline(final LatLng[] points, final int boatClassID, final ErrorReporter errorReporter, final List<SimulatorWindDTO> pathPoints,
            final SimulatorServiceAsync simulatorService, final MapWidget map, final SimulatorMap simulatorMap) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, boatClassID, errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline(final LatLng[] points, final String color, final int weight, final double opacity, final int boatClassID,
            final ErrorReporter errorReporter, final List<SimulatorWindDTO> pathPoints, final SimulatorServiceAsync simulatorService, final MapWidget map,
            final SimulatorMap simulatorMap) {
        this.turnPoints = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;

        this.dashedLines = new ArrayList<Polyline>();
        this.originAndHeads = new HashMap<TwoDPoint, List<TwoDPoint>>();
        this.shadowPoints = new ArrayList<LatLng>();

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
            public void onZoomEnd(final MapZoomEndEvent event) {
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
            public void onUpdate(final PolylineLineUpdatedEvent event) {
                final int indexOfMovedPoint = getIndexOfMovedPoint();
                final int noOfPoints = turnPoints.length;

                if (indexOfMovedPoint == 0 || indexOfMovedPoint == noOfPoints - 1) {
                    // start and end points cannot be moved!
                } else {
                    // boolean isOnTheInside = TwoDPoint.isOnTheInside(
                    // convertToTwoDPoint(points[indexOfMovedPoint - 1]),
                    // convertToTwoDPoint(points[indexOfMovedPoint]),
                    // convertToTwoDPoint(points[indexOfMovedPoint - 1]),
                    // convertToTwoDPoint(polyline.getVertex(indexOfMovedPoint)));
                    // System.out.println("\r\nXXX: Moved point: " + indexOfMovedPoint);
                    // System.out.println("\r\nXXX: Is on the inside: " + isOnTheInside);

                    final double distance = (map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

                    final TwoDPoint origin = convertToTwoDPoint(turnPoints[indexOfMovedPoint]);
                    final TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance, convertToTwoDPoint(turnPoints[indexOfMovedPoint - 1]));
                    final TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance, convertToTwoDPoint(turnPoints[indexOfMovedPoint + 1]));
                    final TwoDPoint newOrigin = TwoDPoint.getCorrectProjection(origin, head1, head2, convertToTwoDPoint(polyline.getVertex(indexOfMovedPoint)));

                    shadowPoints.clear();
                    for (final LatLng point : turnPoints) {
                        shadowPoints.add(point);
                    }

                    TwoDSegment afterLine = null;
                    TwoDVector afterVector = null;
                    TwoDPoint afterNewOrigin = null;
                    TwoDSegment beforeLine = null;
                    TwoDVector beforeVector = null;
                    TwoDPoint beforeNewOrigin = null;

                    if (indexOfMovedPoint == 1) {
                        afterLine = new TwoDSegment(convertToTwoDPoint(turnPoints[indexOfMovedPoint + 1]), convertToTwoDPoint(turnPoints[indexOfMovedPoint + 2]));
                        afterVector = new TwoDVector(origin, convertToTwoDPoint(turnPoints[indexOfMovedPoint + 1]));
                        afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        turnPoints[indexOfMovedPoint] = convertToLatLng(newOrigin);
                        turnPoints[indexOfMovedPoint + 1] = convertToLatLng(afterNewOrigin);

                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        beforeLine = new TwoDSegment(convertToTwoDPoint(turnPoints[indexOfMovedPoint - 2]), convertToTwoDPoint(turnPoints[indexOfMovedPoint - 1]));
                        beforeVector = new TwoDVector(convertToTwoDPoint(turnPoints[indexOfMovedPoint - 1]), origin);
                        beforeNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        turnPoints[indexOfMovedPoint - 1] = convertToLatLng(beforeNewOrigin);
                        turnPoints[indexOfMovedPoint] = convertToLatLng(newOrigin);
                    } else {
                        beforeLine = new TwoDSegment(convertToTwoDPoint(turnPoints[indexOfMovedPoint - 2]), convertToTwoDPoint(turnPoints[indexOfMovedPoint - 1]));
                        beforeVector = new TwoDVector(convertToTwoDPoint(turnPoints[indexOfMovedPoint - 1]), origin);
                        beforeNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        afterLine = new TwoDSegment(convertToTwoDPoint(turnPoints[indexOfMovedPoint + 1]), convertToTwoDPoint(turnPoints[indexOfMovedPoint + 2]));
                        afterVector = new TwoDVector(origin, convertToTwoDPoint(turnPoints[indexOfMovedPoint + 1]));
                        afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        turnPoints[indexOfMovedPoint - 1] = convertToLatLng(beforeNewOrigin);
                        turnPoints[indexOfMovedPoint] = convertToLatLng(newOrigin);
                        turnPoints[indexOfMovedPoint + 1] = convertToLatLng(afterNewOrigin);
                    }
                }

                drawPolylineOnMap();
                drawDashLinesOnMap(map.getZoomLevel());
            }
        });

        this.map.addOverlay(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.turnPoints.length - 1));

        this.getTotalTime();
        this.getTotalTime2();
    }

    private void drawDashLinesOnMap(final int zoomLevel) {
        final int noOfPathPoints = this.turnPoints.length;
        if (noOfPathPoints == 0 || noOfPathPoints == 1 || noOfPathPoints == 2) {
            return;
        }

        for (final Polyline line : this.dashedLines) {
            this.map.removeOverlay(line);
        }
        this.dashedLines.clear();

        TwoDPoint distancedFromPoint = null;
        TwoDPoint startPoint = null;
        TwoDPoint distancedPoint = null;
        Point temp = null;

        this.originAndHeads.clear();

        final double distance = (zoomLevel - 11) * DEFAULT_DISTANCE_PX;

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

        for (final Entry<TwoDPoint, List<TwoDPoint>> entry : this.originAndHeads.entrySet()) {
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

        for (final Polyline line : this.dashedLines) {
            this.map.addOverlay(line);
        }

        if (DRAW_SHADOW_DASHLINES) {
            if (this.shadowPolyline != null) {
                this.map.removeOverlay(this.shadowPolyline);
            }

            this.shadowPolyline = new Polyline(this.shadowPoints.toArray(new LatLng[0]), DEFAULT_COLOR, DEFAULT_DASHLINE_WEIGHT, DEFAULT_OPACITY);
            this.map.addOverlay(this.shadowPolyline);
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

    private TwoDPoint convertToTwoDPoint(final LatLng latLng) {
        final Point point = this.map.convertLatLngToContainerPixel(latLng);
        return new TwoDPoint(point.getX(), point.getY());
    }

    private LatLng convertToLatLng(final TwoDPoint point) {
        return this.map.convertContainerPixelToLatLng(Point.newInstance((int) point.getX(), (int) point.getY()));
    }

    private static PositionDTO convertToPositionDTO(final LatLng position) {
        return new PositionDTO(position.getLatitude(), position.getLongitude());
    }

    private Polyline createPolyline(final TwoDPoint first, final TwoDPoint second, final String color, final int weight) {
        final LatLng[] points = new LatLng[2];

        points[0] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) first.getX(), (int) first.getY()));
        points[1] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) second.getX(), (int) second.getY()));

        return new Polyline(points, color, weight);
    }

    private static boolean equals(final LatLng first, final LatLng second) {
        return (first.getLatitude() == second.getLatitude() && first.getLongitude() == second.getLongitude());
    }

    private void getTotalTime() {
        final List<PositionDTO> turnPointsAsPositionDTO = new ArrayList<PositionDTO>();

        for (final LatLng point : this.turnPoints) {
            turnPointsAsPositionDTO.add(convertToPositionDTO(point));
        }

        final RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(this.boatClassID, this.allPoints, turnPointsAsPositionDTO);

        this.simulatorService.getTotalTime(requestData, new AsyncCallback<ResponseTotalTimeDTO>() {

            @Override
            public void onFailure(final Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(final ResponseTotalTimeDTO receiveData) {
                final String notificationMessage = receiveData.notificationMessage;
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    errorReporter.reportNotification(notificationMessage);
                    warningAlreadyShown = true;
                }

                final long totalTime = receiveData.totalTimeSeconds;

                System.err.println("==================================================");
                System.err.println("total time (old way) = " + totalTime + " seconds!");
                System.err.println("==================================================");

                simulatorMap.addLegendOverlayForPathPolyline(totalTime * 1000);
                simulatorMap.redrawLegendCanvasOverlay();
            }
        });
    }

    private void getTotalTime2() {
        final List<PositionDTO> turnPointsAsPositionDTO = new ArrayList<PositionDTO>();

        for (final LatLng point : this.turnPoints) {
            turnPointsAsPositionDTO.add(convertToPositionDTO(point));
        }

        final RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(this.boatClassID, this.allPoints, turnPointsAsPositionDTO);

        this.simulatorService.getTotalTime2(requestData, new AsyncCallback<ResponseTotalTimeDTO>() {

            @Override
            public void onFailure(final Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(final ResponseTotalTimeDTO receiveData) {
                final String notificationMessage = receiveData.notificationMessage;
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    errorReporter.reportNotification(notificationMessage);
                    warningAlreadyShown = true;
                }

                final long totalTime = receiveData.totalTimeSeconds;

                System.err.println("==================================================");
                System.err.println("total time (new way) = " + totalTime + " seconds!");
                System.err.println("==================================================");

                // simulatorMap.addLegendOverlayForPathPolyline(totalTime * 1000);
                // simulatorMap.redrawLegendCanvasOverlay();
            }
        });
    }

    private static final double FACTOR_DEG2RAD = 0.0174532925;
    private static final double FACTOR_RAD2DEG = 57.2957795;
    private static final double FACTOR_KN2MPS = 0.514444;
    private static final double FACTOR_MPS2KN = 1.94384;

    /**
     * Converts degress to radians
     */
    public static double degreesToRadians(final double degrees) {
        return (degrees * FACTOR_DEG2RAD);
    }

    /**
     * Converts radians to degrees
     */
    public static double radiansToDegrees(final double radians) {
        return (radians * FACTOR_RAD2DEG);
    }

    /**
     * Converts knots to meters per second
     */
    public static double knotsToMetersPerSecond(final double knots) {
        return knots * FACTOR_KN2MPS;
    }

    /**
     * Converts meters per second to knots
     */
    public static double metersPerSecondToKnots(final double metersPerSecond) {
        return metersPerSecond * FACTOR_MPS2KN;
    }
}
