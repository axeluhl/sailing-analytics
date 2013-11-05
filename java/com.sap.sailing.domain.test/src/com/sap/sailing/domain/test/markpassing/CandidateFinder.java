package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class CandidateFinder{

    @SuppressWarnings("serial")
  
    public LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> findCandidates(
            ArrayList<GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions,
            Double boatLength, LinkedHashMap<Waypoint, Double> legLength) {
        LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> allCandidates = new LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>>();

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
        for (Waypoint wp : markPositions.keySet()) {

            // Calculate Distances
            LinkedHashMap<GPSFixMoving, Double> distances = new LinkedHashMap<>();
            LinkedHashMap<GPSFixMoving, Double> relativToHotzoneAndCourseSize = new LinkedHashMap<>();
            ArrayList<Position> pos0 = new ArrayList<>();
            for (int i = 0; i < markPositions.get(wp).size(); i++) {
                pos0.add(markPositions.get(wp).get(i).get(gpsFixes.get(0).getTimePoint()));
            }
            double costMinus = computeDistance(wp.getPassingInstructions(), pos0, gpsFixes.get(0));
            ArrayList<Position> pos1 = new ArrayList<>();
            for (int i = 0; i < markPositions.get(wp).size(); i++) {
                pos1.add(markPositions.get(wp).get(i).get(gpsFixes.get(1).getTimePoint()));
            }
            double cost = computeDistance(wp.getPassingInstructions(), pos1, gpsFixes.get(1));
            double costPlus;

            for (int i = 1; i < gpsFixes.size(); i++) {
                ArrayList<Position> pos = new ArrayList<>();
                for (int j = 0; j < markPositions.get(wp).size(); j++) {
                    pos.add(markPositions.get(wp).get(j).get(gpsFixes.get(i).getTimePoint()));
                }
                costPlus = computeDistance(wp.getPassingInstructions(), pos, gpsFixes.get(i));

                if (cost < costMinus && cost < costPlus) {
                    distances.put(gpsFixes.get(i), cost);
                }
                costMinus = cost;
                cost = costPlus;
            }
            for (GPSFixMoving gps : distances.keySet()) {
                if (distances.get(gps) < boatLength*3){
                    relativToHotzoneAndCourseSize.put(gps, distances.get(gps)/legLength.get(wp));
                } else {
                    relativToHotzoneAndCourseSize.put(gps, Math.pow(distances.get(gps), 3)/legLength.get(wp));
                }
            }
            allCandidates.put(wp, relativToHotzoneAndCourseSize);
            // TODO Factor in if the candidate is behind the mark (Splining?)
        }
        return allCandidates;
    }

    private Double computeDistance(PassingInstruction p, ArrayList<Position> markPositions, GPSFixMoving gps) {
        double distance = 0;

        if (p.equals(PassingInstruction.Port) || p.equals(PassingInstruction.Starboard)
                || p.equals(PassingInstruction.Offset)) {
            distance = gps.getPosition().getDistance(markPositions.get(0)).getMeters();
        }
        if (p.equals(PassingInstruction.Line)) {
        	//TODO Distance to Line!!
        	distance = gps.getPosition().crossTrackError(markPositions.get(0), markPositions.get(0).getBearingGreatCircle(markPositions.get(1))).getMeters();
        }
        if (p.equals(PassingInstruction.Gate)) {
            // TODO Choose only correct Mark to avoid nonsensical Candidates (Splining?)
            if (gps.getPosition().getDistance(markPositions.get(0)).getMeters() < gps.getPosition()
                    .getDistance(markPositions.get(1)).getMeters()) {
                distance = gps.getPosition().getDistance(markPositions.get(0)).getMeters();
            } else {
                distance = gps.getPosition().getDistance(markPositions.get(1)).getMeters();
            }
        }
        return distance;
    }
}