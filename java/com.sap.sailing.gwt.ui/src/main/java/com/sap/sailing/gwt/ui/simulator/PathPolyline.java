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
    private LatLng[] points;

    private Polyline shadowPolyline;
    private List<LatLng> shadowPoints;
    private List<Polyline> dashedLines;
    private MapWidget map;
    private String color;
    private int weight;
    private double opacity;
    private Map<TwoDPoint, List<TwoDPoint>> originAndHeads;

    public PathPolyline(LatLng[] points, MapWidget map) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, map);
    }

    public PathPolyline(LatLng[] points, String color, int weight, double opacity, MapWidget map) {
        this.points = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;
        this.map = map;
        this.dashedLines = new ArrayList<Polyline>();
        this.originAndHeads = new HashMap<TwoDPoint, List<TwoDPoint>>();
        this.shadowPoints = new ArrayList<LatLng>();

        this.drawPolylineOnMap();
        this.drawDashLinesOnMap(this.map.getZoomLevel());

        this.map.addMapZoomEndHandler(new MapZoomEndHandler() {
            public void onZoomEnd(MapZoomEndEvent event) {
                drawDashLinesOnMap(event.getNewZoomLevel());
            }
        });
    }

    private void drawPolylineOnMap() {
        if (this.polyline != null)
            this.map.removeOverlay(this.polyline);

        this.polyline = new Polyline(this.points, this.color, this.weight, this.opacity);

        this.polyline.addPolylineLineUpdatedHandler(new PolylineLineUpdatedHandler() {
            @Override
            public void onUpdate(PolylineLineUpdatedEvent event) {
                int indexOfMovedPoint = getIndexOfMovedPoint();
                int noOfPoints = points.length;

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

                    double distance = (map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

                    TwoDPoint origin = convertToTwoDPoint(points[indexOfMovedPoint]);
                    TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance,
                            convertToTwoDPoint(points[indexOfMovedPoint - 1]));
                    TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance,
                            convertToTwoDPoint(points[indexOfMovedPoint + 1]));
                    TwoDPoint newOrigin = TwoDPoint.getCorrectProjection(origin, head1, head2,
                            convertToTwoDPoint(polyline.getVertex(indexOfMovedPoint)));

                    shadowPoints.clear();
                    for (LatLng point : points)
                        shadowPoints.add(point);

                    if (indexOfMovedPoint == 1) {
                        TwoDSegment afterLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint + 1]),
                                convertToTwoDPoint(points[indexOfMovedPoint + 2]));
                        TwoDVector afterVector = new TwoDVector(origin,
                                convertToTwoDPoint(points[indexOfMovedPoint + 1]));
                        TwoDPoint afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        points[indexOfMovedPoint] = convertToLatLng(newOrigin);
                        points[indexOfMovedPoint + 1] = convertToLatLng(afterNewOrigin);
                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        TwoDSegment beforeLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint - 2]),
                                convertToTwoDPoint(points[indexOfMovedPoint - 1]));
                        TwoDVector beforeVector = new TwoDVector(convertToTwoDPoint(points[indexOfMovedPoint - 1]),
                                origin);
                        TwoDPoint beforeNewOrigin = TwoDPoint
                                .projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        points[indexOfMovedPoint - 1] = convertToLatLng(beforeNewOrigin);
                        points[indexOfMovedPoint] = convertToLatLng(newOrigin);
                    } else {
                        TwoDSegment beforeLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint - 2]),
                                convertToTwoDPoint(points[indexOfMovedPoint - 1]));
                        TwoDVector beforeVector = new TwoDVector(convertToTwoDPoint(points[indexOfMovedPoint - 1]),
                                origin);
                        TwoDPoint beforeNewOrigin = TwoDPoint
                                .projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        TwoDSegment afterLine = new TwoDSegment(convertToTwoDPoint(points[indexOfMovedPoint + 1]),
                                convertToTwoDPoint(points[indexOfMovedPoint + 2]));
                        TwoDVector afterVector = new TwoDVector(origin,
                                convertToTwoDPoint(points[indexOfMovedPoint + 1]));
                        TwoDPoint afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

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
    }

    private void drawDashLinesOnMap(int zoomLevel) {
        int noOfPathPoints = this.points.length;
        if (noOfPathPoints == 0 || noOfPathPoints == 1 || noOfPathPoints == 2)
            return;

        for (Polyline line : this.dashedLines)
            this.map.removeOverlay(line);
        this.dashedLines.clear();

        TwoDPoint distancedFromPoint = null;
        TwoDPoint startPoint = null;
        TwoDPoint distancedPoint = null;
        Point temp = null;

        this.originAndHeads.clear();

        double distance = (zoomLevel - 11) * DEFAULT_DISTANCE_PX;

        for (int index = 0; index < noOfPathPoints - 2; index++) {
            temp = this.map.convertLatLngToContainerPixel(this.points[index + 1]);
            distancedFromPoint = new TwoDPoint(temp.getX(), temp.getY());

            temp = this.map.convertLatLngToContainerPixel(this.points[index]);
            startPoint = new TwoDPoint(temp.getX(), temp.getY());

            distancedPoint = TwoDPoint.getDistancedPoint(distancedFromPoint, distance, startPoint);

            if (this.originAndHeads.containsKey(distancedFromPoint) == false)
                this.originAndHeads.put(distancedFromPoint, new ArrayList<TwoDPoint>());
            this.originAndHeads.get(distancedFromPoint).add(distancedPoint);

            if (SHOULD_DRAW_DISTANCED_DASHLINES)
                this.dashedLines.add(this.createPolyline(distancedFromPoint, distancedPoint,
                        DEFAULT_DASHLINE_DISTANCED_COLOR, DEFAULT_DASHLINE_WEIGHT));
        }

        for (int index = noOfPathPoints - 1; index > 1; index--) {
            temp = this.map.convertLatLngToContainerPixel(this.points[index - 1]);
            distancedFromPoint = new TwoDPoint(temp.getX(), temp.getY());

            temp = this.map.convertLatLngToContainerPixel(this.points[index]);
            startPoint = new TwoDPoint(temp.getX(), temp.getY());

            distancedPoint = TwoDPoint.getDistancedPoint(distancedFromPoint, distance, startPoint);

            if (this.originAndHeads.containsKey(distancedFromPoint) == false)
                this.originAndHeads.put(distancedFromPoint, new ArrayList<TwoDPoint>());
            this.originAndHeads.get(distancedFromPoint).add(distancedPoint);

            if (SHOULD_DRAW_DISTANCED_DASHLINES)
                this.dashedLines.add(this.createPolyline(distancedFromPoint, distancedPoint,
                        DEFAULT_DASHLINE_DISTANCED_COLOR, DEFAULT_DASHLINE_WEIGHT));
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

            if (SHOULD_DRAW_BISECTOR_DASHLINES)
                this.dashedLines.add(this.createPolyline(origin, bisectorHead, DEFAULT_DASHLINE_BISECTOR_COLOR,
                        DEFAULT_DASHLINE_WEIGHT));

            if (SHOULD_DRAW_VERTICAL_DASHLINES) {
                this.dashedLines.add(this.createPolyline(origin, rotated90Head, DEFAULT_DASHLINE_VERTICAL_COLOR,
                        DEFAULT_DASHLINE_WEIGHT));
                this.dashedLines.add(this.createPolyline(origin, rotated270Head, DEFAULT_DASHLINE_VERTICAL_COLOR,
                        DEFAULT_DASHLINE_WEIGHT));
            }
        }

        for (Polyline line : this.dashedLines)
            this.map.addOverlay(line);

        if (SHOULD_DRAW_SHADOW_DASHLINES) {
            if (this.shadowPolyline != null)
                map.removeOverlay(this.shadowPolyline);

            this.shadowPolyline = new Polyline(this.shadowPoints.toArray(new LatLng[0]), DEFAULT_COLOR,
                    DEFAULT_DASHLINE_WEIGHT, DEFAULT_OPACITY);
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

            if (equalsLatLng(oldPointPosition, newPointPosition) == false)
                break;
        }

        return index;
    }

    private TwoDPoint convertToTwoDPoint(LatLng latLng) {
        Point point = this.map.convertLatLngToContainerPixel(latLng);
        return new TwoDPoint(point.getX(), point.getY());
    }

    private LatLng convertToLatLng(TwoDPoint point) {
        return this.map.convertContainerPixelToLatLng(Point.newInstance((int) point.getX(), (int) point.getY()));
    }

    private Polyline createPolyline(TwoDPoint first, TwoDPoint second, String color, int weight) {
        LatLng[] points = new LatLng[2];

        points[0] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) first.getX(), (int) first.getY()));
        points[1] = this.map.convertContainerPixelToLatLng(Point.newInstance((int) second.getX(), (int) second.getY()));

        return new Polyline(points, color, weight);
    }

    private static boolean equalsLatLng(LatLng first, LatLng second) {
        return (first.getLatitude() == second.getLatitude() && first.getLongitude() == second.getLongitude());
    }
}
