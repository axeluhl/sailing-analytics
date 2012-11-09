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
    private final LatLng[] points;

    private Polyline shadowPolyline;
    private final List<LatLng> shadowPoints;
    private final List<Polyline> dashedLines;
    private final MapWidget map;
    private final String color;
    private final int weight;
    private final double opacity;
    private final Map<TwoDPoint, List<TwoDPoint>> originAndHeads;
    private final double averageWindSpeed;

    public PathPolyline(final LatLng[] points, final double averageWindSpeed, final MapWidget map) {
        this(points, DEFAULT_COLOR, DEFAULT_WEIGHT, DEFAULT_OPACITY, averageWindSpeed, map);
    }

    public PathPolyline(final LatLng[] points, final String color, final int weight, final double opacity, final double averageWindSpeed,
            final MapWidget map) {
        this.points = points;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;
        this.map = map;
        this.dashedLines = new ArrayList<Polyline>();
        this.originAndHeads = new HashMap<TwoDPoint, List<TwoDPoint>>();
        this.shadowPoints = new ArrayList<LatLng>();
        this.averageWindSpeed = averageWindSpeed;

        this.drawPolylineOnMap();
        this.drawDashLinesOnMap(this.map.getZoomLevel());

        this.map.addMapZoomEndHandler(new MapZoomEndHandler() {
            @Override
            public void onZoomEnd(final MapZoomEndEvent event) {
                PathPolyline.this.drawDashLinesOnMap(event.getNewZoomLevel());
            }
        });
    }

    public double getAverageWindSpeed() {
        return this.averageWindSpeed;
    }

    private void drawPolylineOnMap() {
        if (this.polyline != null) {
            this.map.removeOverlay(this.polyline);
        }

        this.polyline = new Polyline(this.points, this.color, this.weight, this.opacity);

        this.polyline.addPolylineLineUpdatedHandler(new PolylineLineUpdatedHandler() {
            @Override
            public void onUpdate(final PolylineLineUpdatedEvent event) {
                final int indexOfMovedPoint = PathPolyline.this.getIndexOfMovedPoint();
                final int noOfPoints = PathPolyline.this.points.length;

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

                    final double distance = (PathPolyline.this.map.getZoomLevel() - 11) * DEFAULT_DISTANCE_PX;

                    final TwoDPoint origin = PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint]);
                    final TwoDPoint head1 = TwoDPoint.getDistancedPoint(origin, distance,
                            PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 1]));
                    final TwoDPoint head2 = TwoDPoint.getDistancedPoint(origin, distance,
                            PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 1]));
                    final TwoDPoint newOrigin = TwoDPoint.getCorrectProjection(origin, head1, head2,
                            PathPolyline.this.convertToTwoDPoint(PathPolyline.this.polyline.getVertex(indexOfMovedPoint)));

                    PathPolyline.this.shadowPoints.clear();
                    for (final LatLng point : PathPolyline.this.points) {
                        PathPolyline.this.shadowPoints.add(point);
                    }

                    if (indexOfMovedPoint == 1) {
                        final TwoDSegment afterLine = new TwoDSegment(PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 1]),
                                PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 2]));
                        final TwoDVector afterVector = new TwoDVector(origin,
                                PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 1]));
                        final TwoDPoint afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        PathPolyline.this.points[indexOfMovedPoint] = PathPolyline.this.convertToLatLng(newOrigin);
                        PathPolyline.this.points[indexOfMovedPoint + 1] = PathPolyline.this.convertToLatLng(afterNewOrigin);
                    } else if (indexOfMovedPoint == noOfPoints - 2) {
                        final TwoDSegment beforeLine = new TwoDSegment(PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 2]),
                                PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 1]));
                        final TwoDVector beforeVector = new TwoDVector(PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 1]),
                                origin);
                        final TwoDPoint beforeNewOrigin = TwoDPoint
                                .projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        PathPolyline.this.points[indexOfMovedPoint - 1] = PathPolyline.this.convertToLatLng(beforeNewOrigin);
                        PathPolyline.this.points[indexOfMovedPoint] = PathPolyline.this.convertToLatLng(newOrigin);
                    } else {
                        final TwoDSegment beforeLine = new TwoDSegment(PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 2]),
                                PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 1]));
                        final TwoDVector beforeVector = new TwoDVector(PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint - 1]),
                                origin);
                        final TwoDPoint beforeNewOrigin = TwoDPoint
                                .projectToLineByVector(newOrigin, beforeLine, beforeVector);

                        final TwoDSegment afterLine = new TwoDSegment(PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 1]),
                                PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 2]));
                        final TwoDVector afterVector = new TwoDVector(origin,
                                PathPolyline.this.convertToTwoDPoint(PathPolyline.this.points[indexOfMovedPoint + 1]));
                        final TwoDPoint afterNewOrigin = TwoDPoint.projectToLineByVector(newOrigin, afterLine, afterVector);

                        PathPolyline.this.points[indexOfMovedPoint - 1] = PathPolyline.this.convertToLatLng(beforeNewOrigin);
                        PathPolyline.this.points[indexOfMovedPoint] = PathPolyline.this.convertToLatLng(newOrigin);
                        PathPolyline.this.points[indexOfMovedPoint + 1] = PathPolyline.this.convertToLatLng(afterNewOrigin);
                    }
                }

                PathPolyline.this.drawPolylineOnMap();
                PathPolyline.this.drawDashLinesOnMap(PathPolyline.this.map.getZoomLevel());
            }
        });

        this.map.addOverlay(this.polyline);
        this.polyline.setEditingEnabled(PolyEditingOptions.newInstance(this.points.length - 1));
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
                this.dashedLines.add(this.createPolyline(distancedFromPoint, distancedPoint,
                        DEFAULT_DASHLINE_DISTANCED_COLOR, DEFAULT_DASHLINE_WEIGHT));
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
                this.dashedLines.add(this.createPolyline(distancedFromPoint, distancedPoint,
                        DEFAULT_DASHLINE_DISTANCED_COLOR, DEFAULT_DASHLINE_WEIGHT));
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
                this.dashedLines.add(this.createPolyline(origin, bisectorHead, DEFAULT_DASHLINE_BISECTOR_COLOR,
                        DEFAULT_DASHLINE_WEIGHT));
            }

            if (SHOULD_DRAW_VERTICAL_DASHLINES) {
                this.dashedLines.add(this.createPolyline(origin, rotated90Head, DEFAULT_DASHLINE_VERTICAL_COLOR,
                        DEFAULT_DASHLINE_WEIGHT));
                this.dashedLines.add(this.createPolyline(origin, rotated270Head, DEFAULT_DASHLINE_VERTICAL_COLOR,
                        DEFAULT_DASHLINE_WEIGHT));
            }
        }

        for (final Polyline line : this.dashedLines) {
            this.map.addOverlay(line);
        }

        if (SHOULD_DRAW_SHADOW_DASHLINES) {
            if (this.shadowPolyline != null) {
                this.map.removeOverlay(this.shadowPolyline);
            }

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
}
