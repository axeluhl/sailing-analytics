package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;

public class CandidateFinder {

    ArrayList<Waypoint> waypoints;
    LinkedHashMap<Mark, DynamicGPSFixTrack<Mark, GPSFix>> markTracks;
    LinkedHashMap<Waypoint, Double> legLengths;
    
    LinkedHashMap<Competitor, DynamicGPSFixTrack<Competitor, GPSFix>> competitorTracks;
    LinkedHashMap<Competitor, LinkedHashMap<GPSFix, ArrayList<Double>>> distances;
    LinkedHashMap<Competitor, ArrayList<GPSFix>> candidates;
    

    public void newCompetitorFix(GPSFix fix, Competitor c) {
        competitorTracks.get(c).addGPSFix(fix);
        distances.get(c).put(fix, new ArrayList<Double>());
        for (Waypoint w : waypoints) {
            double distance = calculateDistance(fix, w);
            distances.get(c).get(fix).add(waypoints.indexOf(w), distance);
        }
        ArrayList<GPSFix> fixesToBeReevaluated = new ArrayList<>();
        fixesToBeReevaluated.add(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint()));
        fixesToBeReevaluated.add(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint()));
        fixesToBeReevaluated.add(fix);
        for(Waypoint w : waypoints){
        reevaluateFixes(fixesToBeReevaluated, c, w);
        }
    }
    
    public void newMarkFix(Mark mark, GPSFix fix){
        markTracks.get(mark).addGPSFix(fix);
        TimePoint start = fix.getTimePoint();
        TimePoint end = markTracks.get(mark).getFirstFixAfter(start).getTimePoint();
        
        for(Waypoint w : waypoints){
            Iterator<Mark> it = w.getMarks().iterator();
            while (it.hasNext()){
                if (it.next().equals(mark)) {
                    for (Competitor c : competitorTracks.keySet()) {
                        ArrayList<GPSFix> fixesAffected = new ArrayList<>();
                        TimePoint t = start;
                        while (true) {
                            GPSFix nextFix = competitorTracks.get(c).getFirstFixAfter(t);
                            t = nextFix.getTimePoint();
                            if (!t.before(end)) {
                                break;
                            }
                            fixesAffected.add(nextFix);
                        }
                        for (GPSFix gps : fixesAffected) {
                            distances.get(c).get(gps).add(waypoints.indexOf(w), calculateDistance(gps, w));
                        }
                        fixesAffected.add(competitorTracks.get(c).getFirstFixAtOrAfter(end));
                        fixesAffected.add(competitorTracks.get(c).getLastFixAtOrBefore(start));
                        if(!fixesAffected.contains(competitorTracks.get(c).getLastFixBefore(start))){
                            fixesAffected.add(competitorTracks.get(c).getLastFixBefore(start));
                        }
                        reevaluateFixes(fixesAffected, c, w);
                    }
                    break;
                }
            }
        }
    }

    private void reevaluateFixes(ArrayList<GPSFix> fixes, Competitor c, Waypoint w) {
        // TODO Notify Chooser
        for (GPSFix gps : fixes) {
                if (fixIsACandidate(gps, w, c) && !candidates.get(c).contains(gps)) {
                    candidates.get(c).add(gps);
                    Candidate newCandidate = new Candidate(w, gps.getTimePoint(), getCost(distances.get(c).get(gps)
                            .get(waypoints.indexOf(w)), legLengths.get(w)),
                            waypoints.indexOf(w) + 1);
                }

                if (!fixIsACandidate(gps, w, c) && candidates.get(c).contains(gps)) {
                    candidates.remove(gps);
                    Candidate badCandidate = new Candidate(w, gps.getTimePoint(), getCost(distances.get(c).get(gps)
                            .get(waypoints.indexOf(w)), legLengths.get(w)), waypoints.indexOf(w) + 1);
                }
            
        }
    }
    
    private double getCost(Double distance, Double legLength) {
        if (distance < legLength / 100) {
            return distance/legLength;
        } else {
            // Isn't 10000 way to high? Wouldn't these Candidates be skipped automatically?
            return distance / legLength * 10000;
        }
    }

    private boolean fixIsACandidate(GPSFix fix, Waypoint w, Competitor c) {
        if (distances.get(c).get(fix).get(waypoints.indexOf(w)) < distances.get(c)
                .get(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint())).get(waypoints.indexOf(w))
                && distances.get(c).get(fix).get(waypoints.indexOf(w)) < distances.get(c)
                        .get(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint())).get(waypoints.indexOf(w))) {
            return true;
        }
      return false;
    }

    private double calculateDistance(GPSFix gps, Waypoint w) {
        double distance = 0;
        PassingInstruction p = w.getPassingInstructions();
        ArrayList<Position> positions = new ArrayList<>();
        Iterator<Mark> it = w.getMarks().iterator();
        while (it.hasNext()) {
            positions.add(markTracks.get(it.next()).getEstimatedPosition(gps.getTimePoint(), true));
        }
        if (p.equals(PassingInstruction.Port) || p.equals(PassingInstruction.Starboard)
                || p.equals(PassingInstruction.Offset)) {
            distance = gps.getPosition().getDistance(positions.get(0)).getMeters();
        }
        if (p.equals(PassingInstruction.Line)) {
            distance = gps.getPosition().getDistanceToLine(positions.get(0), positions.get(1)).getMeters();
        }
        if (p.equals(PassingInstruction.Gate)) {
            // Right now, both marks are observered, even though only one is actully rounded.
            if (gps.getPosition().getDistance(positions.get(0)).getMeters() < gps.getPosition()
                    .getDistance(positions.get(1)).getMeters()) {
                distance = gps.getPosition().getDistance(positions.get(0)).getMeters();
            } else {
                distance = gps.getPosition().getDistance(positions.get(1)).getMeters();
            }
        }
        return distance;
    }
}
/*
    public LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> findCandidates(
            ArrayList<GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions, Double boatLength,
            LinkedHashMap<Waypoint, Double> legLength) {
        LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> allCandidates = new LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>>();

        for (Waypoint wp : markPositions.keySet()) {

            // Calculate Distances
            LinkedHashMap<GPSFixMoving, Double> distances = new LinkedHashMap<>();
            LinkedHashMap<GPSFixMoving, Double> costRelativCourseSize = new LinkedHashMap<>();

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
                if (distances.get(gps) < legLength.get(wp) / 100) {
                    costRelativCourseSize.put(gps, distances.get(gps) / legLength.get(wp));
                } else {
                    //  is 10000 way to high? would is skip automatically? so wouldnt work on faraway passes?
                    costRelativCourseSize.put(gps, distances.get(gps) / legLength.get(wp) * 10000);
                }
            }
            allCandidates.put(wp, costRelativCourseSize);
            //  Factor in if the candidate is behind the mark (Splining?)
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
            distance = gps.getPosition().getDistanceToLine(markPositions.get(0), markPositions.get(1)).getMeters();
        }
        if (p.equals(PassingInstruction.Gate)) {
            //  Choose only correct Mark to avoid nonsensical Candidates
            // (Splining?)
            if (gps.getPosition().getDistance(markPositions.get(0)).getMeters() < gps.getPosition()
                    .getDistance(markPositions.get(1)).getMeters()) {
                distance = gps.getPosition().getDistance(markPositions.get(0)).getMeters();
            } else {
                distance = gps.getPosition().getDistance(markPositions.get(1)).getMeters();
            }
        }
        return distance;
    }
}*/