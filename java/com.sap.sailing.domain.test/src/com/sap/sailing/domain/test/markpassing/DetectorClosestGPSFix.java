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
    //    for(int i=0; i<200; i++){
       // System.out.println("Fix " + i + ": " + gpsFixes.get(i).getTimePoint() + ": " + distanceToWayPoint(markTracks,
       //              gpsFixes.get(i)).getMeters());}
   //     int fixesInZone = 0;

    //    int totalFixes = 0;
        try {
       //     System.out.println(lastWayPoint.asDate());

            ArrayList<GPSFixMoving> allFixes = gpsFixes;

            // Remove GPSFixes before last MarkPassing

            Iterator<GPSFixMoving> it = allFixes.iterator();
            while (it.hasNext()) {

         //       totalFixes++;
                if (it.next().getTimePoint().before(lastWayPoint)) {
                    it.remove();
                }
            }

            // Create "Hotspot"
            int startOfHotspot = 0;
            int endOfHotspot = allFixes.size() - 1;

            for (int i = 0; i < allFixes.size(); i++) {
              //  System.out.println(i + ": "+ allFixes.get(i).getTimePoint() + ": " + distanceToWayPoint(markTracks,
              //      allFixes.get(i)).getMeters());

                if (distanceToWayPoint(markTracks, allFixes.get(i)).getMeters() < 250) {
                    startOfHotspot = i;
        //            System.out.println(i + " Start: " + distanceToWayPoint(markTracks,
         //           allFixes.get(i)).getMeters());
                    break;
                }

            }

            for (int i = startOfHotspot; i < allFixes.size(); i++) {
                endOfHotspot = i;
                if (distanceToWayPoint(markTracks, allFixes.get(i)).getMeters() > 250) {

              //      System.out.println(i + " End: " + distanceToWayPoint(markTracks,
              //      allFixes.get(i)).getMeters());
                    break;
                }
            }
            List<GPSFixMoving> hotspot = allFixes.subList(startOfHotspot, endOfHotspot);

    //        System.out.println("Hotspot: " + hotspot.size());
            if (hotspot.size() > 0) {
                Distance smallestDistance = distanceToWayPoint(markTracks, hotspot.get(0));

                for (GPSFixMoving gpsFix : hotspot) {
                    // TODO recognize passing instructions

                    Distance distance = distanceToWayPoint(markTracks, gpsFix);

                    if (distance.getMeters() <= smallestDistance.getMeters()) {

                        smallestDistance = distance;
                        t = gpsFix.getTimePoint();

                    }

                }
            } else
                System.out.println("No Fixes in Hotzone");

        //    System.out.println("Total Fixes: " + totalFixes);
         //   System.out.println("Fixes in Zone: " + fixesInZone);

        }catch(NullPointerException e){
            System.out.println("No previous Markpass");
            
        }
               
            return t;
        
    }

    // Distance from a Competitor to a Waypoint

    private Distance distanceToWayPoint(ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, GPSFixMoving gps) {
        if (markTracks.size() == 2) {

            if (gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true))
                    .getMeters() < gps.getPosition()
                    .getDistance(markTracks.get(1).getEstimatedPosition(gps.getTimePoint(), true)).getMeters()) {

                return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true));

            } else {
                return gps.getPosition().getDistance(markTracks.get(1).getEstimatedPosition(gps.getTimePoint(), true));
            }
        } else {
            return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true));
        }
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