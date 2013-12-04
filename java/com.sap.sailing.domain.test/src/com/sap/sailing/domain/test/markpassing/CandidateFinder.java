package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

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
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;

public class CandidateFinder {

    private CandidateChooser chooser;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private ArrayList<TrackedLeg> legs = new ArrayList<>();
    private LinkedHashMap<Mark, DynamicGPSFixTrack<Mark, GPSFix>> markTracks = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, DynamicGPSFixTrack<Competitor, GPSFix>> competitorTracks = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<GPSFix, LinkedHashMap<Waypoint, Double>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, ArrayList<GPSFix>>> candidates = new LinkedHashMap<>();

    public CandidateFinder(Iterable<Competitor> competitors, CandidateChooser chooser) {
        upDateWaypoints(waypoints, legs);
        this.chooser = chooser;
        for (Competitor c : competitors) {
            competitorTracks.put(c, new DynamicGPSFixTrackImpl<Competitor>(c, 1000));
            distances.put(c, new LinkedHashMap<GPSFix, LinkedHashMap<Waypoint, Double>>());
            candidates.put(c, new LinkedHashMap<Waypoint, ArrayList<GPSFix>>());

        }
    }

    public void upDateWaypoints(ArrayList<Waypoint> waypoints, ArrayList<TrackedLeg> legs) {
        this.waypoints = waypoints;
        this.legs = legs;
        markTracks.clear();
        Set<Mark> marks = new HashSet<>();
        for (Waypoint w : waypoints) {
            Iterator<Mark> it = w.getMarks().iterator();
            while (it.hasNext()) {
                marks.add(it.next());
            }
        }
        for (Mark m : marks) {
            markTracks.put(m, new DynamicGPSFixTrackImpl<Mark>(m, 1000));
        }
        for (Competitor c : candidates.keySet()) {
            candidates.get(c).clear();
            for (Waypoint w : waypoints) {
                candidates.get(c).put(w, new ArrayList<GPSFix>());
            }
        }
        reCalculateEverything();
    }

    public void newCompetitorFixes(Iterable<GPSFixMoving> fixes, Competitor c) {

        ArrayList<GPSFix> fixesToBeReevaluated = new ArrayList<>();
        for (GPSFix fix : fixes) {

            competitorTracks.get(c).addGPSFix(fix);
            distances.get(c).put(fix, new LinkedHashMap<Waypoint, Double>());
            for (Waypoint w : waypoints) {
                distances.get(c).get(fix).put(w, calculateDistance(fix, w));
            }
            fixesToBeReevaluated.add(fix);
            if (!(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint()) == null)
                    && !fixesToBeReevaluated.contains(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint()))) {
                fixesToBeReevaluated.add(competitorTracks.get(c).getLastFixBefore(fix.getTimePoint()));
            }
            if (!(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint()) == null)
                    && !fixesToBeReevaluated.contains(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint()))) {
                fixesToBeReevaluated.add(competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint()));
            }
        }
        for (Waypoint w : waypoints) {
            reEvaluateFixes(fixesToBeReevaluated, c, w);
        }
    }

    public void newMarkFixes(Mark mark, Iterable<GPSFix> gps) {
        TreeSet<GPSFix> fixes = new TreeSet<GPSFix>(new Comparator<GPSFix>() {
            @Override
            public int compare(GPSFix o1, GPSFix o2) {
                return o1.getTimePoint().compareTo(o2.getTimePoint());
            }
        });
        for (GPSFix fix : gps) {
            markTracks.get(mark).addGPSFix(fix);
            fixes.add(fix);
        }
        TimePoint start = fixes.first().getTimePoint();
        GPSFix nextMarkFixAfterLastGivenFix = markTracks.get(mark).getFirstFixAfter(fixes.last().getTimePoint());
        TimePoint end = null;
        if (!(nextMarkFixAfterLastGivenFix == null)) {
            end = markTracks.get(mark).getFirstFixAfter(start).getTimePoint();
        }

        for (Competitor c : competitorTracks.keySet()) {
            ArrayList<GPSFix> fixesAffected = new ArrayList<>();
            TimePoint t = start;
            while (competitorTracks.get(c).getFirstFixAfter(t) != null) {
                GPSFix nextFix = competitorTracks.get(c).getFirstFixAfter(t);
                t = nextFix.getTimePoint();
                if (end != null && !t.before(end)) {
                    break;
                }
                fixesAffected.add(nextFix);
            }
            for (Waypoint w : waypoints) {
                Iterator<Mark> it = w.getMarks().iterator();
                while (it.hasNext()) {
                    if (it.next().equals(mark)) {
                        for (GPSFix fix : fixesAffected) {
                            distances.get(c).get(fix).put(w, calculateDistance(fix, w));
                        }
                        reEvaluateFixes(fixesAffected, c, w);
                        break;
                    }
                }
            }
        }
    }

    private void reEvaluateFixes(ArrayList<GPSFix> fixes, Competitor c, Waypoint w) {
        ArrayList<Candidate> newCans = new ArrayList<>();
        ArrayList<Candidate> wrongCans = new ArrayList<>();
        for (GPSFix gps : fixes) {
            if (fixIsACandidate(gps, w, c) && !candidates.get(c).get(w).contains(gps)) {
                candidates.get(c).get(w).add(gps);
                Candidate newCandidate = new Candidate(waypoints.indexOf(w) + 1, gps.getTimePoint(), getLikelyhood(
                        distances.get(c).get(gps).get(w), getLegLength(gps.getTimePoint(), waypoints.indexOf(w))), w);
                newCans.add(newCandidate);
            }

            if (!fixIsACandidate(gps, w, c) && candidates.get(c).get(w).contains(gps)) {
                candidates.get(c).get(w).remove(gps);
                Candidate badCandidate = new Candidate(waypoints.indexOf(w) + 1, gps.getTimePoint(), getLikelyhood(
                        distances.get(c).get(gps).get(w), getLegLength(gps.getTimePoint(), waypoints.indexOf(w))), w);
                wrongCans.add(badCandidate);
            }
        }
        chooser.addCandidates(newCans, c);
        chooser.removeCandidates(wrongCans, c);
    }

    private double getLegLength(TimePoint t, Integer w) {
        if (w == 0) {
            return legs.get(w).getGreatCircleDistance(t).getMeters();
        } else if (w == waypoints.size() - 1) {
            return legs.get(waypoints.size() - 2).getGreatCircleDistance(t).getMeters();
        } else {
            return (legs.get(w).getGreatCircleDistance(t).getMeters() + legs.get(w - 1).getGreatCircleDistance(t)
                    .getMeters()) / 2;
        }
    }

    private double getLikelyhood(Double distance, Double legLength) {
        return 1 / (500 * Math.abs(distance / legLength) + 1);
    }

    private boolean fixIsACandidate(GPSFix fix, Waypoint w, Competitor c) {
        GPSFix fixBefore = competitorTracks.get(c).getLastFixBefore(fix.getTimePoint());
        GPSFix fixAfter = competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint());
        if (distances.get(c).get(fix) != null && !(fixBefore == null)
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixBefore).get(w) && !(fixAfter == null)
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixAfter).get(w)) {
            return true;
        }
        return false;
    }

    private void reCalculateEverything() {
        for (Competitor c : competitorTracks.keySet()) {
            ArrayList<GPSFix> fixes = new ArrayList<>();
            try {
                competitorTracks.get(c).lockForRead();
                for (GPSFix fix : competitorTracks.get(c).getFixes()) {
                    fixes.add(fix);
                    for (Waypoint w : waypoints) {
                        distances.get(c).get(fix).put(w, calculateDistance(fix, w));
                        reEvaluateFixes(fixes, c, w);
                    }
                }
            } finally {
                competitorTracks.get(c).unlockAfterRead();
            }
        }
    }

    private double calculateDistance(GPSFix gps, Waypoint w) {
        double distance = 0;
        PassingInstruction p = w.getPassingInstructions();
        if (p == null) {
            int index = waypoints.indexOf(w);
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
            if (waypoints.indexOf(w) == 0) {
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





