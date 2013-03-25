package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.maps.client.MapWidget;
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

    public final static String DEFAULT_COLOR = "#8B0000";
    private final static int DEFAULT_WEIGHT = 3;
    private final static double DEFAULT_OPACITY = 1.0;
    private final static double DEFAULT_DISTANCE_PX = 25;

    private static final boolean FIX_AGAINST_THE_WIND = true;
    private static final boolean FIX_CUT_SPIKES = true;
    private static final boolean FIX_CUT_TRIANGLES = true;
    private static final double SMOOTHNESS_MAX_DEG = 20.0;

    private final static int STEP_DURATION_MILLISECONDS = 2000;
    private final static boolean USE_REAL_AVERAGE_WIND = true;

    private Polyline polyline = null;
    private LatLng[] turnPoints = null;
    private String color = "";
    private int weight = 0;
    private double opacity = 0.0;
    private int selectedBoatClassIndex = 0;
    private int selectedRaceIndex = 0;
    private int selectedCompetitorIndex = 0;
    private int selectedLegIndex = 0;
    private List<SimulatorWindDTO> allPoints = null;
    private SimulatorServiceAsync simulatorService = null;
    private MapWidget map = null;
    private ErrorReporter errorReporter = null;
    private boolean warningAlreadyShown = false;
    private SimulatorMap simulatorMap = null;

    public static PathPolyline createPathPolyline(List<SimulatorWindDTO> pathPoints, ErrorReporter errorReporter, SimulatorServiceAsync simulatorService,
            MapWidget map, SimulatorMap simulatorMap, int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        List<LatLng> points = new ArrayList<LatLng>();

        for (SimulatorWindDTO pathPoint : pathPoints) {
            if (pathPoint.isTurn) {
                points.add(LatLng.newInstance(pathPoint.position.latDeg, pathPoint.position.lngDeg));
            }
        }

        return new PathPolyline(points.toArray(new LatLng[0]), DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, selectedBoatClassIndex, selectedRaceIndex,
                selectedCompetitorIndex, selectedLegIndex,
                errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline() {
    }

    private PathPolyline(LatLng[] points, String color, int weight, double opacity, int selectedBoatClassIndex, int selectedRaceIndex,
            int selectedCompetitorIndex, int selectedLegIndex, ErrorReporter errorReporter,
            List<SimulatorWindDTO> pathPoints, SimulatorServiceAsync simulatorService, MapWidget map, SimulatorMap simulatorMap) {

        this.turnPoints = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;
        this.allPoints = pathPoints;
        this.simulatorService = simulatorService;
        this.map = map;
        this.selectedBoatClassIndex = selectedBoatClassIndex;
        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;
        this.errorReporter = errorReporter;
        this.simulatorMap = simulatorMap;

        this.drawPolylineOnMap();
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

                LatLng oldPositionMovedPoint = turnPoints[indexOfMovedPoint];

                if (indexOfMovedPoint == 0 || indexOfMovedPoint == noOfPoints - 1) {
                    // start and end points cannot be moved!
                } else {

                    TwoDPoint newOrigin = computeNewOrigin(indexOfMovedPoint);

                    LatLng edgeStart = null;
                    LatLng edgeEnd = null;
                    LatLng beforeMovedPoint = null;
                    LatLng oldEdgeStart = null;
                    int indexOfEdgeStart = 0;
                    int indexOfEdgeEnd = 0;

                    LatLng afterEdgeEnd = null;

                    if (indexOfMovedPoint == 1) {
                        turnPoints[indexOfMovedPoint + 1] = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin), map);
                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        turnPoints[indexOfMovedPoint - 1] = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin), map);
                    } else {
                        LatLng temp = null;

                        temp = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin), map);
                        if (PathPolyline.equals(temp, turnPoints[indexOfMovedPoint - 1], 0.0001) == false) {

                            indexOfEdgeStart = indexOfMovedPoint - 1;
                            indexOfEdgeEnd = indexOfMovedPoint - 2;
                            oldEdgeStart = turnPoints[indexOfMovedPoint - 1];

                            turnPoints[indexOfMovedPoint - 1] = temp;

                            beforeMovedPoint = turnPoints[indexOfMovedPoint + 1];
                            edgeStart = turnPoints[indexOfMovedPoint - 1];
                            edgeEnd = turnPoints[indexOfMovedPoint - 2];
                            afterEdgeEnd = turnPoints[indexOfMovedPoint - 3];
                        }

                        temp = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin), map);
                        if (PathPolyline.equals(temp, turnPoints[indexOfMovedPoint + 1], 0.0001) == false) {

                            indexOfEdgeStart = indexOfMovedPoint + 1;
                            indexOfEdgeEnd = indexOfMovedPoint + 2;
                            oldEdgeStart = turnPoints[indexOfMovedPoint + 1];

                            turnPoints[indexOfMovedPoint + 1] = temp;

                            beforeMovedPoint = turnPoints[indexOfMovedPoint - 1];
                            edgeStart = turnPoints[indexOfMovedPoint + 1];
                            edgeEnd = turnPoints[indexOfMovedPoint + 2];
                            afterEdgeEnd = turnPoints[indexOfMovedPoint + 3];
                        }
                    }

                    turnPoints[indexOfMovedPoint] = toLatLng(newOrigin, map);

                    LatLng newPositionMovedPoint = turnPoints[indexOfMovedPoint];

                    if (indexOfMovedPoint != 1 && indexOfMovedPoint != noOfPoints - 2) {

                        oldPositionMovedPoint = fixOldPositionMovedPoint(beforeMovedPoint, oldPositionMovedPoint, newPositionMovedPoint);

                        if (FIX_AGAINST_THE_WIND) {
                            turnPoints = fixAgainstTheWind(turnPoints, indexOfMovedPoint, oldPositionMovedPoint, newPositionMovedPoint, beforeMovedPoint,
                                    edgeStart, edgeEnd, afterEdgeEnd, oldEdgeStart, indexOfEdgeStart, indexOfEdgeEnd);
                        }
                    }

                    if (FIX_CUT_SPIKES) {
                        turnPoints = fixCutSpikes(turnPoints);
                    }

                    if (FIX_CUT_TRIANGLES) {
                        fixCutTriangles();
                    }
                }

                drawPolylineOnMap();
            }

        });

        this.map.addOverlay(this.polyline);
        this.simulatorMap.setPolyline(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.turnPoints.length - 1));

        this.getTotalTime();
    }

    protected LatLng[] fixAgainstTheWind(LatLng[] turnPoints, int indexOfMovedPoint, LatLng oldPositionMovedPoint, LatLng newPositionMovedPoint,
            LatLng beforeMovedPoint, LatLng edgeStart, LatLng edgeEnd, LatLng afterEdgeEnd, LatLng oldEdgeStart, int indexOfEdgeStart, int indexOfEdgeEnd) {

        List<LatLng> newTurnPoints = new ArrayList<LatLng>();

        TwoDPoint beforeMoved = toTwoDPoint(beforeMovedPoint, this.map);
        TwoDPoint oldMoved = toTwoDPoint(oldPositionMovedPoint, this.map);

        TwoDPoint newMoved = toTwoDPoint(newPositionMovedPoint, this.map);

        TwoDSegment first = new TwoDSegment(beforeMoved, oldMoved);
        TwoDSegment second = new TwoDSegment(toTwoDPoint(edgeEnd, this.map), toTwoDPoint(afterEdgeEnd, this.map));

        TwoDPoint intersection = first.intersectionPointWith(second);

        TwoDSegment segment = new TwoDSegment(oldMoved, intersection);
        newMoved = segment.projectionOfPointOnLine(newMoved);
        if (segment.contains(newMoved)) {
            // System.out.println("segment contains newmoved");
            return turnPoints;
        } else {
            // System.out.println("segment does not contain newmoved");
            for (int index = 0; index < turnPoints.length; index++) {
                if (index == indexOfMovedPoint) {
                    newTurnPoints.add(toLatLng(intersection, this.map));
                } else if (index == indexOfEdgeStart || index == indexOfEdgeEnd) {
                    // newTurnPoints.add(oldEdgeStart);
                } else {
                    newTurnPoints.add(turnPoints[index]);
                }
            }
            return newTurnPoints.toArray(new LatLng[0]);
        }
    }

    private LatLng fixOldPositionMovedPoint(LatLng beforeMovedPoint, LatLng oldPositionMovedPoint, LatLng newPositionMovedPoint) {

        TwoDPoint beforePoint = toTwoDPoint(beforeMovedPoint, this.map);
        TwoDPoint point = toTwoDPoint(oldPositionMovedPoint, this.map);
        TwoDPoint afterPoint = toTwoDPoint(newPositionMovedPoint, this.map);

        TwoDSegment line = new TwoDSegment(beforePoint, afterPoint);
        TwoDPoint projection = line.projectionOfPointOnLine(point);

        return toLatLng(projection, this.map);
    }

    private double getAngleDegreesBetween(LatLng previous, LatLng current, LatLng next) {

        TwoDVector first = new TwoDVector(PathPolyline.toTwoDPoint(current, this.map), PathPolyline.toTwoDPoint(previous, this.map));
        TwoDVector second = new TwoDVector(PathPolyline.toTwoDPoint(current, this.map), PathPolyline.toTwoDPoint(next, this.map));

        double dotProduct = first.dotProduct(second);

        double length1 = first.getNorm();
        double length2 = second.getNorm();

        double denominator = length1 * length2;

        double product = denominator != 0.0 ? dotProduct / denominator : 0.0;

        double angle = Math.toDegrees(Math.acos(product));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private LatLng[] fixCutSpikes(LatLng[] turnPoints) {

        if (turnPoints.length < 4) {
            return turnPoints;
        }

        List<LatLng> points = new ArrayList<LatLng>();

        int noOfPointsMinus1 = turnPoints.length - 1;

        TwoDSegment before = null;
        TwoDSegment after = null;

        int newIndex = -1;
        TwoDPoint newAtIndex = null;

        for (int index = 2; index < noOfPointsMinus1; index++) {

            if (getAngleDegreesBetween(turnPoints[index - 1], turnPoints[index], turnPoints[index + 1]) < SMOOTHNESS_MAX_DEG) {
                before = new TwoDSegment(toTwoDPoint(turnPoints[index - 2], this.map), toTwoDPoint(turnPoints[index - 1], this.map));
                after = new TwoDSegment(toTwoDPoint(turnPoints[index], this.map), toTwoDPoint(turnPoints[index + 1], this.map));
                newAtIndex = after.intersectionPointWith(before);
                newIndex = index;
            }
        }

        for (int index = 0; index < turnPoints.length; index++) {
            if (index == newIndex - 1) {
                continue;
            } else if (index == newIndex) {
                points.add(toLatLng(newAtIndex, this.map));
            } else {
                points.add(turnPoints[index]);
            }
        }

        return points.toArray(new LatLng[0]);
    }

    private void fixCutTriangles() {
        int noOfPoints = this.turnPoints.length;
        if (noOfPoints > 3) {
            LatLng first = null;
            LatLng second = null;
            LatLng third = null;
            LatLng fourth = null;

            for (int index = 0; (index + 3) < noOfPoints; index++) {
                first = this.turnPoints[index];
                second = this.turnPoints[index + 1];
                third = this.turnPoints[index + 2];
                fourth = this.turnPoints[index + 3];

                if (TwoDPoint.areIntersecting(toTwoDPoint(first, this.map), toTwoDPoint(second, this.map), toTwoDPoint(third, this.map),
                        toTwoDPoint(fourth, this.map))) {

                    TwoDSegment firstSegment = new TwoDSegment(toTwoDPoint(first, this.map), toTwoDPoint(second, this.map));
                    TwoDSegment secondSegment = new TwoDSegment(toTwoDPoint(third, this.map), toTwoDPoint(fourth, this.map));
                    List<LatLng> newTurnPoints = new ArrayList<LatLng>();

                    for (int index2 = 0; index2 < noOfPoints; index2++) {
                        if (index2 == index + 1) {
                            continue;
                        } else if (index2 == index + 2) {
                            newTurnPoints.add(toLatLng(firstSegment.intersectionPointWith(secondSegment), this.map));
                        } else {
                            newTurnPoints.add(LatLng.newInstance(this.turnPoints[index2].getLatitude(), this.turnPoints[index2].getLongitude()));
                        }
                    }

                    this.turnPoints = null;
                    this.turnPoints = newTurnPoints.toArray(new LatLng[0]);

                    break;
                }
            }
        }
    }

    private TwoDPoint computeNewOrigin(int indexOfMovedPoint) {

        double distance = (this.map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

        TwoDPoint beforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 1], this.map);
        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint], this.map);
        TwoDPoint afterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 1], this.map);

        TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance, beforeOrigin);
        TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance, afterOrigin);

        return TwoDPoint.getCorrectProjection(origin, head1, head2, toTwoDPoint(this.polyline.getVertex(indexOfMovedPoint), this.map));
    }

    private TwoDPoint computeAfterNewOrigin(int indexOfMovedPoint, TwoDPoint newOrigin) {

        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint], this.map);
        TwoDPoint afterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 1], this.map);
        TwoDPoint afterAfterOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 2], this.map);

        TwoDSegment afterLine = new TwoDSegment(afterOrigin, afterAfterOrigin);
        TwoDVector afterVector = new TwoDVector(origin, afterOrigin);
        return TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);
    }

    private TwoDPoint computeBeforeNewOrigin(int indexOfMovedPoint, TwoDPoint newOrigin) {

        TwoDPoint beforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 1], this.map);
        TwoDPoint beforeBeforeOrigin = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 2], this.map);
        TwoDPoint origin = toTwoDPoint(this.turnPoints[indexOfMovedPoint], this.map);

        TwoDSegment beforeLine = new TwoDSegment(beforeBeforeOrigin, beforeOrigin);
        TwoDVector beforeVector = new TwoDVector(beforeOrigin, origin);

        return TwoDPoint.projectToLineByVector(newOrigin, beforeLine, beforeVector);
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

    private static TwoDPoint toTwoDPoint(LatLng latLng, MapWidget map) {
        Point point = map.convertLatLngToContainerPixel(latLng);
        return new TwoDPoint(point.getX(), point.getY());
    }

    private static LatLng toLatLng(TwoDPoint point, MapWidget map) {
        return map.convertContainerPixelToLatLng(Point.newInstance((int) point.getX(), (int) point.getY()));
    }

    private static PositionDTO toPositionDTO(LatLng position) {
        return new PositionDTO(position.getLatitude(), position.getLongitude());
    }

    private static boolean equals(LatLng first, LatLng second) {
        return (first.getLatitude() == second.getLatitude() && first.getLongitude() == second.getLongitude());
    }

    private static boolean equals(LatLng first, LatLng second, double delta) {
        double latDiff = Math.abs(first.getLatitude() - second.getLatitude());
        double lngDiff = Math.abs(first.getLongitude() - second.getLongitude());

        return latDiff <= delta && lngDiff <= delta;
    }

    private void getTotalTime() {
        List<PositionDTO> turnPointsAsPositionDTO = new ArrayList<PositionDTO>();

        for (LatLng point : this.turnPoints) {
            turnPointsAsPositionDTO.add(toPositionDTO(point));
        }

        RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(this.selectedBoatClassIndex, this.selectedRaceIndex, this.selectedCompetitorIndex,
                this.selectedLegIndex, STEP_DURATION_MILLISECONDS, this.allPoints, turnPointsAsPositionDTO, USE_REAL_AVERAGE_WIND, false);

        this.simulatorService.getTotalTime(requestData, new AsyncCallback<ResponseTotalTimeDTO>() {

            @Override
            public void onFailure(Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(ResponseTotalTimeDTO receiveData) {
                String notificationMessage = receiveData.notificationMessage;
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    //TODO: Fix errorReporter errorReporter.reportNotification(notificationMessage);
                    warningAlreadyShown = true;
                }

                long totalTime = receiveData.totalTimeSeconds;

                simulatorMap.addLegendOverlayForPathPolyline(totalTime * 1000);
                simulatorMap.redrawLegendCanvasOverlay();
            }
        });
    }

    public void setBoatClassID(int boatClassIndex) {
        this.selectedBoatClassIndex = boatClassIndex;
    }

    private static double FACTOR_KN2MPS = 0.514444;

    /**
     * Converts knots to meters per second
     */
    public static double knotsToMetersPerSecond(double knots) {
        return knots * FACTOR_KN2MPS;
    }
}
