package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.PolylineLineUpdatedHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.PolyEditingOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorUISelectionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDPoint;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDSegment;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDVector;

public class PathPolyline {

    public final static String DEFAULT_COLOR = "#8B0000";
    private final static int DEFAULT_WEIGHT = 3;
    private final static double DEFAULT_OPACITY = 1.0;
    private final static double DEFAULT_DISTANCE_PX = 25;
    private final static  double SMOOTHNESS_MAX_DEG = 20.0;
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
    private SimulatorMainPanel simulatorMainPanel = null;

    public static PathPolyline createPathPolyline(List<SimulatorWindDTO> pathPoints, ErrorReporter errorReporter, SimulatorServiceAsync simulatorService,
            MapWidget map, SimulatorMap simulatorMap, SimulatorMainPanel simulatorMainPanel, SimulatorUISelectionDTO selection) {

        List<LatLng> points = new ArrayList<LatLng>();

        for (SimulatorWindDTO pathPoint : pathPoints) {
            if (pathPoint.isTurn) {

                points.add(LatLng.newInstance(pathPoint.position.latDeg, pathPoint.position.lngDeg));
            }
        }

        return new PathPolyline(points.toArray(new LatLng[0]), DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, selection,
                errorReporter, pathPoints,
                simulatorService, map, simulatorMap, simulatorMainPanel);
    }

    private PathPolyline() {
    }

    private PathPolyline(LatLng[] points, String color, int weight, double opacity, SimulatorUISelectionDTO selection, ErrorReporter errorReporter,
            List<SimulatorWindDTO> pathPoints, SimulatorServiceAsync simulatorService, MapWidget map, SimulatorMap simulatorMap,
            SimulatorMainPanel simulatorMainPanel) {

        this.turnPoints = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;
        this.allPoints = pathPoints;
        this.simulatorService = simulatorService;
        this.map = map;
        this.selectedBoatClassIndex = selection.boatClassIndex;
        this.selectedRaceIndex = selection.raceIndex;
        this.selectedCompetitorIndex = selection.competitorIndex;
        this.selectedLegIndex = selection.legIndex;
        this.errorReporter = errorReporter;
        this.simulatorMap = simulatorMap;
        this.simulatorMainPanel = simulatorMainPanel;

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

                if (simulatorMainPanel.isPathPolylineFreeMode()) {

                    List<LatLng> newTurnPoints = new ArrayList<LatLng>();
                    for (int index = 0; index < polyline.getVertexCount(); index++) {
                        newTurnPoints.add(polyline.getVertex(index));
                    }
                    turnPoints = newTurnPoints.toArray(new LatLng[0]);

                } else {
                    final int indexOfMovedPoint = getIndexOfMovedPoint();
                    final int noOfPoints = turnPoints.length;

                    LatLng temp = null;

                    boolean secondPart = false;

                    if (indexOfMovedPoint == 0 || indexOfMovedPoint == noOfPoints - 1 || noOfPoints == 3) {
                        // start and end points cannot be moved!
                        // nor a 3-turns line.
                    } else {

                        TwoDPoint newOrigin = computeNewOrigin(indexOfMovedPoint);

                        if (indexOfMovedPoint == 1) {
                            turnPoints[indexOfMovedPoint + 1] = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin));
                            secondPart = true;
                        } else if (indexOfMovedPoint == noOfPoints - 2) {
                            turnPoints[indexOfMovedPoint - 1] = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin));
                            secondPart = false;
                        } else {

                            temp = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin));
                            if (PathPolyline.equals(temp, turnPoints[indexOfMovedPoint - 1], 0.0001) == false) {
                                secondPart = false;
                                turnPoints[indexOfMovedPoint - 1] = temp;
                            }

                            temp = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin));
                            if (PathPolyline.equals(temp, turnPoints[indexOfMovedPoint + 1], 0.0001) == false) {
                                secondPart = true;
                                turnPoints[indexOfMovedPoint + 1] = temp;
                            }
                        }

                        turnPoints[indexOfMovedPoint] = toLatLng(newOrigin);

                        turnPoints = fix_againstTheWindMovement(turnPoints, indexOfMovedPoint, secondPart);

                        turnPoints = fix_spikesOnCourse(turnPoints);

                        turnPoints = fix_trianglesOnCourse(turnPoints);
                    }

                    drawPolylineOnMap();
                }
            }
        });

        this.map.addOverlay(this.polyline);
        this.simulatorMap.setPolyline(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.turnPoints.length - 1));

        this.getTotalTime();
    }

    @SuppressWarnings("unused")
    private void addMarker(LatLng point, String title) {

        MarkerOptions options = MarkerOptions.newInstance();
        options.setDraggable(false);
        options.setTitle(title);

        Marker marker = new Marker(point, options);
        this.map.addOverlay(marker);
    }

    private LatLng[] fix_againstTheWindMovement(LatLng[] turnPoints, int indexOfMovedPoint, boolean secondPart) {

        int noOfTurnPoints = turnPoints.length;
        List<LatLng> newTurnPoints = new ArrayList<LatLng>();

        TwoDPoint neww = this.toTwoDPoint(turnPoints[indexOfMovedPoint]);
        TwoDPoint new_before = this.toTwoDPoint(turnPoints[indexOfMovedPoint - 1]);
        TwoDPoint new_after = this.toTwoDPoint(turnPoints[indexOfMovedPoint + 1]);

        boolean newList = false;

        if (secondPart) {

            TwoDPoint after_after = this.toTwoDPoint(turnPoints[indexOfMovedPoint + 2]);

            if (indexOfMovedPoint + 4 <= noOfTurnPoints) {

                TwoDPoint after_after_after = this.toTwoDPoint(turnPoints[indexOfMovedPoint + 3]);
                TwoDSegment after_edge = new TwoDSegment(after_after, after_after_after);
                TwoDSegment current_edge_before = new TwoDSegment(new_before, neww);
                TwoDPoint intersection = after_edge.intersectionPointWith(current_edge_before);
                TwoDSegment segment = new TwoDSegment(new_before, intersection);

                neww = segment.projectionOfPointOnLine(neww);

                TwoDSegment current_edge_after = new TwoDSegment(neww, new_after);
                TwoDPoint intersection2 = after_edge.intersectionPointWith(current_edge_after);
                TwoDSegment segment2 = new TwoDSegment(after_after, intersection);

                if (segment.contains(neww) == false || segment2.contains(intersection2)) {

                    newList = true;

                    for (int index = 0; index < noOfTurnPoints; index++) {
                        if (index == indexOfMovedPoint) {
                            newTurnPoints.add(this.toLatLng(intersection));
                        } else if (index == indexOfMovedPoint + 1 || index == indexOfMovedPoint + 2) {
                            // eliminate turnPoints[index + 1] as well as turnPoints[index + 2]
                        } else {
                            newTurnPoints.add(turnPoints[index]);
                        }
                    }
                }
            } else {

                TwoDSegment line = new TwoDSegment(neww, new_before);
                TwoDVector vector = new TwoDVector(neww, new_after);
                TwoDPoint projection = TwoDPoint.projectToLineByVector(after_after, line, vector);
                TwoDSegment segment = new TwoDSegment(new_before, projection);

                neww = segment.projectionOfPointOnLine(neww);

                if (segment.contains(neww) == false) {

                    newList = true;

                    for (int index = 0; index < noOfTurnPoints; index++) {
                        if (index == indexOfMovedPoint) {
                            newTurnPoints.add(this.toLatLng(projection));
                        } else if (index == indexOfMovedPoint + 1) {
                            // eliminate turnPoints[index + 1]
                        } else {
                            newTurnPoints.add(turnPoints[index]);
                        }
                    }
                }
            }

        } else {

            TwoDPoint before_before = toTwoDPoint(turnPoints[indexOfMovedPoint - 2]);

            if (indexOfMovedPoint >= 3) {

                TwoDPoint before_before_before = this.toTwoDPoint(turnPoints[indexOfMovedPoint - 3]);
                TwoDSegment before_edge = new TwoDSegment(before_before, before_before_before);
                TwoDSegment current_edge_after = new TwoDSegment(neww, new_after);
                TwoDPoint intersection = before_edge.intersectionPointWith(current_edge_after);
                TwoDSegment segment = new TwoDSegment(new_after, intersection);

                neww = segment.projectionOfPointOnLine(neww);

                TwoDSegment current_edge_before = new TwoDSegment(neww, new_before);
                TwoDPoint intersection2 = before_edge.intersectionPointWith(current_edge_before);
                TwoDSegment segment2 = new TwoDSegment(before_before, intersection);

                if (segment.contains(neww) == false || segment2.contains(intersection2)) {

                    newList = true;

                    for (int index = 0; index < noOfTurnPoints; index++) {
                        if (index == indexOfMovedPoint) {
                            newTurnPoints.add(this.toLatLng(intersection));
                        } else if (index == indexOfMovedPoint - 1 || index == indexOfMovedPoint - 2) {
                            // eliminate turnPoints[index - 1] as well as turnPoints[index - 2]
                        } else {
                            newTurnPoints.add(turnPoints[index]);
                        }
                    }
                }

            } else {

                TwoDSegment line = new TwoDSegment(neww, new_after);
                TwoDVector vector = new TwoDVector(neww, new_before);
                TwoDPoint projection = TwoDPoint.projectToLineByVector(before_before, line, vector);
                TwoDSegment segment = new TwoDSegment(new_after, projection);

                neww = segment.projectionOfPointOnLine(neww);

                if (segment.contains(neww) == false) {

                    newList = true;

                    for (int index = 0; index < noOfTurnPoints; index++) {
                        if (index == indexOfMovedPoint) {
                            newTurnPoints.add(this.toLatLng(projection));
                        } else if (index == indexOfMovedPoint - 1) {
                            // eliminate turnPoints[index - 1]
                        } else {
                            newTurnPoints.add(turnPoints[index]);
                        }
                    }
                }
            }
        }

        if (newList) {
            return newTurnPoints.toArray(new LatLng[0]);
        } else {
            return turnPoints;
        }
    }

    private double getAngleDegreesBetween(LatLng previous, LatLng current, LatLng next) {

        TwoDVector first = new TwoDVector(this.toTwoDPoint(current), this.toTwoDPoint(previous));
        TwoDVector second = new TwoDVector(this.toTwoDPoint(current), this.toTwoDPoint(next));

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

    private LatLng[] fix_spikesOnCourse(LatLng[] turnPoints) {

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

            if (this.getAngleDegreesBetween(turnPoints[index - 1], turnPoints[index], turnPoints[index + 1]) < SMOOTHNESS_MAX_DEG) {
                before = new TwoDSegment(toTwoDPoint(turnPoints[index - 2]), toTwoDPoint(turnPoints[index - 1]));
                after = new TwoDSegment(toTwoDPoint(turnPoints[index]), toTwoDPoint(turnPoints[index + 1]));
                newAtIndex = after.intersectionPointWith(before);
                newIndex = index;
            }
        }

        for (int index = 0; index < turnPoints.length; index++) {
            if (index == newIndex - 1) {
                continue;
            } else if (index == newIndex) {
                points.add(toLatLng(newAtIndex));
            } else {
                points.add(turnPoints[index]);
            }
        }

        return points.toArray(new LatLng[0]);
    }

    private LatLng[] fix_trianglesOnCourse(LatLng[] turnPoints) {
        int noOfPoints = turnPoints.length;

        if (noOfPoints < 4) {
            return turnPoints;
        }

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
                        newTurnPoints.add(turnPoints[index2]);
                    }
                }

                return newTurnPoints.toArray(new LatLng[0]);
            }
        }

        return turnPoints;
    }

    private TwoDPoint computeNewOrigin(int indexOfMovedPoint) {

        double distance = (this.map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

        TwoDPoint beforeMovedPoint = toTwoDPoint(this.turnPoints[indexOfMovedPoint - 1]);
        TwoDPoint oldPositionMovedPoint = toTwoDPoint(this.turnPoints[indexOfMovedPoint]);
        TwoDPoint afterMovedPoint = toTwoDPoint(this.turnPoints[indexOfMovedPoint + 1]);
        TwoDPoint newPositionMovedPoint = toTwoDPoint(this.polyline.getVertex(indexOfMovedPoint));

        TwoDPoint head1 = oldPositionMovedPoint.getDistancedPoint(distance, beforeMovedPoint);
        TwoDSegment oh1 = new TwoDSegment(oldPositionMovedPoint, head1);
        TwoDPoint p1 = oh1.projectionOfPointOnLine(newPositionMovedPoint);
        double d1 = newPositionMovedPoint.distanceBetween(p1);

        TwoDPoint head2 = oldPositionMovedPoint.getDistancedPoint(distance, afterMovedPoint);
        TwoDSegment oh2 = new TwoDSegment(oldPositionMovedPoint, head2);
        TwoDPoint p2 = oh2.projectionOfPointOnLine(newPositionMovedPoint);
        double d2 = newPositionMovedPoint.distanceBetween(p2);

        if (indexOfMovedPoint == 1) {
            return p1;
        } else if (indexOfMovedPoint == this.turnPoints.length - 2) {
            return p2;
        } else {
            return (d1 < d2) ? p1 : p2;
        }
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

    private int getIndexOfMovedPoint() {

        int index = 0;
        int noOfVertexes = this.polyline.getVertexCount();
        LatLng oldPointPosition = null;
        LatLng newPointPosition = null;

        for (; index < noOfVertexes; index++) {
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

        RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(new SimulatorUISelectionDTO(this.selectedBoatClassIndex, this.selectedRaceIndex,
                this.selectedCompetitorIndex, this.selectedLegIndex), STEP_DURATION_MILLISECONDS, this.allPoints, turnPointsAsPositionDTO,
                USE_REAL_AVERAGE_WIND);

        this.simulatorService.getTotalTime(requestData, new AsyncCallback<ResponseTotalTimeDTO>() {

            @Override
            public void onFailure(Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(ResponseTotalTimeDTO receiveData) {
                String notificationMessage = receiveData.notificationMessage;
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    errorReporter.reportError(notificationMessage, true);
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
