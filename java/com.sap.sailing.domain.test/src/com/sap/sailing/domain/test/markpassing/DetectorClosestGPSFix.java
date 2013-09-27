package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DetectorClosestGPSFix implements DetectorMarkPassing {

    @Override
    public TimePoint computeMarkpass(ArrayList<GPSFixMoving> gpsFixes,
            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, TimePoint lastWayPoint) {

        TimePoint t = null;
        // int fixesInZone = 0;
        // int fixesOutOfZone = 0;
        // int totalFixes = 0;

        ArrayList<GPSFixMoving> allFixes = gpsFixes;
        System.out.println("All Fixes: "+allFixes.size());

        // Remove GPSFixes before last MarkPassing
        Iterator<GPSFixMoving> it = allFixes.iterator();
        while (it.hasNext()) {
            // totalFixes++;
            if (it.next().getTimePoint().before(lastWayPoint)) {
                it.remove();
            }
        }
        System.out.println("To early removed: "+ allFixes.size());

        // Create "Hotspot"
        int startOfHotspot = 0;
        int endOfHotspot = 0;

        for (int i = 0; i < allFixes.size(); i++) {
            if (distanceToSingleWayPoint(markTracks, allFixes.get(i)).getMeters() < 100) {
                startOfHotspot = i;
                System.out.println(i+" Start: "+ distanceToSingleWayPoint(markTracks, allFixes.get(i)).getMeters());
                break;
            }

        }


        for (int i = startOfHotspot; i < allFixes.size(); i++) {
            if (distanceToSingleWayPoint(markTracks, allFixes.get(i)).getMeters() > 100) {
                endOfHotspot = i;
                System.out.println(i+" End: "+ distanceToSingleWayPoint(markTracks, allFixes.get(i)).getMeters());
                break;
            }
        }
        List<GPSFixMoving> hotspot = allFixes.subList(startOfHotspot, endOfHotspot);
        
        System.out.println("Hotspot: "+ hotspot.size());

        Distance oldDistance = distanceToSingleWayPoint(markTracks, hotspot.get(0));
        System.out.println(oldDistance.getMeters());

        for (GPSFixMoving gpsFix : hotspot) {
            // TODO recognize passing instructions
            // fixesInZone++;
            Distance distance = distanceToSingleWayPoint(markTracks, gpsFix);

            if (distance.getMeters() < oldDistance.getMeters()) {

                oldDistance = distance;
                t = gpsFix.getTimePoint();
            }

        }

        // System.out.println("Total Fixes: " + totalFixes);
        // System.out.println("Fixes in Zone: " + fixesInZone);

        return t;
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