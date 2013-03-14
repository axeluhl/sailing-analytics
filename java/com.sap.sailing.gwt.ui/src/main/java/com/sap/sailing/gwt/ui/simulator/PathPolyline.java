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
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
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

    private static int STEP_DURATION_MILLISECONDS = 2000;
    private static boolean USE_REAL_AVERAGE_WIND = true;

    private static boolean FIX_CUT_SPIKES = true;
    private static boolean FIX_CUT_TRIANGLES = true;
    private static boolean ADD_1_TURNER = true;
    private static double SMOOTHNESS_MAX_DEG = 20.0;

    // private int movedPointIndex = 0;
    // private int alsoMovedPointIndex = 0;

    public static PathPolyline createPathPolyline(List<SimulatorWindDTO> pathPoints, ErrorReporter errorReporter, SimulatorServiceAsync simulatorService,
            MapWidget map, SimulatorMap simulatorMap, int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        List<LatLng> points = new ArrayList<LatLng>();

        for (SimulatorWindDTO pathPoint : pathPoints) {
            if (pathPoint.isTurn) {
                points.add(LatLng.newInstance(pathPoint.position.latDeg, pathPoint.position.lngDeg));
            }
        }

        return new PathPolyline(points.toArray(new LatLng[0]), selectedBoatClassIndex, selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex,
                errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline() {
    }

    private PathPolyline(LatLng[] points, int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex,
            ErrorReporter errorReporter, List<SimulatorWindDTO> pathPoints,
            SimulatorServiceAsync simulatorService, MapWidget map, SimulatorMap simulatorMap) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, selectedBoatClassIndex, selectedRaceIndex, selectedCompetitorIndex,
                selectedLegIndex, errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    private PathPolyline(LatLng[] points, String color, int weight, double opacity, int selectedBoatClassIndex, int selectedRaceIndex,
            int selectedCompetitorIndex, int selectedLegIndex, ErrorReporter errorReporter,
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
        this.selectedBoatClassIndex = selectedBoatClassIndex;
        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;
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

    @SuppressWarnings("unused")
    private void printTurnPoints() {
        System.out.println("-----------------------------");

        for (int index = 0; index < this.turnPoints.length; index++) {
            System.out.println("Point no. " + index + " at " + this.turnPoints[index].toString());
        }

        System.out.println("-----------------------------");
    }

    private void addMarker(LatLng point, String title) {

        MarkerOptions options = MarkerOptions.newInstance();
        options.setDraggable(false);
        options.setTitle(title);

        Marker marker = new Marker(point, options);
        this.map.addOverlay(marker);
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

                // System.out.println("indexOfMovedPoint = " + indexOfMovedPoint);

                final int noOfPoints = turnPoints.length;

                final LatLng originalOriginLatLng = turnPoints[indexOfMovedPoint];

                // addMarker(originalOriginLatLng, "original position of moved point");

                // addMarker(polyline.getVertex(indexOfMovedPoint), "proposed position of moved point");

                if (indexOfMovedPoint == 0 || indexOfMovedPoint == noOfPoints - 1) {
                    // start and end points cannot be moved!
                } else {
                    TwoDPoint newOrigin = computeNewOrigin(indexOfMovedPoint);
                    final boolean projectionOnBeforeLine = projectedToBeforeLine(indexOfMovedPoint);
                    System.out.println("projectionOnBeforeLine = " + projectionOnBeforeLine);

                    LatLng edgeStart = null;
                    LatLng edgeEnd = null;

                    if (indexOfMovedPoint == 1) {
                        turnPoints[indexOfMovedPoint + 1] = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin));
                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        turnPoints[indexOfMovedPoint - 1] = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin));
                    } else {
                        LatLng temp = null;

                        temp = toLatLng(computeBeforeNewOrigin(indexOfMovedPoint, newOrigin));
                        if (PathPolyline.equals(temp, turnPoints[indexOfMovedPoint - 1], 0.0001) == false) {
                            // System.out.println("aici1");
                            turnPoints[indexOfMovedPoint - 1] = temp;

                            edgeStart = turnPoints[indexOfMovedPoint - 1];
                            edgeEnd = turnPoints[indexOfMovedPoint - 2];
                        }

                        temp = toLatLng(computeAfterNewOrigin(indexOfMovedPoint, newOrigin));
                        if (PathPolyline.equals(temp, turnPoints[indexOfMovedPoint + 1], 0.0001) == false) {
                            // System.out.println("aici2");
                            turnPoints[indexOfMovedPoint + 1] = temp;

                            edgeStart = turnPoints[indexOfMovedPoint + 1];
                            edgeEnd = turnPoints[indexOfMovedPoint + 2];
                        }
                    }

                    // addMarker(edgeStart, "edge start");
                    // addMarker(edgeEnd, "edge end");

                    turnPoints[indexOfMovedPoint] = toLatLng(newOrigin);

                    // addMarker(toLatLng(newOrigin), "new position of moved point");

                    LatLng newOriginLatLng = turnPoints[indexOfMovedPoint];

                    if (FIX_CUT_SPIKES) {
                        turnPoints = fixCutSpikes(turnPoints);
                    }

                    if (FIX_CUT_TRIANGLES) {
                        fixCutTriangles();
                    }

                    if(ADD_1_TURNER) {
                        add1Turner(originalOriginLatLng, newOriginLatLng, edgeStart, edgeEnd, !projectionOnBeforeLine, turnPoints);
                    }
                }

                drawPolylineOnMap();
                drawDashLinesOnMap(map.getZoomLevel());
            }
        });

        this.map.addOverlay(this.polyline);
        this.simulatorMap.setPolyline(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.turnPoints.length - 1));

        this.getTotalTime();
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

    private long getTimePointOf(LatLng point) {

        long timepoint = 0L;
        for (SimulatorWindDTO item : this.allPoints) {
            if (item.position.latDeg == point.getLatitude() && item.position.lngDeg == point.getLongitude()) {
                timepoint = item.timepoint;
                break;
            }
        }
        return timepoint;
    }

    private static int getIndexOf(LatLng item, LatLng[] array) {
        int index = 0;
        for (LatLng element : array) {
            if (element.getLatitude() == item.getLatitude() && element.getLongitude() == item.getLongitude()) {
                break;
            }
            index++;
        }

        return index;
    }

    private void add1Turner(final LatLng originalOriginLng, final LatLng newOriginLng, LatLng edgeStart, LatLng edgeEnd, boolean leftSide, final LatLng[] turnPoints) {

        final int indexOfEdgeStart = getIndexOf(edgeStart, turnPoints);
        // System.out.println("indexOfEdgeStart = " + indexOfEdgeStart);

        // int indexOfEdgeEnd = getIndexOf(edgeEnd, turnPoints);
        // System.out.println("indexOfEdgeEnd = " + indexOfEdgeEnd);

        final int indexOfMovedPoint = getIndexOf(newOriginLng, turnPoints);
        // System.out.println("indexOfMovedPoint = " + indexOfMovedPoint);

        final int noOfTurnPoints = turnPoints.length;

        PositionDTO firstPoint = toPositionDTO(originalOriginLng);
        PositionDTO secondPoint = toPositionDTO(newOriginLng);

        PositionDTO edgeStartPoint = toPositionDTO(edgeStart);
        PositionDTO edgeEndPoint = toPositionDTO(edgeEnd);

        long timepoint = this.getTimePointOf(originalOriginLng);

        Request1TurnerDTO requestData = new Request1TurnerDTO(this.selectedBoatClassIndex, this.selectedRaceIndex, this.selectedCompetitorIndex, this.selectedLegIndex,
                firstPoint, timepoint, secondPoint, !leftSide, edgeStartPoint, edgeEndPoint);

        this.simulatorService.get1Turner(requestData, new AsyncCallback<Response1TurnerDTO>() {

            @Override
            public void onFailure(Throwable error) {
                System.err.println("Failed to compute the 1-turner!\r\n" + error.getMessage());
                //errorReporter.reportError("Failed to compute the 1-turner!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(Response1TurnerDTO receiveData) {

                // System.out.println("succes");

                SimulatorWindDTO oneTurner = receiveData.oneTurner;
                // System.out.println("One turner at (" + oneTurner.position.latDeg + ", " + oneTurner.position.lngDeg +
                // ")");
                addMarker(LatLng.newInstance(oneTurner.position.latDeg, oneTurner.position.lngDeg), "One turner");

                SimulatorWindDTO intersection = receiveData.intersection;
                // System.out.println("Intersection at (" + intersection.position.latDeg + ", " +
                // intersection.position.lngDeg + ")");
                addMarker(LatLng.newInstance(intersection.position.latDeg, intersection.position.lngDeg), "Intersection");

                List<LatLng> newTurnPoints = new ArrayList<LatLng>();

                if (indexOfEdgeStart < indexOfMovedPoint) {

                    for (int index = 0; index < noOfTurnPoints; index++) {
                        if (index == indexOfEdgeStart) {
                            newTurnPoints.add(LatLng.newInstance(intersection.position.latDeg, intersection.position.lngDeg));
                        } else if (index == indexOfMovedPoint) {
                            newTurnPoints.add(newOriginLng);
                            newTurnPoints.add(LatLng.newInstance(oneTurner.position.latDeg, oneTurner.position.lngDeg));
                            newTurnPoints.add(originalOriginLng);
                        } else {
                            newTurnPoints.add(turnPoints[index]);
                        }
                    }

                } else {

                    for (int index = 0; index < noOfTurnPoints; index++) {
                        if (index == indexOfEdgeStart) {
                            newTurnPoints.add(LatLng.newInstance(intersection.position.latDeg, intersection.position.lngDeg));
                        } else if (index == indexOfMovedPoint) {
                            newTurnPoints.add(originalOriginLng);
                            newTurnPoints.add(LatLng.newInstance(oneTurner.position.latDeg, oneTurner.position.lngDeg));
                            newTurnPoints.add(newOriginLng);
                        } else {
                            newTurnPoints.add(turnPoints[index]);
                        }
                    }
                }

                PathPolyline.this.turnPoints = newTurnPoints.toArray(new LatLng[0]);
                drawPolylineOnMap();
            }
        });
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
        this.selectedBoatClassIndex = boatClassIndex;
    }
}
