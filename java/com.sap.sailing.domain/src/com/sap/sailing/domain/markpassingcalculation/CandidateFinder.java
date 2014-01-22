package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;

/**
 * The standard implemantation of {@link AbstractCandidateFinder}. The fixes are evaluated for their distance to each
 * Waypoint. Every local minimum is a Candidate, and its probability depends on its ratio to the average lengths of the
 * {@link Leg}s before and after it.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateFinder implements AbstractCandidateFinder {

    private static final Logger logger = Logger.getLogger(CandidateFinder.class.getName());


    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Waypoint, Double>>> crossTrackErrors = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, List<Pair<GPSFix, GPSFix>>>> candidates = new LinkedHashMap<>();
    private DynamicTrackedRace race;
    private double penaltyForSkipping = 1 - Edge.penaltyForSkipped;

    // Calculate for some Waypoints instead of all?

    public CandidateFinder(DynamicTrackedRace race) {
        this.race = race;
        for (Competitor c : race.getRace().getCompetitors()) {
            crossTrackErrors.put(c, new TreeMap<GPSFix, LinkedHashMap<Waypoint, Double>>(new Comparator<GPSFix>() {
                @Override
                public int compare(GPSFix arg0, GPSFix arg1) {
                    return arg0.getTimePoint().compareTo(arg1.getTimePoint());
                }

            }));
            candidates.put(c, new LinkedHashMap<Waypoint, List<Pair<GPSFix, GPSFix>>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                candidates.get(c).put(w, new ArrayList<Pair<GPSFix, GPSFix>>());
            }
        }
    }

    @Override
    public Pair<List<Candidate>, List<Candidate>> getAllCandidates(Competitor c) {
        List<GPSFix> fixes = new ArrayList<GPSFix>();
        try {
            race.getTrack(c).lockForRead();
            for (GPSFix fix : race.getTrack(c).getFixes()) {
                fixes.add(fix);
            }
        } finally {
            race.getTrack(c).unlockAfterRead();
        }
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            candidates.get(c).get(w).clear();
        }
        return getCandidateDeltas(c, fixes);
    }

    @Override
    public void calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps) {
        // TODO !!!
        TreeSet<GPSFix> fixes = new TreeSet<GPSFix>(new Comparator<GPSFix>() {
            @Override
            public int compare(GPSFix o1, GPSFix o2) {
                return o1.getTimePoint().compareTo(o2.getTimePoint());
            }
        });
        for (GPSFix fix : gps) {
            fixes.add(fix);
        }
        TimePoint start = fixes.first().getTimePoint();
        GPSFix nextMarkFixAfterLastGivenFix = race.getOrCreateTrack(mark).getFirstFixAfter(fixes.last().getTimePoint());
        TimePoint end = null;
        if (!(nextMarkFixAfterLastGivenFix == null)) {
            end = race.getOrCreateTrack(mark).getFirstFixAfter(start).getTimePoint();
        }
    
        for (Competitor c : crossTrackErrors.keySet()) {
            TimePoint t = start;
            while (race.getTrack(c).getFirstFixAfter(t) != null) {
                GPSFix nextFix = race.getTrack(c).getFirstFixAfter(t);
                t = nextFix.getTimePoint();
                if (end != null && !t.before(end)) {
                    break;
                }
                newCandidates.get(c).add(nextFix);
            }
            calculateCrossTrackErrors(c);
        }
    }

    @Override
    public Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(Competitor c, List<GPSFix> fixes) {
        if (logger.getLevel() == Level.FINEST) {
            logger.finest("Reevaluating " + fixes.size() + " fixes for" + c);
        }

        ArrayList<Candidate> newCans = new ArrayList<>();
        ArrayList<Candidate> wrongCans = new ArrayList<>();

        LinkedHashMap<Waypoint, List<Pair<GPSFix, GPSFix>>> pairs = calculateCTEAndCheckForChanges(c, fixes);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            for (Pair<GPSFix, GPSFix> pair : pairs.get(c)) {
                double cte1 = crossTrackErrors.get(c).get(pair.getA()).get(w);
                double cte2 = crossTrackErrors.get(c).get(pair.getB()).get(w);
                TimePoint t = pair.getA().getTimePoint().plus(pair.getB().getTimePoint()
                        .minus(pair.getA().getTimePoint().asMillis()).asMillis()
                        * (long) (cte1 / (cte1 + cte2)));
                Position p = race.getTrack(c).getEstimatedPosition(t, true);
                newCans.add(new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, getLikelyhood(w, p,
                        t), w));
            }
        }

        // TODO Removal of candidates!!
        logger.fine(newCans.size() + " new Candidates and " + wrongCans.size() + " removed Candidates for " + c);
        return new Pair<List<Candidate>, List<Candidate>>(newCans, wrongCans);
    }

    private LinkedHashMap<Waypoint, List<Pair<GPSFix, GPSFix>>> calculateCTEAndCheckForChanges(Competitor c, Iterable<GPSFix> fixes) {
        //TODO first part of pair is always first in time
        calculateCrossTrackErrors(c, fixes);
        LinkedHashMap<Waypoint, List<Pair<GPSFix, GPSFix>>> newCandidates = new LinkedHashMap<>();
        for (GPSFix fix : fixes) {
            GPSFix fixBefore = crossTrackErrors.get(c).higherKey(fix);
            GPSFix fixAfter = crossTrackErrors.get(c).lowerKey(fix);
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                if (crossTrackErrors.get(c).get(fix).get(w) == 0) {
                    newCandidates.get(w).add(new Pair<GPSFix, GPSFix>(fix, fix));
                } else {
                    if (fixBefore != null && crossTrackErrorSignChanges(fix, fixBefore, w, c)
                            && !newCandidates.get(w).contains(new Pair<GPSFix, GPSFix>(fix, fixBefore))) {
                        newCandidates.get(w).add(new Pair<GPSFix, GPSFix>(fixBefore, fix));
                    }
                    if (fixAfter != null && crossTrackErrorSignChanges(fix, fixAfter, w, c)&& !newCandidates.get(w).contains(new Pair<GPSFix, GPSFix>(fixAfter, fix))) {
                        newCandidates.get(w).add(new Pair<GPSFix, GPSFix>(fix, fixAfter));
                    }
                }
            }
        }
        return newCandidates;
    }

    private void calculateCrossTrackErrors(Competitor c, Iterable<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            crossTrackErrors.get(c).put(fix, new LinkedHashMap<Waypoint, Double>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                double crossTrackError = fix
                        .getPosition()
                        .crossTrackError(
                                race.getOrCreateTrack(w.getMarks().iterator().next()).getEstimatedPosition(
                                        fix.getTimePoint(), true), race.getCrossingBearing(w, fix.getTimePoint()))
                        .getMeters();
                crossTrackErrors.get(c).get(fix).put(w, crossTrackError);
            }
        }
    }

    private double calculateDistance(Position p, Waypoint w, TimePoint t) {
        double distance = 0;
        PassingInstruction instruction = w.getPassingInstructions();
        if (instruction == null) {
            if (w.equals(race.getRace().getCourse().getFirstWaypoint())
                    || w.equals(race.getRace().getCourse().getLastWaypoint())) {
                instruction = PassingInstruction.Line;
            } else {
                int numberofMarks = 0;
                Iterator<Mark> it = w.getMarks().iterator();
                while (it.hasNext()) {
                    it.next();
                    numberofMarks++;
                }
                if (numberofMarks == 2) {
                    instruction = PassingInstruction.Gate;
                } else if (numberofMarks == 1) {
                    instruction = PassingInstruction.Port;
                } else {
                    instruction = PassingInstruction.None;
                }
            }
        }
        ArrayList<Position> positions = new ArrayList<>();
        for (Mark m : w.getMarks()) {
            positions.add(race.getOrCreateTrack(m).getEstimatedPosition(t, false));
        }
        // TODO Handle Offset correctly: MarkPassing for before with Markpassing for offset
        if (instruction.equals(PassingInstruction.Port) || instruction.equals(PassingInstruction.Starboard)
                || instruction.equals(PassingInstruction.Offset) || instruction.equals(PassingInstruction.None)) {
            distance = p.getDistance(positions.get(0)).getMeters();
        }
        if (instruction.equals(PassingInstruction.Line)) {
            distance = p.getDistanceToLine(positions.get(0), positions.get(1)).getMeters();
            if (race.getRace().getCourse().getIndexOfWaypoint(w) == 0) {
            }
        }
        if (instruction.equals(PassingInstruction.Gate)) {
            if (p.getDistance(positions.get(0)).getMeters() < p.getDistance(positions.get(1)).getMeters()) {
                distance = p.getDistance(positions.get(0)).getMeters();
            } else {
                distance = p.getDistance(positions.get(1)).getMeters();
            }
        }
        return distance;
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

    private boolean crossTrackErrorSignChanges(GPSFix fix, GPSFix fix2, Waypoint w, Competitor c) {
        //TODO CrossTrackError = 0
        if ((crossTrackErrors.get(c).get(w).get(fix) < 0) != (crossTrackErrors.get(c).get(w).get(fix2) <= 0)) {
            return true;
        }
        return false;
    }

    private double getLikelyhood(Waypoint w, Position p, TimePoint t) {
        return 1 / (100 * Math.abs(calculateDistance(p, w, t) / getLegLength(t, w)) + 1);
    }
}
