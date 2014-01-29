package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
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

    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Waypoint, Pair<Double, Double>>>> crossTrackErrors = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Waypoint, Double>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, LinkedHashMap<List<GPSFix>, Candidate>>> candidates = new LinkedHashMap<>();
    private DynamicTrackedRace race;
    private double penaltyForSkipping = 1 - Edge.penaltyForSkipped;
    LinkedHashMap<Waypoint, PassingInstruction> passingInstructions = new LinkedHashMap<>();
    Comparator<GPSFix> comp = new Comparator<GPSFix>() {
        @Override
        public int compare(GPSFix arg0, GPSFix arg1) {
            return arg0.getTimePoint().compareTo(arg1.getTimePoint());
        }
    };

    // Calculate for some Waypoints instead of all?

    public CandidateFinder(DynamicTrackedRace race) {
        this.race = race;

        for (Competitor c : race.getRace().getCompetitors()) {
            crossTrackErrors.put(c, new TreeMap<GPSFix, LinkedHashMap<Waypoint, Pair<Double, Double>>>(comp));
            distances.put(c, new TreeMap<GPSFix, LinkedHashMap<Waypoint, Double>>(comp));
            candidates.put(c, new LinkedHashMap<Waypoint, LinkedHashMap<List<GPSFix>, Candidate>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                candidates.get(c).put(w, new LinkedHashMap<List<GPSFix>, Candidate>());
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
                passingInstructions.put(w, instruction);
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

    // TODO Live!!
    @Override
    public void calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps) {
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
            List<GPSFix> comFixes = new ArrayList<>();
            TimePoint t = start;
            while (race.getTrack(c).getFirstFixAfter(t) != null) {
                GPSFix nextFix = race.getTrack(c).getFirstFixAfter(t);
                t = nextFix.getTimePoint();
                if (end != null && !t.before(end)) {
                    break;
                }
                comFixes.add(nextFix);
            }
            calculateCrossTrackErrors(c, comFixes);
        }

    }

    @Override
    public Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(Competitor c, List<GPSFix> fixes) {
        if (logger.getLevel() == Level.FINEST) {
            logger.finest("Reevaluating " + fixes.size() + " fixes for" + c);
        }
        ArrayList<Candidate> newCans = new ArrayList<>();
        ArrayList<Candidate> wrongCans = new ArrayList<>();
        LinkedHashMap<Waypoint,Pair<List<Pair<GPSFix,GPSFix>>,List<Candidate>>> cteCandidates = checkForCTECandidatesChanges(c, fixes);
        LinkedHashMap<Waypoint, Pair<List<GPSFix>, List<Candidate>>> distanceCandidates = checkForDistanceCandidateChanges(
                c, fixes);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            for (Pair<GPSFix, GPSFix> pair : cteCandidates.get(w).getA()) {
                Candidate ca = createCandidate(c, pair.getA(), pair.getB(), w);
                if (ca.getProbability() > penaltyForSkipping) {
                    candidates.get(c).get(w).put(Arrays.asList(pair.getA(), pair.getB()), ca);
                    newCans.add(ca);
                }
            }
            for (Candidate can : cteCandidates.get(w).getB()){
                wrongCans.add(can);
            }
            for (GPSFix fix : distanceCandidates.get(w).getA()) {
                TimePoint t = fix.getTimePoint();
                Position p = fix.getPosition();
                Candidate ca = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t,
                        getDistanceLikelyhood(w, p, t) * isOnCorrectSideOfWaypoint(w, p, t) * 0.85, w);
                if (ca.getProbability() > penaltyForSkipping) {
                    candidates.get(c).get(w).put(Arrays.asList(fix), ca);
                    newCans.add(ca);
                }
            }
            wrongCans.addAll(distanceCandidates.get(w).getB());
        }
        logger.fine(newCans.size() + " new Candidates and " + wrongCans.size() + " removed Candidates for " + c);
        return new Pair<List<Candidate>, List<Candidate>>(newCans, wrongCans);
    }

    private LinkedHashMap<Waypoint, Pair<List<GPSFix>, List<Candidate>>> checkForDistanceCandidateChanges(Competitor c,
            List<GPSFix> fixes) {
        LinkedHashMap<Waypoint, Pair<List<GPSFix>, List<Candidate>>> result = new LinkedHashMap<>();
        calculatesDistances(c, fixes);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            result.put(w, new Pair<List<GPSFix>, List<Candidate>>(new ArrayList<GPSFix>(), new ArrayList<Candidate>()));
        }
        Set<GPSFix> affectedFixes = new TreeSet<GPSFix>(comp);
        for (GPSFix fix : fixes) {
            affectedFixes.add(fix);
            GPSFix fixBefore = distances.get(c).lowerKey(fix);
            GPSFix fixAfter = distances.get(c).higherKey(fix);
            if (fixBefore != null) {
                affectedFixes.add(fixBefore);
            }
            if (fixAfter != null) {
                affectedFixes.add(fixAfter);
            }
        }
        for (GPSFix fix : affectedFixes) {
            GPSFix fixBefore = distances.get(c).lowerKey(fix);
            GPSFix fixAfter = distances.get(c).higherKey(fix);
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                Boolean wasCan = false;
                Candidate oldCan = null;
                Boolean isCan = false;
                for (List<GPSFix> canPair : candidates.get(c).get(w).keySet()) {
                    if (canPair.size() == 1 && canPair.get(0).equals(fix)) {
                        wasCan = true;
                        oldCan = candidates.get(c).get(w).get(canPair);
                    }
                }
                Double dis1 = fixBefore != null ? distances.get(c).get(fixBefore).get(w) : null;
                Double dis2 = distances.get(c).get(fix).get(w);
                Double dis3 = fixAfter != null ? distances.get(c).get(fixAfter).get(w) : null;
                if (dis1 != null && dis3 != null && dis2 < dis1 && dis2 < dis3) {
                    isCan = true;
                }
                if (!wasCan && isCan) {
                    result.get(w).getA().add(fix);
                } else if (wasCan && !isCan) {
                    result.get(w).getB().add(oldCan);
                }
            }
        }
        return result;
    }

    private void calculatesDistances(Competitor c, List<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            distances.get(c).put(fix, new LinkedHashMap<Waypoint, Double>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                double distance = calculateDistance(fix.getPosition(), w, fix.getTimePoint());
                distances.get(c).get(fix).put(w, distance);
            }
        }
    }

    private LinkedHashMap<Waypoint, Pair<List<Pair<GPSFix, GPSFix>>, List<Candidate>>> checkForCTECandidatesChanges(
            Competitor c, Iterable<GPSFix> fixes) {
        calculateCrossTrackErrors(c, fixes);
        LinkedHashMap<Waypoint, Pair<List<Pair<GPSFix, GPSFix>>, List<Candidate>>> result = new LinkedHashMap<>();
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            result.put(w, new Pair<List<Pair<GPSFix, GPSFix>>, List<Candidate>>(
                    new ArrayList<Pair<GPSFix, GPSFix>>(), new ArrayList<Candidate>()));
        }

        for (GPSFix fix : fixes) {
            GPSFix fixBefore = distances.get(c).lowerKey(fix);
            GPSFix fixAfter = crossTrackErrors.get(c).higherKey(fix);
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                boolean changed = false;
                if (crossTrackErrors.get(c).get(fix).get(w).getA() == 0
                        || (crossTrackErrors.get(c).get(fix).get(w).getB() != null && crossTrackErrors.get(c).get(fix)
                                .get(w).getB() == 0)) {
                    result.get(w).getA().add(new Pair<GPSFix, GPSFix>(fix, fix));
                    changed = true;
                } else {
                    if (fixAfter != null && crossTrackErrorSignChanges(fix, fixAfter, w, c)) {
                        result.get(w).getA().add(new Pair<GPSFix, GPSFix>(fix, fixAfter));
                        changed = true;
                    }
                    if (fixBefore != null && crossTrackErrorSignChanges(fix, fixBefore, w, c)) {
                        result.get(w).getA().add(new Pair<GPSFix, GPSFix>(fixBefore, fix));
                        changed = true;
                    }
                }
                if (changed) {
                    for (List<GPSFix> can : candidates.get(c).get(w).keySet()){
                        if (can.contains(fixBefore)&&can.contains(fixAfter)){
                            result.get(w).getB().add(candidates.get(c).get(w).get(can));
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private void calculateCrossTrackErrors(Competitor c, Iterable<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            crossTrackErrors.get(c).put(fix, new LinkedHashMap<Waypoint, Pair<Double, Double>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                Iterator<Mark> it = w.getMarks().iterator();
                List<Mark> marks = new ArrayList<Mark>();
                while (it.hasNext()) {
                    marks.add(it.next());
                }
                Pair<Double, Double> ctes;
                Position fixPos = fix.getPosition();
                Position markPos = race.getOrCreateTrack(marks.get(0)).getEstimatedPosition(fix.getTimePoint(), true);
                Bearing crossingBearing = race.getCrossingBearing(w, fix.getTimePoint());
                double cte1 = fixPos.crossTrackError(markPos, crossingBearing).getMeters();
                ctes = new Pair<Double, Double>(cte1, null);
                if (passingInstructions.get(w) == PassingInstruction.Gate) {
                    double cte2 = fix
                            .getPosition()
                            .crossTrackError(
                                    race.getOrCreateTrack(marks.get(1)).getEstimatedPosition(fix.getTimePoint(), true),
                                    race.getCrossingBearing(w, fix.getTimePoint())).getMeters();
                    ctes = new Pair<Double, Double>(cte1, cte2);
                }
                crossTrackErrors.get(c).get(fix).put(w, ctes);
            }
        }
    }

    private boolean crossTrackErrorSignChanges(GPSFix fix, GPSFix fix2, Waypoint w, Competitor c) {
        if ((crossTrackErrors.get(c).get(fix).get(w).getA() < 0) != (crossTrackErrors.get(c).get(fix2).get(w).getA() <= 0)) {
            return true;
        } else if (passingInstructions.get(w) == PassingInstruction.Gate
                && (crossTrackErrors.get(c).get(fix).get(w).getB() < 0) != (crossTrackErrors.get(c).get(fix2).get(w)
                        .getB() <= 0)) {
            return true;
        }
        return false;
    }

    private Candidate createCandidate(Competitor c, GPSFix fix1, GPSFix fix2, Waypoint w) {

        double cte1 = crossTrackErrors.get(c).get(fix1).get(w).getA();
        double cte2 = crossTrackErrors.get(c).get(fix2).get(w).getA();
        if ((cte1 < 0 && cte2 < 0) || (cte1 > 0 && cte2 > 0)) {
            cte1 = crossTrackErrors.get(c).get(fix1).get(w).getB();
            cte2 = crossTrackErrors.get(c).get(fix2).get(w).getB();
        }
        TimePoint start = fix1.getTimePoint();
        long differenceInMillis = fix2.getTimePoint().asMillis() - fix1.getTimePoint().asMillis();
        double ratio = (Math.abs(cte1) / (Math.abs(cte1) + Math.abs(cte2)));
        TimePoint t = start.plus((long) (differenceInMillis * ratio));
        Position p = race.getTrack(c).getEstimatedPosition(t, true);
        double cost = getDistanceLikelyhood(w, p, t) * isOnCorrectSideOfWaypoint(w, p, t)
                * passesInTheRightDirection(w, cte1, cte2);

        return new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w);
    }

    private double isOnCorrectSideOfWaypoint(Waypoint w, Position p, TimePoint t) {
        if (w.getPassingInstructions() == PassingInstruction.Port
                || w.getPassingInstructions() == PassingInstruction.Starboard
                || w.getPassingInstructions() == PassingInstruction.FixedBearing) {
            return p.crossTrackError(
                    race.getOrCreateTrack(w.getMarks().iterator().next()).getEstimatedPosition(t, true),
                    race.getCrossingBearing(w, t).add(new DegreeBearingImpl(90))).getMeters() < 0 ? 1 : 0.5;

        } else if (w.getPassingInstructions() == PassingInstruction.Gate) {
            // TODO
        } else if (w.getPassingInstructions() == PassingInstruction.Line) {
            // TODO
        } else if (w.getPassingInstructions() == PassingInstruction.Offset) {
            // TODO
        }
        return 1;
    }

    private double passesInTheRightDirection(Waypoint w, double cte1, double cte2) {
        if (w.getPassingInstructions() == PassingInstruction.Port) {
            return cte1 > cte2 ? 1 : 0.5;
        }
        if (w.getPassingInstructions() == PassingInstruction.Starboard) {
            return cte1 < cte2 ? 1 : 0.5;
        } else if (w.getPassingInstructions() == PassingInstruction.Gate) {
            // TODO
        } else if (w.getPassingInstructions() == PassingInstruction.Line) {
            // TODO
        } else if (w.getPassingInstructions() == PassingInstruction.Offset) {
            // TODO
        } else if (w.getPassingInstructions() == PassingInstruction.FixedBearing) {
            // TODO
        }
        return 1;
    }

    private double getDistanceLikelyhood(Waypoint w, Position p, TimePoint t) {
        return 1 / (50 * Math.abs(calculateDistance(p, w, t) / getLegLength(t, w)) + 1);
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

    private double calculateDistance(Position p, Waypoint w, TimePoint t) {
        double distance = 0;
        PassingInstruction instruction = passingInstructions.get(w);
        ArrayList<Position> positions = new ArrayList<>();
        for (Mark m : w.getMarks()) {
            positions.add(race.getOrCreateTrack(m).getEstimatedPosition(t, false));
        }
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
}
