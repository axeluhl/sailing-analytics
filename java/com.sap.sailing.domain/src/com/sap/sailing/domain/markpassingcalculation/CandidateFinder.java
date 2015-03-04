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
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * The standard implemantation of {@link AbstractCandidateFinder}. There are two ways {@link Candidate}s can be created.
 * First of all, all local distance minima to a waypoint are a candidate. Secondly, every time a competitor passes the
 * crossing Bearing of a Waypoint a Candidate is created through linear interpolation. The probability of a Candidate
 * depends on its distance to the waypoint, whether it is on the right side and if it passes in the right direction.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateFinder implements AbstractCandidateFinder {

    private static final Logger logger = Logger.getLogger(CandidateFinder.class.getName());

    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Waypoint, Util.Pair<Double, Double>>>> crossTrackErrors = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Waypoint, Distance>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, LinkedHashMap<List<GPSFix>, Candidate>>> cteCandidates = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, LinkedHashMap<GPSFix, Candidate>>> distanceCandidates = new LinkedHashMap<>();
    private final DynamicTrackedRace race;
    private final double penaltyForSkipping = 1 - Edge.getPenaltyForSkipping();
    private final LinkedHashMap<Waypoint, PassingInstruction> passingInstructions = new LinkedHashMap<>();
    private final Comparator<GPSFix> comp = new Comparator<GPSFix>() {
        @Override
        public int compare(GPSFix arg0, GPSFix arg1) {
            return arg0.getTimePoint().compareTo(arg1.getTimePoint());
        }
    };

    private double strictness = 9; // Higher = stricter; 9

    public CandidateFinder(DynamicTrackedRace race) {
        this.race = race;
        for (Competitor c : race.getRace().getCompetitors()) {
            crossTrackErrors.put(c, new TreeMap<GPSFix, LinkedHashMap<Waypoint, Util.Pair<Double, Double>>>(comp));
            distances.put(c, new TreeMap<GPSFix, LinkedHashMap<Waypoint, Distance>>(comp));
            cteCandidates.put(c, new LinkedHashMap<Waypoint, LinkedHashMap<List<GPSFix>, Candidate>>());
            distanceCandidates.put(c, new LinkedHashMap<Waypoint, LinkedHashMap<GPSFix, Candidate>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                cteCandidates.get(c).put(w, new LinkedHashMap<List<GPSFix>, Candidate>());
                distanceCandidates.get(c).put(w, new LinkedHashMap<GPSFix, Candidate>());
            }
        }
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            PassingInstruction instruction = w.getPassingInstructions();
            if (instruction == PassingInstruction.None || instruction == null) {
                if (w.equals(race.getRace().getCourse().getFirstWaypoint()) || w.equals(race.getRace().getCourse().getLastWaypoint())) {
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
        Edge.setNumberOfWayoints(passingInstructions.keySet().size());
    }

    @Override
    public Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c) {
        Set<GPSFix> fixes = new TreeSet<GPSFix>(comp);
        try {
            race.getTrack(c).lockForRead();
            for (GPSFix fix : race.getTrack(c).getFixes()) {
                fixes.add(fix);
            }
        } finally {
            race.getTrack(c).unlockAfterRead();
        }
        for (Waypoint w : passingInstructions.keySet()) {
            cteCandidates.get(c).get(w).clear();
            distanceCandidates.get(c).get(w).clear();
        }
        return getCandidateDeltas(c, fixes);
    }

    @Override
    public LinkedHashMap<Competitor,List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps) {
        TreeSet<GPSFix> fixes = new TreeSet<GPSFix>(comp);
        for (GPSFix fix : gps) {
            fixes.add(fix);
        }
        LinkedHashMap<Competitor, List<GPSFix>> affectedFixes = new LinkedHashMap<>();
        for (Competitor c : race.getRace().getCompetitors()) {
            affectedFixes.put(c, new ArrayList<GPSFix>());
        }
        for (GPSFix fix : fixes) {
            GPSFix end = fixes.higher(fix);
            for (Competitor c : affectedFixes.keySet()) {
                GPSFix comFix = crossTrackErrors.get(c).ceilingKey(fix);
                if (end != null) {
                    while (comFix != null && comFix.getTimePoint().before(end.getTimePoint())) {
                        affectedFixes.get(c).add(comFix);
                        comFix = crossTrackErrors.get(c).higherKey(comFix);
                    }
                } else {
                    while (comFix != null) {
                        affectedFixes.get(c).add(comFix);
                        comFix = crossTrackErrors.get(c).higherKey(comFix);
                    }
                }
            }
        }
        return affectedFixes;
    }

    @Override
    public Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes) {
        if (logger.getLevel() == Level.FINEST) {
            logger.finest("Reevaluating MarkPasses for" + c);
        }
        Set<Candidate> newCans = new TreeSet<>();
        Set<Candidate> wrongCans = new TreeSet<>();
        LinkedHashMap<Waypoint, Util.Pair<List<Candidate>, List<Candidate>>> distanceCandidates = checkForDistanceCandidateChanges(c, fixes);
        LinkedHashMap<Waypoint, Util.Pair<List<Candidate>, List<Candidate>>> cteCandidates = checkForCTECandidatesChanges(c, fixes);
        for (Waypoint w : passingInstructions.keySet()) {
            newCans.addAll(cteCandidates.get(w).getA());
            newCans.addAll(distanceCandidates.get(w).getA());
            wrongCans.addAll(distanceCandidates.get(w).getB());
            wrongCans.addAll(cteCandidates.get(w).getB());
        }
        if (newCans.size() != 0 || wrongCans.size() != 0) {
            logger.fine(newCans.size() + " new Candidates and " + wrongCans.size() + " removed Candidates for " + c);
        }
        return new Util.Pair<Iterable<Candidate>, Iterable<Candidate>>(newCans, wrongCans);
    }

    private LinkedHashMap<Waypoint, Util.Pair<List<Candidate>, List<Candidate>>> checkForDistanceCandidateChanges(Competitor c, Iterable<GPSFix> fixes) {
        LinkedHashMap<Waypoint, Util.Pair<List<Candidate>, List<Candidate>>> result = new LinkedHashMap<>();
        calculatesDistances(c, fixes);
        for (Waypoint w : passingInstructions.keySet()) {
            result.put(w, new Util.Pair<List<Candidate>, List<Candidate>>(new ArrayList<Candidate>(), new ArrayList<Candidate>()));
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
            TimePoint t = null;
            Position p = null;
            GPSFix fixBefore = distances.get(c).lowerKey(fix);
            GPSFix fixAfter = distances.get(c).higherKey(fix);
            for (Waypoint w : passingInstructions.keySet()) {
                Double cost = null;
                Boolean wasCan = false;
                Candidate oldCan = null;
                Boolean isCan = false;
                Distance dis = distances.get(c).get(fix).get(w);
                Distance disBefore = fixBefore != null ? distances.get(c).get(fixBefore).get(w) : null;
                Distance disAfter = fixAfter != null ? distances.get(c).get(fixAfter).get(w) : null;
                if (dis == null || disBefore == null || disAfter == null) {
                    continue;
                }
                if (disBefore != null && disAfter != null && dis.getMeters() < disBefore.getMeters() && dis.getMeters() < disAfter.getMeters()) {
                    t = fix.getTimePoint();
                    p = fix.getPosition();
                    cost = getDistanceLikelyhood(w, p, t);
                    if (cost == null) {
                        continue;
                    }
                    cost *= 0.9;
                    if (cost > penaltyForSkipping) {
                        isCan = true;
                    }
                }
                for (GPSFix possibleCanFix : distanceCandidates.get(c).get(w).keySet()) {
                    if (possibleCanFix.equals(fix)) {
                        wasCan = true;
                        oldCan = distanceCandidates.get(c).get(w).get(possibleCanFix);
                        break;
                    }
                }
                if (!wasCan && isCan) {
                    Candidate newCan = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t), true, "Distance");
                    distanceCandidates.get(c).get(w).put(fix, newCan);
                    result.get(w).getA().add(newCan);
                } else if (wasCan && !isCan) {
                    distanceCandidates.get(c).get(w).remove(fix);
                    result.get(w).getB().add(oldCan);
                } else if (wasCan && isCan && oldCan.getProbability() != cost) {
                    Candidate newCan = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t), true, "Distance");
                    distanceCandidates.get(c).get(w).put(fix, newCan);
                    result.get(w).getA().add(newCan);
                    result.get(w).getB().add(oldCan);
                }
            }
        }
        return result;
    }

    private void calculatesDistances(Competitor c, Iterable<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            distances.get(c).put(fix, new LinkedHashMap<Waypoint, Distance>());
            for (Waypoint w : passingInstructions.keySet()) {
                Distance distance = calculateDistance(fix.getPosition(), w, fix.getTimePoint());
                distances.get(c).get(fix).put(w, distance);
            }
        }
    }

    private LinkedHashMap<Waypoint, Util.Pair<List<Candidate>, List<Candidate>>> checkForCTECandidatesChanges(Competitor c, Iterable<GPSFix> fixes) {
        calculateCrossTrackErrors(c, fixes);
        LinkedHashMap<Waypoint, Util.Pair<List<Candidate>, List<Candidate>>> result = new LinkedHashMap<>();
        for (Waypoint w : passingInstructions.keySet()) {
            result.put(w, new Util.Pair<List<Candidate>, List<Candidate>>(new ArrayList<Candidate>(), new ArrayList<Candidate>()));
        }
        for (GPSFix fix : fixes) {
            GPSFix fixBefore = crossTrackErrors.get(c).lowerKey(fix);
            GPSFix fixAfter = crossTrackErrors.get(c).higherKey(fix);
            for (Waypoint w : passingInstructions.keySet()) {
                List<List<GPSFix>> oldCandidates = new ArrayList<>();
                Set<List<GPSFix>> newCandidates = new TreeSet<List<GPSFix>>(new Comparator<List<GPSFix>>() {
                    @Override
                    public int compare(List<GPSFix> arg0, List<GPSFix> arg1) {
                        int firstFix = arg0.get(0).getTimePoint().compareTo(arg1.get(0).getTimePoint());
                        return firstFix != 0 ? firstFix : arg0.get(1).getTimePoint().compareTo(arg1.get(1).getTimePoint());
                    }
                });
                for (List<GPSFix> fixPair : cteCandidates.get(c).get(w).keySet()) {
                    if (fixPair.contains(fix)) {
                        oldCandidates.add(fixPair);
                    }
                }
                if ((crossTrackErrors.get(c).get(fix).get(w).getA() != null && crossTrackErrors.get(c).get(fix).get(w).getA() == 0)
                        || (crossTrackErrors.get(c).get(fix).get(w).getB() != null && crossTrackErrors.get(c).get(fix).get(w).getB() == 0)) {
                    newCandidates.add(Arrays.asList(fix, fix));
                } else {
                    if (fixAfter != null && crossTrackErrorSignChanges(fix, fixAfter, w, c)) {
                        newCandidates.add(Arrays.asList(fix, fixAfter));
                    }
                    if (fixBefore != null && crossTrackErrorSignChanges(fix, fixBefore, w, c)) {
                        newCandidates.add(Arrays.asList(fixBefore, fix));
                    }
                }
                for (List<GPSFix> canFixes : newCandidates) {
                    Candidate newCan = createCandidate(c, canFixes.get(0), canFixes.get(1), w);
                    if (oldCandidates.contains(canFixes)) {
                        oldCandidates.remove(canFixes);
                        if (newCan.getProbability() != cteCandidates.get(c).get(w).get(canFixes).getProbability()) {
                            result.get(w).getB().add(cteCandidates.get(c).get(w).get(canFixes));
                            cteCandidates.get(c).get(w).remove(canFixes);
                            if (newCan.getProbability() > penaltyForSkipping) {
                                result.get(w).getA().add(newCan);
                                cteCandidates.get(c).get(w).put(canFixes, newCan);
                            }
                        }
                    } else {
                        if (newCan.getProbability() > penaltyForSkipping) {
                            result.get(w).getA().add(newCan);
                            cteCandidates.get(c).get(w).put(canFixes, newCan);
                        }
                    }
                }
                for (List<GPSFix> badCanFixes : oldCandidates) {
                    result.get(w).getB().add(cteCandidates.get(c).get(w).get(badCanFixes));
                    cteCandidates.get(c).get(w).remove(badCanFixes);
                }
            }
        }
        return result;
    }

    private void calculateCrossTrackErrors(Competitor c, Iterable<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            crossTrackErrors.get(c).put(fix, new LinkedHashMap<Waypoint, Util.Pair<Double, Double>>());
            for (Waypoint w : passingInstructions.keySet()) {
                Iterator<Mark> it = w.getMarks().iterator();
                List<Mark> marks = new ArrayList<Mark>();
                while (it.hasNext()) {
                    marks.add(it.next());
                }
                Util.Pair<Double, Double> ctes;
                Position fixPos = fix.getPosition();
                Position markPos = race.getOrCreateTrack(marks.get(0)).getEstimatedPosition(fix.getTimePoint(), false);
                Bearing crossingBearing = race.getCrossingBearing(w, fix.getTimePoint());
                Double cte1 = null;
                Double cte2 = null;
                if (crossingBearing != null) {
                    cte1 = fixPos.crossTrackError(markPos, crossingBearing).getMeters();
                    if (passingInstructions.get(w) == PassingInstruction.Gate) {
                        cte2 = fixPos.crossTrackError(race.getOrCreateTrack(marks.get(1)).getEstimatedPosition(fix.getTimePoint(), false), crossingBearing).getMeters();

                    }
                }
                ctes = new Util.Pair<Double, Double>(cte1, cte2);
                crossTrackErrors.get(c).get(fix).put(w, ctes);
            }
        }
    }

    private boolean crossTrackErrorSignChanges(GPSFix fix, GPSFix fix2, Waypoint w, Competitor c) {
        Double cte = crossTrackErrors.get(c).get(fix).get(w).getA();
        Double cte2 = crossTrackErrors.get(c).get(fix2).get(w).getA();
        if (cte!=null&&cte2!=null&&( cte< 0) != (cte2<= 0)) {
            return true;
        } else if (passingInstructions.get(w) == PassingInstruction.Gate){
            cte = crossTrackErrors.get(c).get(fix).get(w).getB();
            cte2 = crossTrackErrors.get(c).get(fix2).get(w).getB();
            if (cte!=null&&cte2!=null&&( cte< 0) != (cte2<= 0)) {
                return true;
            } 
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
        double cost = getDistanceLikelyhood(w, p, t);
        return new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t), passesInTheRightDirection(w, cte1, cte2), "CTE");
    }

    private boolean isOnCorrectSideOfWaypoint(Waypoint w, Position p, TimePoint t) {
        boolean result = true;
        if (passingInstructions.get(w) == PassingInstruction.Port || passingInstructions.get(w) == PassingInstruction.Starboard
                || passingInstructions.get(w) == PassingInstruction.FixedBearing) {
            final Position estimatedMarkPosition = race.getOrCreateTrack(w.getMarks().iterator().next()).getEstimatedPosition(t, true);
            if (estimatedMarkPosition != null) {
                result = p.crossTrackError(estimatedMarkPosition,
                        race.getCrossingBearing(w, t).add(new DegreeBearingImpl(90))).getMeters() < 0;
            } else {
                // TODO
            }
        } else if (passingInstructions.get(w) == PassingInstruction.Gate) {
            // TODO
        } else if (passingInstructions.get(w) == PassingInstruction.Offset) {
            // TODO
        }
        return result;
    }

    private boolean passesInTheRightDirection(Waypoint w, double cte1, double cte2) {
        boolean result = true;
        if (passingInstructions.get(w) == PassingInstruction.Port) {
            result = cte1 > cte2 ? true : false;
        } else if (passingInstructions.get(w) == PassingInstruction.Starboard) {
            result = cte1 < cte2 ? true : false;
        } else if (passingInstructions.get(w) == PassingInstruction.Line) {
            result = cte1 > cte2 ? true : false;
        } else if (passingInstructions.get(w) == PassingInstruction.Gate) {
            // TODO
        } else if (passingInstructions.get(w) == PassingInstruction.Offset) {
            // TODO
        } else if (passingInstructions.get(w) == PassingInstruction.FixedBearing) {
            // TODO
        }
        if (result == false) {
            System.currentTimeMillis();
        }
        return result;
    }

    private Double getDistanceLikelyhood(Waypoint w, Position p, TimePoint t) {
        Distance distance = calculateDistance(p, w, t);
        Distance legLength = getLegLength(t, w);
        if (legLength != null) {
            double result = 1 / (strictness * Math.abs(distance.getMeters() / legLength.getMeters()) + 1);
            // Auch NormalVerteilung??!
            return result;
        }
        return null;
    }

    private Distance getLegLength(TimePoint t, Waypoint w) {
        if (w == race.getRace().getCourse().getFirstWaypoint()) {
            return race.getTrackedLegStartingAt(w).getGreatCircleDistance(t);
        } else if (w == race.getRace().getCourse().getLastWaypoint()) {
            return race.getTrackedLegFinishingAt(w).getGreatCircleDistance(t);
        } else {
            Distance before = race.getTrackedLegStartingAt(w).getGreatCircleDistance(t);
            Distance after = race.getTrackedLegFinishingAt(w).getGreatCircleDistance(t);
            if (after != null && before != null) {
                return new MeterDistance(before.add(after).getMeters() / 2);
            }
            return null;
        }
    }

    private Distance calculateDistance(Position p, Waypoint w, TimePoint t) {
        Distance distance = null;
        PassingInstruction instruction = passingInstructions.get(w);
        Position p1 = null;
        Position p2 = null;

        if (instruction == PassingInstruction.Line) {
            Util.Pair<Mark, Mark> pos = race.getPortAndStarboardMarks(t, w);
            p1 = pos.getA() == null ? null : race.getOrCreateTrack(pos.getA()).getEstimatedPosition(t, false);
            p2 = pos.getB() == null ? null : race.getOrCreateTrack(pos.getB()).getEstimatedPosition(t, false);
        } else {
            int i = 1;
            for (Mark m : w.getMarks()) {
                Position markPos = race.getOrCreateTrack(m).getEstimatedPosition(t, false);
                if (i == 1) {
                    p1 = markPos;
                } else {
                    p2 = markPos;
                }
                i++;
            }
        }
        if (instruction.equals(PassingInstruction.Port) || instruction.equals(PassingInstruction.Starboard) || instruction.equals(PassingInstruction.Offset)) {
            distance = p1 != null ? p.getDistance(p1) : null;
        }
        if (instruction.equals(PassingInstruction.Line)) {
            distance = (p1 != null && p2 != null) ? p.getDistanceToLine(p1, p2) : p1 != null ? p.getDistance(p1) : p2 != null ? p.getDistance(p1) : null;
        }
        if (instruction.equals(PassingInstruction.Gate)) {
            Distance d1 = p1 != null ? p.getDistance(p1) : null;
            Distance d2 = p2 != null ? p.getDistance(p2) : null;
            distance = d1 != null && d2 != null ? (d1.getMeters() < d2.getMeters() ? d1 : d2) : d1 == null ? d2 : d1;
        }
        return distance;
    }
}
