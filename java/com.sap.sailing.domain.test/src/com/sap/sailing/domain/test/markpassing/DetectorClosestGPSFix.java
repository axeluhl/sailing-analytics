package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DetectorClosestGPSFix implements DetectorMarkPassing {

    @Override
    public TimePoint computeMarkpass(DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes,
            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, TimePoint lastWayPoint) {

        System.out.println(!gpsFixes.equals(null));
        System.out.println(!markTracks.equals(null));
        System.out.println("Recieved TimePoint");
        System.out.println(lastWayPoint);

        TreeMap<Double, GPSFixMoving> possibleMarkPasses = new TreeMap<Double, GPSFixMoving>();

        // int numberOfMarks = markTracks.size();

        try {
            gpsFixes.lockForRead();

            for (GPSFixMoving gpsFix : gpsFixes.getFixes()) {

                // //// If Single Mark or Offset (TODO)
                /*
                 * if (w.getPassingInstructions().equals("PORT") || w.getPassingInstructions().equals("STARBOARD") ||
                 * w.getPassingInstructions().equals("OFFSET")) {
                 */

                // if (numberOfMarks == 1) {

                Distance distance = distanceToSingleWayPoint(markTracks, gpsFix);

                if (distance.getMeters() < 10000 && gpsFix.getTimePoint().after(lastWayPoint)) {

                    possibleMarkPasses.put(distance.getMeters(), gpsFix);
                    System.out.println("Found posssible Fix");

                }

            }
        } finally {
            gpsFixes.unlockAfterRead();
        }

        if (possibleMarkPasses.isEmpty()) {

            return lastWayPoint;
        } else {

            return possibleMarkPasses.firstEntry().getValue().getTimePoint();
        }
    }

    // Distance from a Competitor to a Waypoint

    private Distance distanceToSingleWayPoint(ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, GPSFixMoving gps) {

        return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true));

    }

    // create a StraightLine for a WayPoint at a specific TimePoint
    /*
     * private StraightLine calculateStraightLine(TimePoint p, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks) {
     * 
     * Vector2D location = new Vector2D(markTracks.get(0).getEstimatedPosition(p, true)); Vector2D direction =
     * location.subtract(new Vector2D(markTracks.get(1).getEstimatedPosition(p, true)));
     * 
     * return new StraightLine(location, direction);
     * 
     * }
     */
}