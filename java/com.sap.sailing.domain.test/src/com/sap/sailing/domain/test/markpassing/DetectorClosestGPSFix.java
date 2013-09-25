package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
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
        System.out.println("Competitor started");
        LinkedHashMap<Waypoint, MarkPassing> computedMarkPasses = new LinkedHashMap<Waypoint, MarkPassing>();

        for (Waypoint w : markTracks.keySet()) {
            TreeMap<Double, GPSFixMoving> possibleMarkPasses = new TreeMap<Double, GPSFixMoving>();
            System.out.println("TreeMap initiated");
            try {
                gpsFixes.lockForRead();
                for (GPSFixMoving gpsFix : gpsFixes.getFixes()) {
                  
                    // //// If Single Mark or Offset (TODO)
                    /*
                     * if (w.getPassingInstructions().equals("PORT") || w.getPassingInstructions().equals("STARBOARD")
                     * || w.getPassingInstructions().equals("OFFSET")) {
                     */

                    Distance distance = distanceToSingleWayPoint(markTracks, gpsFix, w);

                    if (distance.getMeters() > 100 && gpsFix.getTimePoint().after(raceStart)) {

                        possibleMarkPasses.put(distance.getMeters(), gpsFix);
                    }

                } System.out.println("GPSFixes added");
            } finally {
                gpsFixes.unlockAfterRead();
            }

            /*
             * // If Line if (w.getPassingInstructions().equals("LINE")) {
             * 
             * StraightLine lineAtTimepoint = calculateStraightLine(gpsFix.getTimePoint(), markTracks.get(w)); double
             * distance = lineAtTimepoint.getDistanceToPoint(new Vector2D(gpsFix.getPosition())); if (distance < 300 &&
             * gpsFix.getTimePoint().after(raceStart)) {
             * 
             * possibleMarkPasses.put(distance, gpsFix); }
             * 
             * }
             * 
             * // If Gate if (w.getPassingInstructions().equals("GATE")) {
             * 
             * Vector2D gate = new Vector2D(markTracks.get(w).get(0) .getEstimatedPosition(gpsFix.getTimePoint(),
             * true)).subtract(new Vector2D(markTracks .get(w).get(1).getEstimatedPosition(gpsFix.getTimePoint(),
             * true))); for (int i = 0; markTracks.get(w).get(i).equals(null); i++) {
             * 
             * StraightLine lineForMark = new StraightLine(new Vector2D(markTracks.get(w).get(i)
             * .getEstimatedPosition(gpsFix.getTimePoint(), true)), gate.getPerpendicularVector()); double distance =
             * lineForMark.getDistanceToPoint(new Vector2D(gpsFix.getPosition())); if (distance < 300 &&
             * gpsFix.getTimePoint().after(raceStart)) {
             * 
             * possibleMarkPasses.put(distance, gpsFix); }
             * 
             * }
             * 
             * }
             */

            /* } */
            System.out.println("Timepoint: "+possibleMarkPasses.firstEntry().getValue().getTimePoint() +"\nWaypoint: " + w + "\nCompetitor: " +
                    gpsFixes.getTrackedItem());
            MarkPassingImpl m = new MarkPassingImpl(possibleMarkPasses.firstEntry().getValue().getTimePoint(), w,
                    gpsFixes.getTrackedItem());
            computedMarkPasses.put(w, m);
            System.out.println("Timepoint: "+possibleMarkPasses.firstEntry().getValue().getTimePoint() +"\nWaypoint: " + w + "\nCompetitor: " +
                    gpsFixes.getTrackedItem());

        }
        System.out.println("Competitor finished");
        return computedMarkPasses;

    }

    // Distance from a Competitor to a Waypoint

    private Distance distanceToSingleWayPoint(
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markTracks, GPSFixMoving gps,
            Waypoint w) {

        return gps.getPosition().getDistance(
                markTracks.get(w).iterator().next().getEstimatedPosition(gps.getTimePoint(), true));

    }

    /*
     * // create a StraightLine for a WayPoint at a specific TimePoint
     * 
     * private StraightLine calculateStraightLine(TimePoint p, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks) {
     * 
     * SortedSet<DynamicGPSFixTrack<Mark, GPSFix>> tracks = new TreeSet<DynamicGPSFixTrack<Mark, GPSFix>>();
     * 
     * for (DynamicGPSFixTrack<Mark, GPSFix> markTrack : markTracks) {
     * 
     * tracks.add(markTrack); }
     * 
     * Vector2D location = new Vector2D(tracks.first().getEstimatedPosition(p, true)); Vector2D direction =
     * location.subtract(new Vector2D(tracks.last().getEstimatedPosition(p, true)));
     * 
     * return new StraightLine(location, direction);
     * 
     * }
     */
}