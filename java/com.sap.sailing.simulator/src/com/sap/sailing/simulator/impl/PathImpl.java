package com.sap.sailing.simulator.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindField;

public class PathImpl implements Path, Serializable {

    private static final long serialVersionUID = -6354445155884413937L;
    List<TimedPositionWithSpeed> pathPoints;
    WindField windField;

    public PathImpl(final List<TimedPositionWithSpeed> pointsList, final WindField wf) {

        this.pathPoints = pointsList;
        this.windField = wf;

    }

    @Override
    public List<TimedPositionWithSpeed> getPathPoints() {
        return this.pathPoints;
    }

    @Override
    public void setPathPoints(final List<TimedPositionWithSpeed> pointsList) {
        this.pathPoints = pointsList;
    }

    @Override
    public Path getEvenTimedPath(final long timestep) {
        return new PathImpl(this.getEvenTimedPathAsList(timestep), this.windField);
    }

    private List<TimedPositionWithSpeed> getEvenTimedPathAsList(final long timeStep) {

        final TimePoint startTime = this.pathPoints.get(0).getTimePoint();
        final TimePoint endTime = this.pathPoints.get(this.pathPoints.size() - 1).getTimePoint();

        final List<TimedPositionWithSpeed> path = new ArrayList<TimedPositionWithSpeed>();

        TimedPositionWithSpeed startPoint = this.pathPoints.get(0);
        SpeedWithBearing startWind = null;
        if (this.windField != null) {
            startWind = this.windField.getWind(new TimedPositionImpl(startPoint.getTimePoint(), startPoint
                    .getPosition()));
            startPoint = new TimedPositionWithSpeedImpl(startPoint.getTimePoint(), startPoint.getPosition(), startWind);
        }
        path.add(startPoint);
        TimePoint nextTimePoint = new MillisecondsTimePoint(startTime.asMillis() + timeStep);
        List<TimedPositionWithSpeed> points = new ArrayList<TimedPositionWithSpeed>();

        int idx = 1;
        while (idx < this.pathPoints.size()) {

            if (this.pathPoints.get(idx).getTimePoint().asMillis() >= nextTimePoint.asMillis()) {

                // reached point after next timestep
                final TimedPositionWithSpeed p1 = this.pathPoints.get(idx - 1);
                final TimedPositionWithSpeed p2 = this.pathPoints.get(idx);
                final Distance dist = p1.getPosition().getDistance(p2.getPosition());
                // long nextTime = (double)nextTimePoint.asMillis();
                // System.out.println(""+(nextTimePoint.asMillis() -
                // p1.getTimePoint().asMillis())+" - "+(p2.getTimePoint().asMillis() - p1.getTimePoint().asMillis()));
                final double scale1 = nextTimePoint.asMillis() - p1.getTimePoint().asMillis();
                final double scale2 = p2.getTimePoint().asMillis() - p1.getTimePoint().asMillis();
                final Position nextPosition = p1.getPosition().translateGreatCircle(
                        p1.getPosition().getBearingGreatCircle(p2.getPosition()), dist.scale(scale1 / scale2));
                SpeedWithBearing nextWind = null;
                if (this.windField != null) {
                    nextWind = this.windField.getWind(new TimedPositionImpl(nextTimePoint, nextPosition));
                } else {
                    final double nextWindSpeed = p1.getSpeed().getKnots()
                            + (p2.getSpeed().getKnots() - p1.getSpeed().getKnots()) * scale1 / scale2;
                    final double nextWindAngle = p1.getSpeed().getBearing().getDegrees()
                            + (p2.getSpeed().getBearing().getDegrees() - p1.getSpeed().getBearing().getDegrees())
                            * scale1 / scale2;
                    final SpeedWithBearing nextWindSpeedWithBearing = new KnotSpeedWithBearingImpl(nextWindSpeed,
                            new DegreeBearingImpl(nextWindAngle));
                    nextWind = new WindImpl(nextPosition, nextTimePoint, nextWindSpeedWithBearing);
                }
                final TimedPositionWithSpeed nextPoint = new TimedPositionWithSpeedImpl(nextTimePoint, nextPosition,
                        nextWind);

                // distance scale: percentage of distance between previous point and next point
                final TimedPositionWithSpeed prevPoint = path.get(path.size() - 1);
                final double scaleDist = 0.01 * nextPoint.getPosition().getDistance(prevPoint.getPosition())
                        .getMeters();

                // evaluate collected points to potentially find turn/corner
                double maxDist = 0;
                TimedPositionWithSpeed maxPoint = null;
                final Bearing nextBear = prevPoint.getPosition().getBearingGreatCircle(nextPoint.getPosition());
                for (int jdx = 0; jdx < points.size(); jdx++) {

                    final Position pcur = points.get(jdx).getPosition();
                    final Position ptmp = pcur.projectToLineThrough(prevPoint.getPosition(), nextBear);
                    final double lineDist = ptmp.getDistance(pcur).getMeters();
                    if (lineDist > maxDist) {
                        maxPoint = points.get(jdx);
                        maxDist = lineDist;
                    }

                }

                if (maxDist > scaleDist) {
                    // add intermediate corner point
                    SpeedWithBearing maxWind = null;
                    if (this.windField != null) {
                        maxWind = this.windField.getWind(new TimedPositionImpl(maxPoint.getTimePoint(), maxPoint
                                .getPosition()));
                        maxPoint = new TimedPositionWithSpeedImpl(maxPoint.getTimePoint(), maxPoint.getPosition(),
                                maxWind);
                    }
                    path.add(maxPoint);
                }

                // add next even timed point
                path.add(nextPoint);

                // clear list of intermediate points
                points = new ArrayList<TimedPositionWithSpeed>();

                // increase nextTimePoint by timeStep
                nextTimePoint = nextTimePoint.plus(timeStep);
            }

            // collect points between previous timestep and next timestep
            points.add(this.pathPoints.get(idx));

            if (this.pathPoints.get(idx).getTimePoint().asMillis() < nextTimePoint.asMillis()) {
                idx++;
            }

        }

        if (path.get(path.size() - 1).getTimePoint().asMillis() < endTime.asMillis()) {
            TimedPositionWithSpeed endPoint = this.pathPoints.get(this.pathPoints.size() - 1);
            SpeedWithBearing endWind = null;
            if (this.windField != null) {
                endWind = this.windField
                        .getWind(new TimedPositionImpl(endPoint.getTimePoint(), endPoint.getPosition()));
                endPoint = new TimedPositionWithSpeedImpl(endPoint.getTimePoint(), endPoint.getPosition(), endWind);
            }
            path.add(endPoint);
        }

        return path;
    }

    @Override
    public void setWindField(final WindField wf) {

        this.windField = wf;

    }

    // @Override
    public TimedPositionWithSpeed getPositionAtTime(final TimePoint t) {

        if (t.compareTo(this.pathPoints.get(0).getTimePoint()) == 0) {
            return this.pathPoints.get(0);
        }
        if (t.compareTo(this.pathPoints.get(this.pathPoints.size() - 1).getTimePoint()) >= 0) {
            return this.pathPoints.get(this.pathPoints.size() - 1);
        }

        TimedPositionWithSpeed p1 = null;
        TimedPositionWithSpeed p2 = null;
        for (final TimedPositionWithSpeed p : this.pathPoints) {
            if (p.getTimePoint().compareTo(t) >= 0) {
                p2 = p;
                p1 = this.pathPoints.get(this.pathPoints.indexOf(p) - 1);
                break;
            }
        }

        final double t1 = 1000.0 * p1.getTimePoint().asMillis();
        final double t2 = 1000.0 * p2.getTimePoint().asMillis();
        final double t0 = 1000.0 * t.asMillis();

        final Distance dist = p1.getPosition().getDistance(p2.getPosition());
        final Position p0 = p1.getPosition().translateGreatCircle(
                p1.getPosition().getBearingGreatCircle(p2.getPosition()), dist.scale((t0 - t1) / (t2 - t1)));
        final SpeedWithBearing windAtPoint = this.windField.getWind(new TimedPositionImpl(t, p0));

        return new TimedPositionWithSpeedImpl(t, p0, windAtPoint);
    }

    // @Override
    public List<TimedPositionWithSpeed> getEvenTimedPoints(final long milliseconds) {

        if (milliseconds == 0) {
            return null;
        }

        final List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
        TimePoint t = this.pathPoints.get(0).getTimePoint();
        final TimePoint lastPoint = this.pathPoints.get(this.pathPoints.size() - 1).getTimePoint();

        while ((t.compareTo(lastPoint) <= 0) && (lst.size() < 200)) { // paths with more than 200 points lead to
            // performance issues
            lst.add(this.getPositionAtTime(t));
            t = new MillisecondsTimePoint(t.asMillis() + milliseconds);
        }
        if (t.compareTo(lastPoint) > 0) { // without this, the path may not reach end
            lst.add(this.getPositionAtTime(t));
        }
        // if (!lst.contains(pathPoints.get(pathPoints.size()-1)))
        // lst.add(pathPoints.get(pathPoints.size()-1));

        return lst;
    }

    // not implemented yet!
    // @Override
    public List<TimedPositionWithSpeed> getEvenDistancedPoints(final Distance dist) {
        return null;
    }

    @Override
    public List<TimedPositionWithSpeed> getTurns() {
        if (this.pathPoints == null) {
            return null;
        }

        final List<TimedPositionWithSpeed> list = new ArrayList<TimedPositionWithSpeed>();

        if (this.pathPoints.isEmpty()) {
            return list;
        }

        final int noOfPoints = this.pathPoints.size();
        TimedPositionWithSpeed previousPoint = null;
        TimedPositionWithSpeed currentPoint = null;
        TimedPositionWithSpeed nextPoint = null;

        for (int index = 0; index < noOfPoints; index++) {

            currentPoint = this.pathPoints.get(index);

            if (index == 0 || index == (noOfPoints - 1)) {
                list.add(currentPoint);
            } else {

                previousPoint = this.pathPoints.get(index - 1);
                nextPoint = this.pathPoints.get(index + 1);

                if (currentPoint.getPosition().isTurn(previousPoint.getPosition(), nextPoint.getPosition())) {
                    list.add(currentPoint);
                }
            }
        }

        return list;
    }

    public static boolean saveToGpxFile(final Path path, final String fileName) {

        if (path == null) {
            return false;
        }

        final List<TimedPositionWithSpeed> pathPoints = path.getPathPoints();
        if (pathPoints == null || pathPoints.isEmpty()) {
            return false;
        }

        TimedPositionWithSpeed timedPoint = null;
        Position point = null;
        final int noOfPoints = pathPoints.size();
        final Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final StringBuffer buffer = new StringBuffer();

        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\r\n\t<trk>\r\n\t\t<name>GPSPolyPath</name>\r\n\t\t<trkseg>");

        for (int index = 0; index < noOfPoints; index++) {
            timedPoint = pathPoints.get(index);
            point = timedPoint.getPosition();
            buffer.append("\r\n\t\t\t<trkpt lat=\"" + point.getLatDeg() + "\" lon=\"" + point.getLngDeg()
                    + "\">\r\n\t\t\t\t<ele>0</ele>\r\n\t\t\t\t<time>"
                    + formatter.format(timedPoint.getTimePoint().asDate()) + "</time>\r\n\t\t\t</trkpt>");
        }

        buffer.append("\r\n\t\t</trkseg>\r\n\t</trk>\r\n</gpx>\r\n");

        final String content = buffer.toString();

        try {

            final FileWriter writer = new FileWriter(fileName);
            final BufferedWriter output = new BufferedWriter(writer, 32768);

            try {
                output.write(content);
            } finally {
                output.close();
                writer.close();
            }

        } catch (final IOException e) {
            return false;
        }

        return true;
    }
}
