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
import com.sap.sailing.gwt.ui.shared.ReceivePolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.RequestPolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.SpeedBearingPositionDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
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

    private static final double STEP_DURATION_MILLISECONDS = 1000;
    private static final boolean USE_ONLY_TURN_POINTS = false;

    private Polyline polyline;
    private final LatLng[] points;

    private Polyline shadowPolyline;
    private final List<LatLng> shadowPoints;
    private final List<Polyline> dashedLines;

    private final String color;
    private final int weight;
    private final double opacity;
    private final Map<TwoDPoint, List<TwoDPoint>> originAndHeads;
    private SpeedWithBearingDTO averageWindSpeed;

    private final int boatClassID;
    private final List<SimulatorWindDTO> pathPoints;
    private final SimulatorServiceAsync simulatorService;
    private final MapWidget map;
    private final ErrorReporter errorReporter;
    private boolean warningAlreadyShown = false;

    private boolean firstTime = true;

    private SimulatorMap simulatorMap = null;

    private double stepSizeMeters = 0.0;

    public PathPolyline(final LatLng[] points, final int boatClassID, final ErrorReporter errorReporter, final List<SimulatorWindDTO> pathPoints,
            final SimulatorServiceAsync simulatorService, final MapWidget map, final SimulatorMap simulatorMap) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, boatClassID, errorReporter, pathPoints, simulatorService, map, simulatorMap);
    }

    public PathPolyline(final LatLng[] points, final String color, final int weight, final double opacity, final int boatClassID,
            final ErrorReporter errorReporter, final List<SimulatorWindDTO> pathPoints, final SimulatorServiceAsync simulatorService, final MapWidget map,
            final SimulatorMap simulatorMap) {
        this.points = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;

        this.dashedLines = new ArrayList<Polyline>();
        this.originAndHeads = new HashMap<TwoDPoint, List<TwoDPoint>>();
        this.shadowPoints = new ArrayList<LatLng>();

        this.pathPoints = pathPoints;
        this.simulatorService = simulatorService;
        this.map = map;
        this.averageWindSpeed = new SpeedWithBearingDTO();
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

        this.polyline = new Polyline(this.points, this.color, this.weight, this.opacity);

        this.polyline.addPolylineLineUpdatedHandler(new PolylineLineUpdatedHandler() {
            @Override
            public void onUpdate(final PolylineLineUpdatedEvent event) {
                final int indexOfMovedPoint = getIndexOfMovedPoint();
                final int noOfPoints = points.length;

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

                    final TwoDPoint origin = convertToTwoDPoint(points[indexOfMovedPoint]);
                    final TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance, convertToTwoDPoint(points[indexOfMovedPoint - 1]));
                    final TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance, convertToTwoDPoint(points[indexOfMovedPoint + 1]));
                    final TwoDPoint newOrigin = TwoDPoint.getCorrectProjection(origin, head1, head2, convertToTwoDPoint(polyline.getVertex(indexOfMovedPoint)));

                    shadowPoints.clear();
                    for (final LatLng point : points) {
                        shadowPoints.add(point);
                    }

                    TwoDSegment afterLine = null;
                    TwoDVector afterVector = null;
                    TwoDPoint afterNewOrigin = null;
                    TwoDSegment beforeLine = null;
                    TwoDVector beforeVector = null;
                    TwoDPoint beforeNewOrigin = null;

                    if (indexOfMovedPoint == 1) {
                        afterLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint + 1]), convertToTwoDPoint(points[indexOfMovedPoint + 2]));
                        afterVector = new TwoDVector(origin, convertToTwoDPoint(points[indexOfMovedPoint + 1]));
                        afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        points[indexOfMovedPoint] = convertToLatLng(newOrigin);
                        points[indexOfMovedPoint + 1] = convertToLatLng(afterNewOrigin);

                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        beforeLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint - 2]), convertToTwoDPoint(points[indexOfMovedPoint - 1]));
                        beforeVector = new TwoDVector(convertToTwoDPoint(points[indexOfMovedPoint - 1]), origin);
                        beforeNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        points[indexOfMovedPoint - 1] = convertToLatLng(beforeNewOrigin);
                        points[indexOfMovedPoint] = convertToLatLng(newOrigin);
                    } else {
                        beforeLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint - 2]), convertToTwoDPoint(points[indexOfMovedPoint - 1]));
                        beforeVector = new TwoDVector(convertToTwoDPoint(points[indexOfMovedPoint - 1]), origin);
                        beforeNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        afterLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint + 1]), convertToTwoDPoint(points[indexOfMovedPoint + 2]));
                        afterVector = new TwoDVector(origin, convertToTwoDPoint(points[indexOfMovedPoint + 1]));
                        afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        points[indexOfMovedPoint - 1] = convertToLatLng(beforeNewOrigin);
                        points[indexOfMovedPoint] = convertToLatLng(newOrigin);
                        points[indexOfMovedPoint + 1] = convertToLatLng(afterNewOrigin);
                    }
                }

                drawPolylineOnMap();
                drawDashLinesOnMap(map.getZoomLevel());
            }
        });

        this.map.addOverlay(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.points.length - 1));

        if (this.firstTime) {

            this.averageWindSpeed = this.getAverageWindSpeed();
            this.stepSizeMeters = PathPolylineUtils.knotsToMetersPerSecond(this.averageWindSpeed.speedInKnots) * (STEP_DURATION_MILLISECONDS / 1000);
            this.firstTime = false;
        }

        this.computeTotalTimeOfPathPolyline();
    }

    private void drawDashLinesOnMap(final int zoomLevel) {
        final int noOfPathPoints = this.points.length;
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
            temp = this.map.convertLatLngToContainerPixel(this.points[index + 1]);
            distancedFromPoint = new TwoDPoint(temp.getX(), temp.getY());

            temp = this.map.convertLatLngToContainerPixel(this.points[index]);
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
            temp = this.map.convertLatLngToContainerPixel(this.points[index - 1]);
            distancedFromPoint = new TwoDPoint(temp.getX(), temp.getY());

            temp = this.map.convertLatLngToContainerPixel(this.points[index]);
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
            oldPointPosition = this.points[index];
            newPointPosition = this.polyline.getVertex(index);

            if (equalsLatLng(oldPointPosition, newPointPosition) == false) {
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

    private LatLng convertToLatLng(final PositionDTO position) {
        return LatLng.newInstance(position.latDeg, position.lngDeg);
    }

    private Polyline createPolyline(final TwoDPoint first, final TwoDPoint second, final String color, final int weight) {
        final LatLng[] points = new LatLng[2];

        points[0] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) first.getX(), (int) first.getY()));
        points[1] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) second.getX(), (int) second.getY()));

        return new Polyline(points, color, weight);
    }

    private static boolean equalsLatLng(final LatLng first, final LatLng second) {
        return (first.getLatitude() == second.getLatitude() && first.getLongitude() == second.getLongitude());
    }

    private void computeTotalTimeOfPathPolyline() {

        final RequestPolarDiagramDataDTO requestData = this.createRequestPolarDiagramDataDTO();

        this.simulatorService.getSpeedsFromPolarDiagram(requestData, new AsyncCallback<ReceivePolarDiagramDataDTO>() {

            @Override
            public void onFailure(final Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(final ReceivePolarDiagramDataDTO receiveData) {
                final String notificationMessage = receiveData.getNotificationMessage();
                if (notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
                    errorReporter.reportNotification(notificationMessage);
                    warningAlreadyShown = true;
                }

                final List<SpeedBearingPositionDTO> speeds = receiveData.getSpeedsBearingsPositions();
                final int noOfSpeeds = speeds.size();

                SpeedBearingPositionDTO startSpeedAndPosition = null;
                SpeedBearingPositionDTO endSpeedAndPosition = null;
                LatLng startPoint = null;
                LatLng endPoint = null;
                double endSpeed = 0.0;
                double totalTime = 0.0;

                for (int index = 0; index < noOfSpeeds; index++) {

                    if (index == noOfSpeeds - 1) {
                        break;
                    }

                    startSpeedAndPosition = speeds.get(index);
                    endSpeedAndPosition = speeds.get(index + 1);

                    startPoint = convertToLatLng(startSpeedAndPosition.getPosition());
                    endPoint = convertToLatLng(endSpeedAndPosition.getPosition());
                    endSpeed = PathPolylineUtils.knotsToMetersPerSecond(endSpeedAndPosition.getSpeedWithBearing().speedInKnots);

                    totalTime += PathPolylineUtils.getTime(startPoint, endPoint, endSpeed);
                }

                System.err.println("==================================================");
                System.err.println("total distance = " + PathPolylineUtils.getTotalDistanceMeters(points) + " meters");
                System.err.println("step size = " + stepSizeMeters + " meters");
                System.err.println("average wind speed = " + PathPolylineUtils.knotsToMetersPerSecond(averageWindSpeed.speedInKnots)
                        + " meters/second, with bearing of " + averageWindSpeed.bearingInDegrees + " degrees");
                System.err.println("total time = " + (long) totalTime + " seconds!");
                System.err.println("==================================================");


                simulatorMap.addLegendOverlayForPathPolyline(((long) totalTime) * 1000);
                simulatorMap.redrawLegendCanvasOverlay();
            }
        });
    }

    private SpeedWithBearingDTO getAverageWindSpeed() {
        return PathPolylineUtils.getAverage(this.pathPoints);
    }

    private RequestPolarDiagramDataDTO createRequestPolarDiagramDataDTO() {

        final List<PositionDTO> positions = new ArrayList<PositionDTO>();

        if (USE_ONLY_TURN_POINTS) {
            for (final LatLng point : this.points) {
                positions.add(new PositionDTO(point.getLatitude(), point.getLongitude()));
            }
        } else {
            for (final LatLng point : PathPolylineUtils.getIntermediatePoints(this.points, this.stepSizeMeters)) {
                positions.add(new PositionDTO(point.getLatitude(), point.getLongitude()));
            }
        }

        return new RequestPolarDiagramDataDTO(this.boatClassID, positions, this.averageWindSpeed);
    }
}
