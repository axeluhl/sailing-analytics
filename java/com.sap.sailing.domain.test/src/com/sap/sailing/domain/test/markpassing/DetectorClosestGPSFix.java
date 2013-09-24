package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.test.markpassing.util.StraightLine;
import com.sap.sailing.domain.test.markpassing.util.Vector2D;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class DetectorClosestGPSFix implements DetectorMarkPassing {

    @Override
    public LinkedHashMap<Waypoint, MarkPassing> computeMarkpasses(
            DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markTracks, TimePoint raceStart) {

        LinkedHashMap<Waypoint, MarkPassing> computedMarkPasses = new LinkedHashMap<Waypoint, MarkPassing>();

        for (Waypoint w : markTracks.keySet()) {
            LinkedHashMap<Double, GPSFixMoving> possibleMarkPasses = new LinkedHashMap<Double, GPSFixMoving>();

            // If Line
            if (w.getControlPoint().hasTwoMarks()) {

                for (GPSFixMoving gpsFix : gpsFixes.getFixes()) {

                    StraightLine lineAtTimepoint = calculateStraightLine(gpsFix.getTimePoint(), markTracks.get(w));
                    double distance = lineAtTimepoint.getDistanceToPoint(new Vector2D(gpsFix.getPosition()));
                    if (distance < 300 && gpsFix.getTimePoint().after(raceStart)) {

                        possibleMarkPasses.put(distance, gpsFix);
                    }
                }
            }

            ////// If Single Mark
            if (!w.getControlPoint().hasTwoMarks()) {

                for (GPSFixMoving gpsFix : gpsFixes.getFixes()) {

                    Distance distance = distanceToSingleWayPoint(markTracks, gpsFix, w);

                    if (distance.getMeters() > 100 && gpsFix.getTimePoint().after(raceStart)) {

                        possibleMarkPasses.put(distance.getMeters(), gpsFix);
                    }
                }
            }

            /*
             * // if Gate if(w.getControlPoint().hasTwoMarks()&&!w.getControlPoint().isLine()){ }
             */

            @SuppressWarnings("unchecked")
            LinkedList<Double> distances = (LinkedList<Double>) possibleMarkPasses.keySet();

            while (distances.size() > 1) {

                if (distances.get(0) >= distances.get(1)) {

                    possibleMarkPasses.remove(distances.get(0));
                    distances.remove(0);

                } else {
                    possibleMarkPasses.remove(distances.get(1));
                    distances.remove(1);

                }
            }

            MarkPassing m = new MarkPassingImpl(possibleMarkPasses.get(0).getTimePoint(), w, gpsFixes.getTrackedItem());
            computedMarkPasses.put(w, m);
        }
        return computedMarkPasses;
    }

    // Distance from a Competitor to a Waypoint

    private Distance distanceToSingleWayPoint(
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markTracks, GPSFixMoving gps,
            Waypoint w) {

        return gps.getPosition().getDistance(
                markTracks.get(w).iterator().next().getEstimatedPosition(gps.getTimePoint(), true));

    }

    // create a StraightLine for a WayPoint at a specific TimePoint

    private StraightLine calculateStraightLine(TimePoint p, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks) {

        SortedSet<DynamicGPSFixTrack<Mark, GPSFix>> tracks = new TreeSet<DynamicGPSFixTrack<Mark, GPSFix>>();

        for (DynamicGPSFixTrack<Mark, GPSFix> markTrack : markTracks) {

            tracks.add(markTrack);
        }

        Vector2D location = new Vector2D(tracks.first().getEstimatedPosition(p, true));
        Vector2D direction = location.subtract(new Vector2D(tracks.last().getEstimatedPosition(p, true)));

        return new StraightLine(location, direction);

    }
}