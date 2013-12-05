package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CandidateFinder implements AbstractCandidateFinder {

    private LinkedHashMap<Competitor, LinkedHashMap<GPSFixMoving, LinkedHashMap<Waypoint, Double>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, List<GPSFixMoving>>> candidates = new LinkedHashMap<>();
    TrackedRace race;

    // Calculate for some Waypoints instead of all?

    public CandidateFinder(TrackedRace race) {
        this.race = race;
        for (Competitor c : race.getRace().getCompetitors()) {
            distances.put(c, new LinkedHashMap<GPSFixMoving, LinkedHashMap<Waypoint, Double>>());
            candidates.put(c, new LinkedHashMap<Waypoint, List<GPSFixMoving>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                candidates.get(c).put(w, new ArrayList<GPSFixMoving>());
            }
            reCalculateEverything();
        }
    }

    public LinkedHashMap<Competitor, List<Candidate>> getAllCandidates() {
        LinkedHashMap<Competitor, List<Candidate>> allCandidates = new LinkedHashMap<>();
        for (Competitor c : candidates.keySet()) {
            List<Candidate> competitorCandidates = new ArrayList<Candidate>();
            for (Waypoint w : candidates.get(c).keySet()) {
                for (GPSFixMoving fix : candidates.get(c).get(w)) {
                    competitorCandidates.add(new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, fix
                            .getTimePoint(), getLikelyhood(distances.get(c).get(fix).get(w),
                            getLegLength(fix.getTimePoint(), w)), w));
                }
            }
            allCandidates.put(c, competitorCandidates);
        }
        return allCandidates;
    }

    @Override
    public LinkedHashMap<Competitor, Pair<List<Candidate>, List<Candidate>>> getCandidateDeltas(
            Pair<LinkedHashMap<Competitor, List<GPSFixMoving>>, LinkedHashMap<Mark, List<GPSFix>>> fixes) {

        LinkedHashMap<Competitor, Pair<List<Candidate>, List<Candidate>>> candidateDeltas = new LinkedHashMap<>();

        LinkedHashMap<Competitor, List<GPSFixMoving>> allAffectedFixes = new LinkedHashMap<>();
        for (Competitor c : fixes.first().keySet()) {
            allAffectedFixes.put(c, newCompetitorFixes(fixes.first().get(c), c));
        }
        for (Mark m : fixes.second().keySet()) {
            LinkedHashMap<Competitor, List<GPSFixMoving>> markAffectedFixes = newMarkFixes(m, fixes.second().get(m));
            for (Competitor c : markAffectedFixes.keySet()) {
                if (!allAffectedFixes.keySet().contains(c)) {
                    allAffectedFixes.put(c, markAffectedFixes.get(c));
                } else {
                    allAffectedFixes.get(c).addAll(markAffectedFixes.get(c));
                }
            }
            for (Competitor c : allAffectedFixes.keySet()) {
                candidateDeltas.put(c, reEvaluateFixes(allAffectedFixes.get(c), c));
            }
        }

        // TODO Auto-generated method stub
        return null;
    }

    private void reCalculateEverything() {
        for (Competitor c : distances.keySet()) {
            ArrayList<GPSFixMoving> fixes = new ArrayList<>();
            try {
                race.getTrack(c).lockForRead();
                for (GPSFixMoving fix : race.getTrack(c).getFixes()) {
                    fixes.add(fix);
                }
            } finally {
                race.getTrack(c).unlockAfterRead();
            }
            calculateDistances(fixes, c);
            reEvaluateFixes(fixes, c);
        }
    }

    private List<GPSFixMoving> newCompetitorFixes(Iterable<GPSFixMoving> fixes, Competitor c) {

        calculateDistances(fixes, c);
        ArrayList<GPSFixMoving> fixesToBeReevaluated = new ArrayList<>();
        for (GPSFixMoving fix : fixes) {
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
        return fixesToBeReevaluated;
    }

    private LinkedHashMap<Competitor, List<GPSFixMoving>> newMarkFixes(Mark mark, Iterable<GPSFix> gps) {
        LinkedHashMap<Competitor, List<GPSFixMoving>> allAffectedFixes = new LinkedHashMap<>();

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
            ArrayList<GPSFixMoving> fixesAffected = new ArrayList<>();
            TimePoint t = start;
            while (race.getTrack(c).getFirstFixAfter(t) != null) {
                GPSFixMoving nextFix = race.getTrack(c).getFirstFixAfter(t);
                t = nextFix.getTimePoint();
                if (end != null && !t.before(end)) {
                    break;
                }
                fixesAffected.add(nextFix);
            }
            calculateDistances(fixesAffected, c);
            allAffectedFixes.put(c, fixesAffected);
        }
        return allAffectedFixes;
    }

    private void calculateDistances(Iterable<GPSFixMoving> fixes, Competitor c) {
        for (GPSFixMoving fix : fixes) {
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

    private Pair<List<Candidate>, List<Candidate>> reEvaluateFixes(Iterable<GPSFixMoving> fixes, Competitor c) {
        ArrayList<Candidate> newCans = new ArrayList<>();
        ArrayList<Candidate> wrongCans = new ArrayList<>();
        for (GPSFixMoving gps : fixes) {
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                if (fixIsACandidate(gps, w, c) && !candidates.get(c).get(w).contains(gps)) {
                    candidates.get(c).get(w).add(gps);
                    Candidate newCandidate = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1,
                            gps.getTimePoint(), getLikelyhood(distances.get(c).get(gps).get(w),
                                    getLegLength(gps.getTimePoint(), w)), w);
                    newCans.add(newCandidate);
                }

                if (!fixIsACandidate(gps, w, c) && candidates.get(c).get(w).contains(gps)) {
                    candidates.get(c).get(w).remove(gps);
                    Candidate badCandidate = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1,
                            gps.getTimePoint(), getLikelyhood(distances.get(c).get(gps).get(w),
                                    getLegLength(gps.getTimePoint(), w)), w);
                    wrongCans.add(badCandidate);
                }
            }
        }
        return new Pair<List<Candidate>, List<Candidate>>(newCans, wrongCans);
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
                && distances.get(c).get(fix).get(w) <= distances.get(c).get(fixAfter).get(w)) {
            return true;
        }
        return false;
    }

    private double getLikelyhood(Double distance, Double legLength) {
        return 1 / (500 * Math.abs(distance / legLength) + 1);
    }
}
