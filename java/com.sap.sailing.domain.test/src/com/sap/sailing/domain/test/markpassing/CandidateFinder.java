package com.sap.sailing.domain.test.markpassing;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstructions;
import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class CandidateFinder implements AbstractCandidateFinder {

    @Override
    public LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> findCandidates(
            ArrayList<GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> wayPointTracks) {

        LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> examineFixes = new LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>>();

        for (Waypoint wp : wayPointTracks.keySet()) {

            LinkedHashMap<GPSFixMoving, Double> candidates = new LinkedHashMap<GPSFixMoving, Double>();

            if (wp.getPassingInstructions().equals(PassingInstructions.PORT)) {

                for (int i = 1; i < gpsFixes.size() - 1; i++)

                    if (distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToWayPoint(
                            wayPointTracks.get(wp), gpsFixes.get(i - 1))
                            && distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToWayPoint(
                                    wayPointTracks.get(wp), gpsFixes.get(i + 1))) {

                        candidates.put(gpsFixes.get(i), distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)));

                    }

            }

            if (wp.getPassingInstructions().equals(PassingInstructions.GATE)) {
                

                for (int i = 1; i < gpsFixes.size() - 1; i++) {

                    if (distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToWayPoint(
                            wayPointTracks.get(wp), gpsFixes.get(i - 1))
                            && distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToWayPoint(
                                    wayPointTracks.get(wp), gpsFixes.get(i + 1))) {

                        candidates.put(gpsFixes.get(i), distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)));

                    }
                }
            }
            if(wp.getPassingInstructions().equals(PassingInstructions.LINE)){
                for (int i = 1; i < gpsFixes.size() - 1; i++)

                    if (distanceToLine(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToLine(
                            wayPointTracks.get(wp), gpsFixes.get(i - 1))
                            && distanceToLine(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToLine(
                                    wayPointTracks.get(wp), gpsFixes.get(i + 1))) {

                        candidates.put(gpsFixes.get(i), distanceToLine(wayPointTracks.get(wp), gpsFixes.get(i)));
                    }



            }
            examineFixes.put(wp, candidates);

        }

        return examineFixes;
    }

    // Distance from a Competitor to a single WayPoint or Gate

    private Double distanceToWayPoint(ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, GPSFixMoving gps) {
        if (markTracks.size() == 2) {

            if (gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true))
                    .getMeters() < gps.getPosition()
                    .getDistance(markTracks.get(1).getEstimatedPosition(gps.getTimePoint(), true)).getMeters()) {

                return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true))
                        .getMeters();

            } else {
                return gps.getPosition().getDistance(markTracks.get(1).getEstimatedPosition(gps.getTimePoint(), true))
                        .getMeters();
            }
        } else {
            return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true))
                    .getMeters();
        }
    }

    // Distance to a Line

    private double distanceToLine(ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, GPSFixMoving gps) {

        Line2D line = new Line2D.Double(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true).getLatDeg(),
                markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true).getLngDeg(), markTracks.get(1)
                        .getEstimatedPosition(gps.getTimePoint(), true).getLatDeg(), markTracks.get(1)
                        .getEstimatedPosition(gps.getTimePoint(), true).getLngDeg());

        CentralAngleDistance distance = new CentralAngleDistance(line.ptSegDist(gps.getPosition().getLatDeg(), gps
                .getPosition().getLngDeg()));

        return distance.getMeters();
    }

}

/*
 * 
 * try {
 * 
 * 
 * // Remove GPSFixes before last MarkPassing
 * 
 * Iterator<GPSFixMoving> it = gpsFixes.iterator(); while (it.hasNext()) {
 * 
 * // totalFixes++; if (it.next().getTimePoint().before(lastWayPoint)) { it.remove(); } }
 * 
 * if (!line) {
 * 
 * // Create "Hotspot" int startOfHotspot = 0; int endOfHotspot = gpsFixes.size() - 1;
 * 
 * for (int i = 0; i < gpsFixes.size(); i++) {
 * 
 * if (distanceToWayPoint(controlPointTracks, gpsFixes.get(i)).getMeters() < 250) { startOfHotspot = i;
 * 
 * break; }
 * 
 * }
 * 
 * for (int i = startOfHotspot; i < gpsFixes.size(); i++) { endOfHotspot = i; if (distanceToWayPoint(controlPointTracks,
 * gpsFixes.get(i)).getMeters() > 250) {
 * 
 * break; } } List<GPSFixMoving> hotspot = gpsFixes.subList(startOfHotspot, endOfHotspot);
 * 
 * // Find smallest disance if (hotspot.size() > 0) { Distance smallestDistance = distanceToWayPoint(controlPointTracks,
 * hotspot.get(0));
 * 
 * for (GPSFixMoving gpsFix : hotspot) {
 * 
 * Distance distance = distanceToWayPoint(controlPointTracks, gpsFix);
 * 
 * if (distance.getMeters() <= smallestDistance.getMeters()) {
 * 
 * smallestDistance = distance; t = gpsFix.getTimePoint();
 * 
 * }
 * 
 * } } else { // System.out.println("No Fixes in Hotzone"); }
 * 
 * // System.out.println("Total Fixes: " + totalFixes); // System.out.println("Fixes in Zone: " + fixesInZone);
 * 
 * 
 * 
 * 
 * } else { // => Line
 * 
 * 
 * // Create "Hotspot" int startOfHotspot = 0; int endOfHotspot = gpsFixes.size() - 1;
 * 
 * for (int i = 0; i < gpsFixes.size(); i++) {
 * 
 * if (distanceToLine(controlPointTracks, gpsFixes.get(i)) < 0.005) { startOfHotspot = i;
 * 
 * 
 * break; }
 * 
 * }
 * 
 * for (int i = startOfHotspot; i < gpsFixes.size(); i++) { endOfHotspot = i; if (distanceToLine(controlPointTracks,
 * gpsFixes.get(i)) > 0.005) {
 * 
 * break; } }
 * 
 * List<GPSFixMoving> hotspot = gpsFixes.subList(startOfHotspot, endOfHotspot);
 * 
 * // distance to line!! if (hotspot.size() > 0) { double smallestDistance = distanceToLine(controlPointTracks,
 * hotspot.get(0));
 * 
 * for (GPSFixMoving gpsFix : hotspot) {
 * 
 * double distance = distanceToLine(controlPointTracks, gpsFix);
 * 
 * if (distance <= smallestDistance) {
 * 
 * smallestDistance = distance; t = gpsFix.getTimePoint();
 * 
 * }
 * 
 * } } else { // System.out.println("No Fixes in Hotzone"); }
 * 
 * // System.out.println("Total Fixes: " + totalFixes); // System.out.println("Fixes in Zone: " + fixesInZone);
 * 
 * } } catch (NullPointerException e) { // System.out.println("No previous Markpass");
 * 
 * }
 * 
 * return t;
 * 
 * }
 * 
 * // Distance to a Line
 * 
 * private double distanceToLine(ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, GPSFixMoving gps) {
 * 
 * Line2D line = new Line2D.Double(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true).getLatDeg(),
 * markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true).getLngDeg(), markTracks.get(1)
 * .getEstimatedPosition(gps.getTimePoint(), true).getLatDeg(), markTracks.get(1)
 * .getEstimatedPosition(gps.getTimePoint(), true).getLngDeg());
 * 
 * double distance = line.ptSegDist(gps.getPosition().getLatDeg(), gps.getPosition().getLngDeg());
 * 
 * return distance; }
 * 
 * // Distance from a Competitor to a single WayPoint or Gate
 * 
 * private Distance distanceToWayPoint(ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markTracks, GPSFixMoving gps) { if
 * (markTracks.size() == 2) {
 * 
 * if (gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true)) .getMeters() <
 * gps.getPosition() .getDistance(markTracks.get(1).getEstimatedPosition(gps.getTimePoint(), true)).getMeters()) {
 * 
 * return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true));
 * 
 * } else { return gps.getPosition().getDistance(markTracks.get(1).getEstimatedPosition(gps.getTimePoint(), true)); } }
 * else { return gps.getPosition().getDistance(markTracks.get(0).getEstimatedPosition(gps.getTimePoint(), true)); } }
 */

