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
                for (int i = 1; i < gpsFixes.size() - 1; i++){
                    if (distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToWayPoint(
                            wayPointTracks.get(wp), gpsFixes.get(i - 1))
                            && distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)) < distanceToWayPoint(
                                    wayPointTracks.get(wp), gpsFixes.get(i + 1))) {
                        candidates.put(gpsFixes.get(i), distanceToWayPoint(wayPointTracks.get(wp), gpsFixes.get(i)));
                    }
                }
            }
            if (wp.getPassingInstructions().equals(PassingInstructions.GATE)) {
                
                //TODO Choose only correct Mark to avoid nonsensical Candidates!!
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
            //TODO Factor in if the candidate is behind the mark!!
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