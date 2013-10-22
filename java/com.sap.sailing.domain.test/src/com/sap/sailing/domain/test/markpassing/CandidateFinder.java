package com.sap.sailing.domain.test.markpassing;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstructions;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class CandidateFinder implements AbstractCandidateFinder {

    @SuppressWarnings("serial")
    @Override
    public LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> findCandidates(
            ArrayList<GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> wayPointTracks) {

        LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> examineFixes = new LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>>();

        // Get Waypoint Positions
        List<Timed> timeds = new ArrayList<>();
        for (GPSFixMoving gps : gpsFixes) {
            final TimePoint finalT = gps.getTimePoint();
            timeds.add(new Timed() {
                public TimePoint getTimePoint() {
                    return finalT;
                }
            });
        }
        for (Waypoint wp : wayPointTracks.keySet()) {
            LinkedHashMap<GPSFixMoving, Double> candidates = new LinkedHashMap<>();
            ArrayList<LinkedHashMap<TimePoint, Position>> markPositions = new ArrayList<>();
            for (int i = 0; i < wayPointTracks.get(wp).size(); i++) {
                LinkedHashMap<TimePoint, Position> markPosition = new LinkedHashMap<>();
                Iterator<Timed> itTim = timeds.iterator();
                wayPointTracks.get(wp).get(i).lockForRead();
                try {
                    Iterator<Position> itPos = wayPointTracks.get(wp).get(i).getEstimatedPositions(timeds, true);
                    while (itPos.hasNext()) {
                        markPosition.put(itTim.next().getTimePoint(), itPos.next());
                    }
                    markPositions.add(i, markPosition);
                } finally {
                    wayPointTracks.get(wp).get(i).unlockAfterRead();
                }
            }
            // Calculate Distances
            ArrayList<Position> pos0 = new ArrayList<>();
            for (int i = 0; i < markPositions.size(); i++) {
                pos0.add(markPositions.get(i).get(gpsFixes.get(0).getTimePoint()));
            }
            double costMinus = computeCost(wp.getPassingInstructions(), pos0, gpsFixes.get(0));
            ArrayList<Position> pos1 = new ArrayList<>();
            for (int i = 0; i < markPositions.size(); i++) {
                pos1.add(markPositions.get(i).get(gpsFixes.get(1).getTimePoint()));
            }
            double cost = computeCost(wp.getPassingInstructions(), pos1, gpsFixes.get(1));
            double costPlus;

            for (int i = 1; i < gpsFixes.size(); i++) {
                ArrayList<Position> pos = new ArrayList<>();
                for (int j = 0; j < markPositions.size(); j++) {
                pos.add(markPositions.get(j).get(gpsFixes.get(i).getTimePoint()));
            }
                costPlus = computeCost(wp.getPassingInstructions(), pos, gpsFixes.get(i));

                if (cost < costMinus && cost < costPlus) {
                    candidates.put(gpsFixes.get(i), cost);
                }
                costMinus = cost;
                cost = costPlus;
                
            }
        
    
    examineFixes.put(wp, candidates);
    // TODO Factor in if the candidate is behind the mark!!
}
return examineFixes;
}
    private Double computeCost(PassingInstructions p, ArrayList<Position> markPositions, GPSFixMoving gps){
        double distance = 0;
        
        if(p.equals(PassingInstructions.PORT)||p.equals(PassingInstructions.STARBOARD)||p.equals(PassingInstructions.OFFSET)){
            distance = gps.getPosition().getDistance(markPositions.get(0)).getMeters();
        }
        
        if(p.equals(PassingInstructions.LINE)){
            Line2D line = new Line2D.Double(markPositions.get(0).getLatDeg(), markPositions.get(0).getLngDeg(),
                    markPositions.get(1).getLatDeg(), markPositions.get(1).getLngDeg());
            distance = new CentralAngleDistance(line.ptSegDist(gps.getPosition().getLatDeg(), gps
                    .getPosition().getLngDeg())).getMeters();
        }
        
        if(p.equals(PassingInstructions.GATE)){
            if (gps.getPosition().getDistance(markPositions.get(0)).getMeters() < gps.getPosition()
                    .getDistance(markPositions.get(1)).getMeters()) {
                distance = gps.getPosition().getDistance(markPositions.get(0)).getMeters();
            } else {
                distance = gps.getPosition().getDistance(markPositions.get(1)).getMeters();
            }
        } return distance;
    }
}   
    
    
    
/*    
    
    // Distance from a Competitor to a single WayPoint or Gate
    private Double distanceToGate(ArrayList<Position> markPositions, GPSFixMoving gps) {
        if (gps.getPosition().getDistance(markPositions.get(0)).getMeters() < gps.getPosition()
                .getDistance(markPositions.get(1)).getMeters()) {
            return gps.getPosition().getDistance(markPositions.get(0)).getMeters();
        } else {
            return gps.getPosition().getDistance(markPositions.get(1)).getMeters();
        }
    }

    // Distance to a Line
    private double distanceToLine(ArrayList<Position> markPositions, GPSFixMoving gps) {
        Line2D line = new Line2D.Double(markPositions.get(0).getLatDeg(), markPositions.get(0).getLngDeg(),
                markPositions.get(1).getLatDeg(), markPositions.get(1).getLngDeg());
        CentralAngleDistance distance = new CentralAngleDistance(line.ptSegDist(gps.getPosition().getLatDeg(), gps
                .getPosition().getLngDeg()));
        return distance.getMeters();
    }
    if (wp.getPassingInstructions().equals(PassingInstructions.PORT)) {
        Pair<GPSFixMoving, Double> distance = new Pair<>(gps, gps.getPosition()
                .getDistance(markPositions.get(0).get(gps.getTimePoint())).getMeters());
        distances.add(distance);
    }
    if (wp.getPassingInstructions().equals(PassingInstructions.GATE)) {
        // TODO Choose only correct Mark to avoid nonsensical Candidates!!
        ArrayList<Position> pos = new ArrayList<>();
        pos.add(markPositions.get(0).get(gps.getTimePoint()));
        pos.add(markPositions.get(1).get(gps.getTimePoint()));
        Pair<GPSFixMoving, Double> distance = new Pair<>(gps, distanceToGate(pos, gps));
        distances.add(distance);
    }
    if (wp.getPassingInstructions().equals(PassingInstructions.LINE)) {
        ArrayList<Position> pos = new ArrayList<>();
        pos.add(markPositions.get(0).get(gps.getTimePoint()));
        pos.add(markPositions.get(1).get(gps.getTimePoint()));
        Pair<GPSFixMoving, Double> distance = new Pair<>(gps, distanceToLine(pos, gps));
        distances.add(distance);
    }
    for (int i = 1; i < distances.size() - 1; i++) {

        if (distances.get(i).getB() < distances.get(i - 1).getB()
                && distances.get(i).getB() < distances.get(i + 1).getB()) {
            candidates.put(distances.get(i).getA(), distances.get(i).getB());*/
        


