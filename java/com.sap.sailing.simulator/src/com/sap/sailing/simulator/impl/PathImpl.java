package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;

public class PathImpl implements Path {

    List<TimedPositionWithSpeed> pathPoints;
    WindField windField;

    public PathImpl(List<TimedPositionWithSpeed> pointsList, WindField wf) {

        pathPoints = pointsList;
        windField = wf;

    }

    @Override
    public List<TimedPositionWithSpeed> getPathPoints() {
        return pathPoints;
    }

    @Override
    public void setPathPoints(List<TimedPositionWithSpeed> pointsList) {
        pathPoints = pointsList;
    }

    @Override
    public List<TimedPositionWithSpeed> getEvenTimedPath(long timeStep) {

        TimePoint startTime = pathPoints.get(0).getTimePoint();
        TimePoint endTime = pathPoints.get(pathPoints.size() - 1).getTimePoint();

        List<TimedPositionWithSpeed> path = new ArrayList<TimedPositionWithSpeed>();

        path.add(pathPoints.get(0));
        TimePoint nextTimePoint = new MillisecondsTimePoint(startTime.asMillis() + timeStep);
        List<TimedPositionWithSpeed> points = new ArrayList<TimedPositionWithSpeed>();

        int idx = 1;
        while (idx < pathPoints.size()) {

            if (pathPoints.get(idx).getTimePoint().asMillis() >= nextTimePoint.asMillis()) {

                // reached point after next timestep
                TimedPositionWithSpeed p1 = pathPoints.get(idx - 1);
                TimedPositionWithSpeed p2 = pathPoints.get(idx);
                Distance dist = p1.getPosition().getDistance(p2.getPosition());
                // long nextTime = (double)nextTimePoint.asMillis();
                // System.out.println(""+(nextTimePoint.asMillis() -
                // p1.getTimePoint().asMillis())+" - "+(p2.getTimePoint().asMillis() - p1.getTimePoint().asMillis()));
                double scale1 = (double) (nextTimePoint.asMillis() - p1.getTimePoint().asMillis());
                double scale2 = (double) (p2.getTimePoint().asMillis() - p1.getTimePoint().asMillis());
                Position nextPosition = p1.getPosition().translateGreatCircle(p1.getPosition().getBearingGreatCircle(p2.getPosition()),
                        dist.scale(scale1 / scale2));
                SpeedWithBearing nextWind = windField.getWind(new TimedPositionImpl(nextTimePoint, nextPosition));
                TimedPositionWithSpeed nextPoint = new TimedPositionWithSpeedImpl(nextTimePoint, nextPosition, nextWind);

                // distance scale: percentage of distance between previous point and next point
                TimedPositionWithSpeed prevPoint = path.get(path.size() - 1);
                double scaleDist = 0.01 * nextPoint.getPosition().getDistance(prevPoint.getPosition()).getMeters();

                // evaluate collected points to potentially find turn/corner
                double maxDist = 0;
                TimedPositionWithSpeed maxPoint = null;
                Bearing nextBear = prevPoint.getPosition().getBearingGreatCircle(nextPoint.getPosition());
                for (int jdx = 0; jdx < points.size(); jdx++) {

                    Position pcur = points.get(jdx).getPosition();
                    Position ptmp = pcur.projectToLineThrough(prevPoint.getPosition(), nextBear);
                    double lineDist = ptmp.getDistance(pcur).getMeters();
                    if (lineDist > maxDist) {
                        maxPoint = points.get(jdx);
                        maxDist = lineDist;
                    }

                }

                if (maxDist > scaleDist) {
                    // add intermediate corner point
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
            points.add(pathPoints.get(idx));

            if (pathPoints.get(idx).getTimePoint().asMillis() < nextTimePoint.asMillis()) {
                idx++;
            }

        }

        if (path.get(path.size() - 1).getTimePoint().asMillis() < endTime.asMillis()) {
            path.add(pathPoints.get(pathPoints.size() - 1));
        }

        return path;
    }

    @Override
    public void setWindField(WindField wf) {

        windField = wf;

    }

    //@Override
    public TimedPositionWithSpeed getPositionAtTime(TimePoint t) {

        if (t.compareTo(pathPoints.get(0).getTimePoint()) == 0)
            return pathPoints.get(0);
        if (t.compareTo(pathPoints.get(pathPoints.size() - 1).getTimePoint()) >= 0)
            return pathPoints.get(pathPoints.size() - 1);

        TimedPositionWithSpeed p1 = null;
        TimedPositionWithSpeed p2 = null;
        for (TimedPositionWithSpeed p : pathPoints) {
            if (p.getTimePoint().compareTo(t) >= 0) {
                p2 = p;
                p1 = pathPoints.get(pathPoints.indexOf(p) - 1);
                break;
            }
        }

        double t1 = 1000.0 * p1.getTimePoint().asMillis();
        double t2 = 1000.0 * p2.getTimePoint().asMillis();
        double t0 = 1000.0 * t.asMillis();

        Distance dist = p1.getPosition().getDistance(p2.getPosition());
        Position p0 = p1.getPosition().translateGreatCircle(p1.getPosition().getBearingGreatCircle(p2.getPosition()),
                dist.scale((t0 - t1) / (t2 - t1)));
        SpeedWithBearing windAtPoint = windField.getWind(new TimedPositionImpl(t, p0));

        return new TimedPositionWithSpeedImpl(t, p0, windAtPoint);
    }

    //@Override
    public List<TimedPositionWithSpeed> getEvenTimedPoints(long milliseconds) {

        if (milliseconds == 0)
            return null;

        List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
        TimePoint t = pathPoints.get(0).getTimePoint();
        TimePoint lastPoint = pathPoints.get(pathPoints.size() - 1).getTimePoint();

        while ((t.compareTo(lastPoint) <= 0) && (lst.size() < 200)) { // paths with more than 200 points lead to
                                                                      // performance issues
            lst.add(getPositionAtTime(t));
            t = new MillisecondsTimePoint(t.asMillis() + milliseconds);
        }
        if (t.compareTo(lastPoint) > 0) { // without this, the path may not reach end
            lst.add(getPositionAtTime(t));
        }
        // if (!lst.contains(pathPoints.get(pathPoints.size()-1)))
        // lst.add(pathPoints.get(pathPoints.size()-1));

        return lst;
    }

    // not implemented yet!
    //@Override
    public List<TimedPositionWithSpeed> getEvenDistancedPoints(Distance dist) {
        return null;
    }

}
