package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import com.sap.sailing.domain.common.impl.Util.Pair;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CandidateFinder implements AbstractCandidateFinder {

    private LinkedHashMap<Competitor, List<GPSFix>> affectedFixes = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<GPSFix, LinkedHashMap<Waypoint, Double>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, List<GPSFix>>> candidates = new LinkedHashMap<>();
    private TrackedRace race;
    private double penaltyForSkipping = 1 - Edge.penaltyForSkipped;

    // Calculate for some Waypoints instead of all?

    public CandidateFinder(TrackedRace race) {
        this.race = race;
        for (Competitor c : race.getRace().getCompetitors()) {
            affectedFixes.put(c, new ArrayList<GPSFix>());
            distances.put(c, new LinkedHashMap<GPSFix, LinkedHashMap<Waypoint, Double>>());
            candidates.put(c, new LinkedHashMap<Waypoint, List<GPSFix>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                candidates.get(c).put(w, new ArrayList<GPSFix>());
            }
        }
    }

    @Override
    public void calculateFixesAffectedByNewCompetitorFixes(List<GPSFix> fixes, Competitor c) {
        if (!affectedFixes.containsKey(c)) {
            affectedFixes.put(c, new ArrayList<GPSFix>());
        }
        affectedFixes.get(c).addAll(fixes);
        calculateDistances(c);
        ArrayList<GPSFix> fixesToBeReevaluated = new ArrayList<>();
        for (GPSFix fix : fixes) {
            fixesToBeReevaluated.add(fix);
            if (!(race.getTrack(c).getLastFixBefore(fix.getTimePoint()) == null)
                    && !fixesToBeReevaluated.contains(race.getTrack(c).getLastFixBefore(fix.getTimePoint()))) {
                fixesToBeReevaluated.add(race.getTrack(c).getLastFixBefore(fix.getTimePoint()));
            }
            if (!(race.getTrack(c).getFirstFixAfter(fix.getTimePoint()) == null)
                    && !fixesToBeReevaluated.contains(race.getTrack(c).getFirstFixAfter(fix.getTimePoint()))) {
                fixesToBeReevaluated.add(race.getTrack(c).getFirstFixAfter(fix.getTimePoint()));
            }
        }
    }

    @Override
    public void calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps) {
        TreeSet<GPSFix> fixes = new TreeSet<GPSFix>(new Comparator<GPSFix>() {
            @Override
            public int compare(GPSFix o1, GPSFix o2) {
                return o1.getTimePoint().compareTo(o2.getTimePoint());
            }
        });

        TimePoint start = fixes.first().getTimePoint();
        GPSFix nextMarkFixAfterLastGivenFix = race.getOrCreateTrack(mark).getFirstFixAfter(fixes.last().getTimePoint());
        TimePoint end = null;
        if (!(nextMarkFixAfterLastGivenFix == null)) {
            end = race.getOrCreateTrack(mark).getFirstFixAfter(start).getTimePoint();
        }

        for (Competitor c : distances.keySet()) {
            if (!affectedFixes.containsKey(c)) {
                affectedFixes.put(c, new ArrayList<GPSFix>());
            }
            TimePoint t = start;
            while (race.getTrack(c).getFirstFixAfter(t) != null) {
                GPSFix nextFix = race.getTrack(c).getFirstFixAfter(t);
                t = nextFix.getTimePoint();
                if (end != null && !t.before(end)) {
                    break;
                }
                affectedFixes.get(c).add(nextFix);
            }
            calculateDistances(c);
        }
    }

    @Override
    public void reCalculateAllFixes(Competitor c) {
        try {
            race.getTrack(c).lockForRead();
            for (GPSFix fix : race.getTrack(c).getFixes()) {
                affectedFixes.get(c).add(fix);
            }
        } finally {
            race.getTrack(c).unlockAfterRead();
        }
        calculateDistances(c);
    }

    @Override
    public Iterable<Competitor> getAffectedCompetitors() {
        return affectedFixes.keySet();
    }

    @Override
    public Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(Competitor c) {
        // CreateCandidate method?
        ArrayList<Candidate> newCans = new ArrayList<>();
        ArrayList<Candidate> wrongCans = new ArrayList<>();
        for (GPSFix gps : affectedFixes.get(c)) {
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                if (fixIsACandidate(gps, w, c) && !candidates.get(c).get(w).contains(gps)) {
                    candidates.get(c).get(w).add(gps);
                    Candidate newCandidate = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1,
                            gps.getTimePoint(), getLikelyhood(c, w, gps), w);
                    newCans.add(newCandidate);
                }

                if (!fixIsACandidate(gps, w, c) && candidates.get(c).get(w).contains(gps)) {
                    candidates.get(c).get(w).remove(gps);
                    Candidate badCandidate = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1,
                            gps.getTimePoint(), getLikelyhood(c, w, gps), w);
                    wrongCans.add(badCandidate);
                }
            }
        }
        affectedFixes.get(c).clear();
        return new Pair<List<Candidate>, List<Candidate>>(newCans, wrongCans);
    }

    private void calculateDistances(Competitor c) {
        for (GPSFix fix : affectedFixes.get(c)) {
            distances.get(c).put(fix, new LinkedHashMap<Waypoint, Double>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                double distance = 0;
                PassingInstruction p = w.getPassingInstructions();
                if (p == null) {

                    if (w.equals(race.getRace().getCourse().getFirstWaypoint())
                            || w.equals(race.getRace().getCourse().getLastWaypoint())) {
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
                        } else if (numberofMarks == 1) {
                            p = PassingInstruction.Port;
                        } else {
                            p = PassingInstruction.None;
                        }
                    }
                }
                ArrayList<Position> positions = new ArrayList<>();
                for (Mark m : w.getMarks()) {
                    positions.add(race.getOrCreateTrack(m).getEstimatedPosition(fix.getTimePoint(), false));
                }
                if (p.equals(PassingInstruction.Port) || p.equals(PassingInstruction.Starboard)
                        || p.equals(PassingInstruction.Offset) || p.equals(PassingInstruction.None)) {
                    distance = fix.getPosition().getDistance(positions.get(0)).getMeters();
                }
                if (p.equals(PassingInstruction.Line)) {
                    distance = fix.getPosition().getDistanceToLine(positions.get(0), positions.get(1)).getMeters();
                    if (race.getRace().getCourse().getIndexOfWaypoint(w) == 0) {
                    }
                }
                if (p.equals(PassingInstruction.Gate)) {
                    // Right now, both marks are observered, even though only one is actully rounded.
                    if (fix.getPosition().getDistance(positions.get(0)).getMeters() < fix.getPosition()
                            .getDistance(positions.get(1)).getMeters()) {
                        distance = fix.getPosition().getDistance(positions.get(0)).getMeters();
                    } else {
                        distance = fix.getPosition().getDistance(positions.get(1)).getMeters();
                    }
                }
                distances.get(c).get(fix).put(w, distance);
            }
        }
    }

    private double getLegLength(TimePoint t, Waypoint w) {
        if (w == race.getRace().getCourse().getFirstWaypoint()) {
            return race.getTrackedLegStartingAt(w).getGreatCircleDistance(t).getMeters();
        } else if (w == race.getRace().getCourse().getLastWaypoint()) {
            return race.getTrackedLegFinishingAt(w).getGreatCircleDistance(t).getMeters();
        } else {
            return (race.getTrackedLegStartingAt(w).getGreatCircleDistance(t).getMeters() + race
                    .getTrackedLegFinishingAt(w).getGreatCircleDistance(t).getMeters()) / 2;
        }
    }

    private boolean fixIsACandidate(GPSFix fix, Waypoint w, Competitor c) {
        GPSFix fixBefore = race.getTrack(c).getLastFixBefore(fix.getTimePoint());
        GPSFix fixAfter = race.getTrack(c).getFirstFixAfter(fix.getTimePoint());
        if (distances.get(c).get(fix) != null && !(fixBefore == null)
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixBefore).get(w) && !(fixAfter == null)
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixAfter).get(w)
                && getLikelyhood(c, w, fix) > penaltyForSkipping) {
            return true;
        }
        return false;
    }

    private double getLikelyhood(Competitor c, Waypoint w, GPSFix fix) {
        return 1 / (10 * Math.abs(distances.get(c).get(fix).get(w) / getLegLength(fix.getTimePoint(), w)) + 1);
    }
}
