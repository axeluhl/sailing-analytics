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
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.ReceivePolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.RequestPolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDPoint;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDSegment;
import com.sap.sailing.gwt.ui.shared.racemap.TwoDVector;

public class PathPolyline {
    private static final boolean SHOULD_DRAW_DISTANCED_DASHLINES = true;
    private static final boolean SHOULD_DRAW_BISECTOR_DASHLINES = true;
    private static final boolean SHOULD_DRAW_VERTICAL_DASHLINES = true;
    private static final boolean SHOULD_DRAW_SHADOW_DASHLINES = false;

    private static final String DEFAULT_COLOR = "#8B0000";
    private static final int DEFAULT_WEIGHT = 3;
    private static final double DEFAULT_OPACITY = 1.0;
    private static final double DEFAULT_DISTANCE_PX = 25;

    private static final int DEFAULT_DASHLINE_WEIGHT = 1;
    private static final String DEFAULT_DASHLINE_DISTANCED_COLOR = "Red";
    private static final String DEFAULT_DASHLINE_BISECTOR_COLOR = "Green";
    private static final String DEFAULT_DASHLINE_VERTICAL_COLOR = "Blue";

    private Polyline polyline;
    private final LatLng[] points;

    private Polyline shadowPolyline;
    private final List<LatLng> shadowPoints;
    private final List<Polyline> dashedLines;

    private final String color;
    private final int weight;
    private final double opacity;
    private final Map<TwoDPoint, List<TwoDPoint>> originAndHeads;
    private final SpeedWithBearingDTO averageWindSpeed;

    private final int boatClassID;
    private final PathDTO pathDTO;
    private final SimulatorServiceAsync simulatorService;
    private final MapWidget map;
    private final ErrorReporter errorReporter;
    private boolean warningAlreadyShown = false;

    public PathPolyline(final LatLng[] points, final int boatClassID, final ErrorReporter errorReporter, final PathDTO pathDTO,
            final SimulatorServiceAsync simulatorService, final MapWidget map) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, boatClassID, errorReporter, pathDTO, simulatorService, map);
    }

    public PathPolyline(final LatLng[] points, final String color, final int weight, final double opacity, final int boatClassID,
            final ErrorReporter errorReporter, final PathDTO pathDTO, final SimulatorServiceAsync simulatorService, final MapWidget map) {
        this.points = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;

        this.dashedLines = new ArrayList<Polyline>();
        this.originAndHeads = new HashMap<TwoDPoint, List<TwoDPoint>>();
        this.shadowPoints = new ArrayList<LatLng>();

        this.pathDTO = pathDTO;
        this.simulatorService = simulatorService;
        this.map = map;
        this.averageWindSpeed = this.getAverageWindSpeed();
        this.boatClassID = boatClassID;
        this.errorReporter = errorReporter;

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

        // this.computeTotalTimeOfPathPolyline();
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

            if (SHOULD_DRAW_DISTANCED_DASHLINES) {
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

            if (SHOULD_DRAW_DISTANCED_DASHLINES) {
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

            if (SHOULD_DRAW_BISECTOR_DASHLINES) {
                this.dashedLines.add(this.createPolyline(origin, bisectorHead, DEFAULT_DASHLINE_BISECTOR_COLOR, DEFAULT_DASHLINE_WEIGHT));
            }

            if (SHOULD_DRAW_VERTICAL_DASHLINES) {
                this.dashedLines.add(this.createPolyline(origin, rotated90Head, DEFAULT_DASHLINE_VERTICAL_COLOR, DEFAULT_DASHLINE_WEIGHT));
                this.dashedLines.add(this.createPolyline(origin, rotated270Head, DEFAULT_DASHLINE_VERTICAL_COLOR, DEFAULT_DASHLINE_WEIGHT));
            }
        }

        for (final Polyline line : this.dashedLines) {
            this.map.addOverlay(line);
        }

        if (SHOULD_DRAW_SHADOW_DASHLINES) {
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

    private Polyline createPolyline(final TwoDPoint first, final TwoDPoint second, final String color, final int weight) {
        final LatLng[] points = new LatLng[2];

        points[0] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) first.getX(), (int) first.getY()));
        points[1] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) second.getX(), (int) second.getY()));

        return new Polyline(points, color, weight);
    }

    private static boolean equalsLatLng(final LatLng first, final LatLng second) {
        return (first.getLatitude() == second.getLatitude() && first.getLongitude() == second.getLongitude());
    }

    @SuppressWarnings("unused")
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

                final List<PositionDTO> positions = requestData.getPositions();
                final List<SpeedWithBearingDTO> speeds = receiveData.getSpeeds();

                final int noOfPositions = positions.size();
                final int noOfSpeeds = speeds.size();

                if (noOfPositions != 1 + noOfSpeeds) {
                    // ERROR
                }

                PositionDTO currentStartPositionDTO = null;
                PositionDTO currentEndPositionDTO = null;
                SpeedWithBearingDTO currentSpeedWithBearingDTO = null;
                double totalTime = 0.0;

                for (int index = 0; index < noOfPositions; index++) {

                    if (index == noOfPositions - 1) {
                        break;
                    }

                    currentStartPositionDTO = positions.get(index);
                    currentEndPositionDTO = positions.get(index + 1);
                    currentSpeedWithBearingDTO = speeds.get(index);

                    totalTime += getTimeRequiredToSail(currentStartPositionDTO, currentEndPositionDTO, currentSpeedWithBearingDTO);
                }

                System.out.println("XXXYYYZZZ: Total time of the path polyline is " + totalTime + " seconds!");
            }
        });
    }

    private static double convertFromDegToRad(final double noOfDegrees) {
        return (noOfDegrees / 180) * Math.PI;
    }

    private SpeedWithBearingDTO computeAverageWindSpeed_TrueWind() {

        final List<WindDTO> windDTOs = this.pathDTO.getMatrix();
        final int count = windDTOs.size();

        double sumOfProductOfSpeedAndCosBearing = 0;
        double sumOfProductOfSpeedAndSinBearing = 0;

        for (final WindDTO windDTO : windDTOs) {
            sumOfProductOfSpeedAndCosBearing += (windDTO.trueWindSpeedInKnots * Math.cos(convertFromDegToRad(windDTO.trueWindBearingDeg)));
            sumOfProductOfSpeedAndSinBearing += (windDTO.trueWindSpeedInKnots * Math.sin(convertFromDegToRad(windDTO.trueWindBearingDeg)));
        }

        final double a = sumOfProductOfSpeedAndCosBearing / count;
        final double b = sumOfProductOfSpeedAndSinBearing / count;
        final double c = Math.atan(b / a);

        double averageBearing = 0;

        if (a == 0) {
            averageBearing = (b >= 0) ? 90 : 270;
        } else if (a < 0) { // 2nd Q : 3rd Q
            averageBearing = 180 + c;
        } else if (a > 0) {
            averageBearing = (b >= 0) ? c : 360 + c; // 1st Q : 4th Q
        }

        final double averageSpeed = Math.sqrt(a * a + b * b);

        return new SpeedWithBearingDTO(averageSpeed, averageBearing);
    }

    private SpeedWithBearingDTO computeAverageWindSpeed_DampenedTrueWind() {

        final List<WindDTO> windDTOs = this.pathDTO.getMatrix();
        final int count = windDTOs.size();

        double sumOfProductOfSpeedAndCosBearing = 0;
        double sumOfProductOfSpeedAndSinBearing = 0;

        for (final WindDTO windDTO : windDTOs) {

            if (windDTO.dampenedTrueWindSpeedInKnots == null) {
                System.err.println("XXX: windDTO.dampenedTrueWindSpeedInKnots is null");
            }

            sumOfProductOfSpeedAndCosBearing += (windDTO.dampenedTrueWindSpeedInKnots * Math.cos(convertFromDegToRad(windDTO.dampenedTrueWindBearingDeg)));
            sumOfProductOfSpeedAndSinBearing += (windDTO.dampenedTrueWindSpeedInKnots * Math.sin(convertFromDegToRad(windDTO.dampenedTrueWindBearingDeg)));
        }

        final double a = sumOfProductOfSpeedAndCosBearing / count;
        final double b = sumOfProductOfSpeedAndSinBearing / count;
        final double c = Math.atan(b / a);

        double averageBearing = 0;

        if (a == 0) {
            averageBearing = (b >= 0) ? 90 : 270;
        } else if (a < 0) { // 2nd Q : 3rd Q
            averageBearing = 180 + c;
        } else if (a > 0) {
            averageBearing = (b >= 0) ? c : 360 + c; // 1st Q : 4th Q
        }

        final double averageSpeed = Math.sqrt(a * a + b * b);

        return new SpeedWithBearingDTO(averageSpeed, averageBearing);
    }

    private SpeedWithBearingDTO computeAverageWindSpeed_MinMax_DampenedTrueWind() {

        final List<WindDTO> windDTOs = this.pathDTO.getMatrix();

        WindDTO minWindDTO = windDTOs.get(0);
        WindDTO maxWindDTO = windDTOs.get(0);
        WindDTO currentWindDTO = null;
        final int count = windDTOs.size();

        for (int index = 1; index < count; index++) {

            currentWindDTO = windDTOs.get(index);

            if (currentWindDTO.dampenedTrueWindSpeedInKnots < minWindDTO.dampenedTrueWindSpeedInKnots) {
                minWindDTO = currentWindDTO;
            }

            if (currentWindDTO.dampenedTrueWindSpeedInKnots > maxWindDTO.dampenedTrueWindSpeedInKnots) {
                maxWindDTO = currentWindDTO;
            }
        }

        final double speedInKnots = (minWindDTO.dampenedTrueWindSpeedInKnots + maxWindDTO.dampenedTrueWindSpeedInKnots) / 2;
        final double bearingInDegrees = (minWindDTO.dampenedTrueWindBearingDeg + maxWindDTO.dampenedTrueWindBearingDeg) / 2;

        return new SpeedWithBearingDTO(speedInKnots, bearingInDegrees);
    }

    private SpeedWithBearingDTO computeAverageWindSpeed_MinMax_TrueWind() {

        final List<WindDTO> windDTOs = this.pathDTO.getMatrix();

        WindDTO minWindDTO = windDTOs.get(0);
        WindDTO maxWindDTO = windDTOs.get(0);
        WindDTO currentWindDTO = null;
        final int count = windDTOs.size();

        for (int index = 1; index < count; index++) {

            currentWindDTO = windDTOs.get(index);

            if (currentWindDTO.trueWindSpeedInKnots < minWindDTO.trueWindSpeedInKnots) {
                minWindDTO = currentWindDTO;
            }

            if (currentWindDTO.trueWindSpeedInKnots > maxWindDTO.trueWindSpeedInKnots) {
                maxWindDTO = currentWindDTO;
            }
        }

        final double speedInKnots = (minWindDTO.trueWindSpeedInKnots + maxWindDTO.trueWindSpeedInKnots) / 2;
        final double bearingInDegrees = (minWindDTO.trueWindBearingDeg + maxWindDTO.trueWindBearingDeg) / 2;

        return new SpeedWithBearingDTO(speedInKnots, bearingInDegrees);
    }

    private SpeedWithBearingDTO getAverageWindSpeed() {

        @SuppressWarnings("unused")
        final SpeedWithBearingDTO averageWindSpeed_TrueWind = this.computeAverageWindSpeed_TrueWind();

        final SpeedWithBearingDTO averageWindSpeed_DampenedTrueWind = this.computeAverageWindSpeed_DampenedTrueWind();

        @SuppressWarnings("unused")
        final SpeedWithBearingDTO averageWindSpeed_MinMax = this.computeAverageWindSpeed_MinMax_DampenedTrueWind();

        @SuppressWarnings("unused")
        final SpeedWithBearingDTO averageWindSpeed_MinMax_TrueWind = this.computeAverageWindSpeed_MinMax_TrueWind();

        return averageWindSpeed_DampenedTrueWind;
    }

    private RequestPolarDiagramDataDTO createRequestPolarDiagramDataDTO() {

        final List<PositionDTO> positions = new ArrayList<PositionDTO>();

        for (final LatLng point : this.points) {
            positions.add(new PositionDTO(point.getLatitude(), point.getLongitude()));
        }

        return new RequestPolarDiagramDataDTO(this.boatClassID, positions, this.averageWindSpeed);
    }

    private static double getTimeRequiredToSail(final PositionDTO positionDTOStart, final PositionDTO positionDTOEnd, final SpeedWithBearingDTO speed) {

        final double speedMetersPerSecond = speed.speedInKnots * 0.51444;

        if (speedMetersPerSecond == 0.0) {
            return Double.MAX_VALUE;
        }

        final DegreePosition degreePositionStart = new DegreePosition(positionDTOStart.latDeg, positionDTOStart.lngDeg);
        final DegreePosition degreePositionEnd = new DegreePosition(positionDTOEnd.latDeg, positionDTOEnd.lngDeg);

        final Distance distance = degreePositionStart.getDistance(degreePositionEnd);

        return (distance.getMeters() / speedMetersPerSecond);
    }
}
