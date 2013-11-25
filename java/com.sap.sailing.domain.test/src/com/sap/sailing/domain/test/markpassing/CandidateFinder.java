package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;

public class CandidateFinder {

    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private LinkedHashMap<Mark, DynamicGPSFixTrack<Mark, GPSFix>> markTracks = new LinkedHashMap<>();
    private LinkedHashMap<Waypoint, TrackedLeg> averageLegLengths = new LinkedHashMap<>();
    private CandidateChooser chooser;

    private LinkedHashMap<Competitor, DynamicGPSFixTrack<Competitor, GPSFix>> competitorTracks = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<GPSFix, LinkedHashMap<Waypoint, Double>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, ArrayList<GPSFix>>> candidates = new LinkedHashMap<>();

    public CandidateFinder(ArrayList<Waypoint> waypoints, Iterable<Competitor> competitors, CandidateChooser chooser,
            ArrayList<TrackedLeg> legs) {
        this.waypoints = waypoints;
        upDateLegs(legs);
        this.chooser = chooser;
        for (Competitor c : competitors) {
            competitorTracks.put(c, new DynamicTrackImpl<Competitor, GPSFix>(c, 1000));
            distances.put(c, new LinkedHashMap<GPSFix, LinkedHashMap<Waypoint, Double>>());
            candidates.put(c, new LinkedHashMap<Waypoint, ArrayList<GPSFix>>());
            for (Waypoint w : waypoints) {
                candidates.get(c).put(w, new ArrayList<GPSFix>());
            }
        }
        Set<Mark> marks = new HashSet<>();
        for (Waypoint w : waypoints) {
            Iterator<Mark> it = w.getMarks().iterator();
            while (it.hasNext()) {
                marks.add(it.next());
            }
        }
        for (Mark m : marks) {
            markTracks.put(m, new DynamicTrackImpl<Mark, GPSFix>(m, 1000));
        }
    }

    public void upDateLegs(ArrayList<TrackedLeg> legs) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            averageLegLengths.put(waypoints.get(i), legs.get(i));
        }
    }

    public void newCompetitorFix(GPSFixMoving fix, Competitor c) {
        competitorTracks.get(c).addGPSFix(fix);
        distances.get(c).put(fix, new LinkedHashMap<Waypoint, Double>());
        for (Waypoint w : waypoints) {
            double distance = calculateDistance(fix, w);
            distances.get(c).get(fix).put(w, distance);
        }
        ArrayList<GPSFix> fixesToBeReevaluated = new ArrayList<>();
        fixesToBeReevaluated.add(fix);
        if (!(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint()) == null)) {
            fixesToBeReevaluated.add(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint()));
        }
        if (!(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint()) == null)) {
            fixesToBeReevaluated.add(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint()));
        }
        for (Waypoint w : waypoints) {
            reEvaluateFixes(fixesToBeReevaluated, c, w);
        }
    }

    public void newMarkFix(Mark mark, GPSFix fix) {
        markTracks.get(mark).addGPSFix(fix);
        TimePoint start = fix.getTimePoint();
        GPSFix nextMarkFix = markTracks.get(mark).getFirstFixAfter(start);
        TimePoint end = null;
        if (!(nextMarkFix == null)) {
            end = markTracks.get(mark).getFirstFixAfter(start).getTimePoint();
        }
        for (Waypoint w : waypoints) {
            Iterator<Mark> it = w.getMarks().iterator();
            while (it.hasNext()) {
                if (it.next().equals(mark)) {
                    for (Competitor c : competitorTracks.keySet()) {
                        ArrayList<GPSFix> fixesAffected = new ArrayList<>();
                        TimePoint t = start;
                        while (competitorTracks.get(c).getFirstFixAfter(t) != null) {
                            GPSFix nextFix = competitorTracks.get(c).getFirstFixAfter(t);
                            t = nextFix.getTimePoint();
                            if (end!=null && !t.before(end)) {
                                break;
                            }
                            fixesAffected.add(nextFix);
                        }
                        for (GPSFix gps : fixesAffected) {
                            distances.get(c).get(gps).put(w, calculateDistance(gps, w));
                        }
                        if (fixesAffected.size() > 0) {
                            System.out.println(fixesAffected.size());
                            reEvaluateFixes(fixesAffected, c, w);
                        }
                    }
                }
            }
        }
    }

    private void reEvaluateFixes(ArrayList<GPSFix> fixes, Competitor c, Waypoint w) {
        for (GPSFix gps : fixes) {
            if (fixIsACandidate(gps, w, c) && !candidates.get(c).get(w).contains(gps)) {
                candidates.get(c).get(w).add(gps);
                if (waypoints.indexOf(w) == 0) {
                }
                Candidate newCandidate = new Candidate(w, gps.getTimePoint(), getCost(distances.get(c).get(gps).get(w),
                        getLegLength(gps.getTimePoint(), w)), waypoints.indexOf(w) + 1);
                chooser.addCandidate(newCandidate, c);
            }

            if (!fixIsACandidate(gps, w, c) && candidates.get(c).get(w).contains(gps)) {
                candidates.get(c).get(w).remove(gps);
                Candidate badCandidate = new Candidate(w, gps.getTimePoint(), getCost(distances.get(c).get(gps).get(w),
                        getLegLength(gps.getTimePoint(), w)), waypoints.indexOf(w) + 1);
                chooser.removeCandidate(badCandidate, c);
            }

        }
    }

    private double getLegLength(TimePoint t, Waypoint w) {
        if (waypoints.indexOf(w) == 0) {
            return averageLegLengths.get(w).getGreatCircleDistance(t).getMeters();
        } else if (waypoints.indexOf(w) == waypoints.size() - 1) {
            return averageLegLengths.get(waypoints.get(waypoints.size() - 2)).getGreatCircleDistance(t).getMeters();
        } else {
            return (averageLegLengths.get(w).getGreatCircleDistance(t).getMeters() + averageLegLengths
                    .get(waypoints.get(waypoints.indexOf(w) - 1)).getGreatCircleDistance(t).getMeters()) / 2;
        }
    }

    private double getCost(Double distance, Double legLength) {
        return 1/(500*Math.abs(distance/legLength)+1);
    }

    private boolean fixIsACandidate(GPSFix fix, Waypoint w, Competitor c) {
        GPSFix fixBefore = competitorTracks.get(c).getLastFixBefore(fix.getTimePoint());
        GPSFix fixAfter = competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint());
        if (distances.get(c).containsKey(fix) && !(fixBefore == null)
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixBefore).get(w) && !(fixAfter == null)
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixAfter).get(w)) {
            return true;
        }
        return false;
    }

    private double calculateDistance(GPSFix gps, Waypoint w) {
        double distance = 0;
        PassingInstruction p = w.getPassingInstructions();
        if (p == null) {
            int index = waypoints.indexOf(w);
            if (w.getPassingInstructions() == null) {
                if (index == 0 || index == waypoints.size() - 1) {
                    p = PassingInstruction.Line;
                } else {
                    int numberofMarks = 0;
                    Iterator<Mark> it = w.getMarks().iterator();
                    while (it.hasNext()) {
                        it.next();
                        numberofMarks++;
                    }
                    if (numberofMarks == 2) {
                        p = PassingInstruction.Gate;
                    } else {
                        if (numberofMarks == 1) {
                            p = PassingInstruction.Port;
                        } else {
                            p = PassingInstruction.None;
                        }
                    }
                }
            }
        }
        ArrayList<Position> positions = new ArrayList<>();
        Iterator<Mark> it = w.getMarks().iterator();
        while (it.hasNext()) {
            positions.add(markTracks.get(it.next()).getEstimatedPosition(gps.getTimePoint(), false));
        }
        if (p.equals(PassingInstruction.Port) || p.equals(PassingInstruction.Starboard)
                || p.equals(PassingInstruction.Offset) || p.equals(PassingInstruction.None)) {
            distance = gps.getPosition().getDistance(positions.get(0)).getMeters();
        }
        if (p.equals(PassingInstruction.Line)) {
            distance = gps.getPosition().getDistanceToLine(positions.get(0), positions.get(1)).getMeters();
            if(waypoints.indexOf(w)==0){
            }
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