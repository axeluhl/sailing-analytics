package com.sap.sailing.domain.test.markpassing;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DetectorClosestGPSFix implements DetectorMarkPassing {

    @Override
    public LinkedHashMap<ControlPoint, ArrayList<LinkedHashMap<GPSFixMoving, Double>>> findCandidates(
            ArrayList<GPSFixMoving> gpsFixes,
            LinkedHashMap<ControlPoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> controlPointTracks) {

        LinkedHashMap<ControlPoint, ArrayList<LinkedHashMap<GPSFixMoving, Double>>> examineFixes = new LinkedHashMap<ControlPoint, ArrayList<LinkedHashMap<GPSFixMoving, Double>>>();

        for (ControlPoint cp : controlPointTracks.keySet()) {

            ArrayList<LinkedHashMap<GPSFixMoving, Double>> ar = new ArrayList<LinkedHashMap<GPSFixMoving, Double>>();

            int numberofMarks = 0;
            Iterator<Mark> it = cp.getMarks().iterator();
            while (it.hasNext()) {
                it.next();
                numberofMarks++;
            }
            if (numberofMarks == 1) {

                LinkedHashMap<GPSFixMoving, Double> lhs = new LinkedHashMap<GPSFixMoving, Double>();

                for (int i = 1; i < gpsFixes.size() - 1; i++)

                    if (distanceToWayPoint(controlPointTracks.get(cp), gpsFixes.get(i)) < distanceToWayPoint(
                            controlPointTracks.get(cp), gpsFixes.get(i - 1))
                            && distanceToWayPoint(controlPointTracks.get(cp), gpsFixes.get(i)) < distanceToWayPoint(
                                    controlPointTracks.get(cp), gpsFixes.get(i + 1))) {

                        lhs.put(gpsFixes.get(i), distanceToWayPoint(controlPointTracks.get(cp), gpsFixes.get(i)));

                    }

                ar.add(lhs);

            }

            if (numberofMarks == 2) {
                LinkedHashMap<GPSFixMoving, Double> lhsGate = new LinkedHashMap<GPSFixMoving, Double>();
                LinkedHashMap<GPSFixMoving, Double> lhsLine = new LinkedHashMap<GPSFixMoving, Double>();

                for (int i = 1; i < gpsFixes.size() - 1; i++) {

                    if (distanceToWayPoint(controlPointTracks.get(cp), gpsFixes.get(i)) < distanceToWayPoint(
                            controlPointTracks.get(cp), gpsFixes.get(i - 1))
                            && distanceToWayPoint(controlPointTracks.get(cp), gpsFixes.get(i)) < distanceToWayPoint(
                                    controlPointTracks.get(cp), gpsFixes.get(i + 1))) {

                        lhsGate.put(gpsFixes.get(i), distanceToWayPoint(controlPointTracks.get(cp), gpsFixes.get(i)));

                    }
                }
                for (int i = 1; i < gpsFixes.size() - 1; i++)

                    if (distanceToLine(controlPointTracks.get(cp), gpsFixes.get(i)) < distanceToLine(
                            controlPointTracks.get(cp), gpsFixes.get(i - 1))
                            && distanceToLine(controlPointTracks.get(cp), gpsFixes.get(i)) < distanceToLine(
                                    controlPointTracks.get(cp), gpsFixes.get(i + 1))) {

                        lhsLine.put(gpsFixes.get(i), distanceToLine(controlPointTracks.get(cp), gpsFixes.get(i)));
                    }

                ar.add(lhsGate);
                ar.add(lhsLine);

            }
            examineFixes.put(cp, ar);

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

