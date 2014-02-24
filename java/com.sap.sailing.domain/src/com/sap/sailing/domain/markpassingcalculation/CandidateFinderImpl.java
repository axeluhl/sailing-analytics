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
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;

/**
 * The standard implemantation of {@link CandidateFinder}. There are two ways {@link Candidate}s can be created. First
 * of all, all local distance minima to a waypoint are candidates. Secondly, every time a competitor passes the crossing
 * Bearing of a Waypoint, a candidate is created using linear interpolation to estimate the exact time the bearing was
 * crossed. The probability of a candidate depends on its distance to the waypoint, whether it is on the right side and
 * if it passes in the right direction.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateFinderImpl implements CandidateFinder {

    private static final Logger logger = Logger.getLogger(CandidateFinderImpl.class.getName());
    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Waypoint, Pair<Double, Double>>>> crossTrackErrors = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, TreeMap<GPSFix, LinkedHashMap<Mark, Distance>>> distances = new LinkedHashMap<>();
    private LinkedHashMap<Waypoint, Mark> lineMarks = new LinkedHashMap<>();
    private Set<Mark> marks = new TreeSet<Mark>(new Comparator<Mark>() {
        @Override
        public int compare(Mark o1, Mark o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });
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

    private final double strictness = 9; // Higher = stricter; 9

    public CandidateFinderImpl(DynamicTrackedRace race) {
        this.race = race;
        for (Competitor c : race.getRace().getCompetitors()) {
            crossTrackErrors.put(c, new TreeMap<GPSFix, LinkedHashMap<Waypoint, Pair<Double, Double>>>(comp));
            distances.put(c, new TreeMap<GPSFix, LinkedHashMap<Mark, Distance>>(comp));
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
            if (instruction == PassingInstruction.Line) {
                Mark proxy = new MarkImpl(w.getName());
                lineMarks.put(w, proxy);
                marks.add(proxy);
            } else
                Util.addAll(w.getMarks(), marks);
        }
        Edge.setNumberOfWayoints(Util.size(race.getRace().getCourse().getWaypoints()));
    }

    @Override
    public Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c) {
        Set<GPSFix> fixes = new TreeSet<GPSFix>(comp);
        try {
            race.getTrack(c).lockForRead();
            for (GPSFix fix : race.getTrack(c).getFixes()) {
                fixes.add(fix);
            }
        } finally {
            race.getTrack(c).unlockAfterRead();
        }
        distances.get(c).clear();
        crossTrackErrors.get(c).clear();
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            cteCandidates.get(c).get(w).clear();
            distanceCandidates.get(c).get(w).clear();
        }
        return getCandidateDeltas(c, fixes);
    }

    @Override
    public LinkedHashMap<Competitor, List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps) {
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
    public Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes) {
        Set<Candidate> newCans = new TreeSet<>();
        Set<Candidate> wrongCans = new TreeSet<>();
        LinkedHashMap<Waypoint, Pair<List<Candidate>, List<Candidate>>> distanceCandidates = checkForDistanceCandidateChanges(c, fixes);
        LinkedHashMap<Waypoint, Pair<List<Candidate>, List<Candidate>>> cteCandidates = checkForCTECandidatesChanges(c, fixes);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            newCans.addAll(cteCandidates.get(w).getA());
            newCans.addAll(distanceCandidates.get(w).getA());
            wrongCans.addAll(distanceCandidates.get(w).getB());
            wrongCans.addAll(cteCandidates.get(w).getB());
        }
        if (newCans.size() != 0 || wrongCans.size() != 0) {
            logger.finest(newCans.size() + " new Candidates and " + wrongCans.size() + " removed Candidates for " + c);
        }
        return new Pair<Iterable<Candidate>, Iterable<Candidate>>(newCans, wrongCans);
    }

    private LinkedHashMap<Waypoint, Pair<List<Candidate>, List<Candidate>>> checkForDistanceCandidateChanges(Competitor c, Iterable<GPSFix> fixes) {
        LinkedHashMap<Waypoint, Pair<List<Candidate>, List<Candidate>>> result = new LinkedHashMap<>();
        calculatesDistances(c, fixes);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            result.put(w, new Pair<List<Candidate>, List<Candidate>>(new ArrayList<Candidate>(), new ArrayList<Candidate>()));
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
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                Boolean wasCan = false;
                Boolean isCan = false;
                Candidate oldCan = null;
                Double cost = null;
                boolean portMark = true;
                for (Mark mark : w.getMarks()) {
                    Mark m = null;
                    if (passingInstructions.get(w) == PassingInstruction.Line) {
                        m = lineMarks.get(w);
                    } else {
                        m = mark;
                    }
                    Distance dis = distances.get(c).get(fix).get(m);
                    Distance disBefore = fixBefore != null ? distances.get(c).get(fixBefore).get(m) : null;
                    Distance disAfter = fixAfter != null ? distances.get(c).get(fixAfter).get(m) : null;
                    if (dis == null || disBefore == null || disAfter == null) {
                        continue;
                    }
                    if (disBefore != null && disAfter != null && dis.getMeters() < disBefore.getMeters() && dis.getMeters() < disAfter.getMeters()) {
                        t = fix.getTimePoint();
                        p = fix.getPosition();
                        if (cost != null) { // Candidate for both marks of a gate, the more likely one is chosen
                            Double newCost = getDistanceLikelyhood(w, t, dis);
                            if (newCost == null || newCost * 0.8 < cost) {
                                continue;
                            }
                        }
                        cost = getDistanceLikelyhood(w, t, dis);
                        if (cost == null) {
                            continue;
                        }
                        cost *= 0.8;
                        if (cost > penaltyForSkipping) {
                            isCan = true;
                            if (passingInstructions.get(w) == PassingInstruction.Gate) {
                                Pair<Mark, Mark> pair = race.getPortAndStarboardMarks(t, w);
                                if (m == pair.getB()) {
                                    portMark = false;
                                }
                            }
                        }
                    }
                    if (passingInstructions.get(w) == PassingInstruction.Line) {
                        break;
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
                    Candidate newCan = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t, portMark),
                    /* correct Direction */true, "Distance");
                    distanceCandidates.get(c).get(w).put(fix, newCan);
                    result.get(w).getA().add(newCan);
                } else if (wasCan && !isCan) {
                    distanceCandidates.get(c).get(w).remove(fix);
                    result.get(w).getB().add(oldCan);
                } else if (wasCan && isCan && oldCan.getProbability() != cost) {
                    Candidate newCan = new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t, portMark),
                    /* correct Direction */true, "Distance");
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
            distances.get(c).put(fix, new LinkedHashMap<Mark, Distance>());
            for (Mark m : marks) {
                Distance distance = calculateDistance(fix.getPosition(), m, fix.getTimePoint());
                distances.get(c).get(fix).put(m, distance);
            }
        }
    }

    private LinkedHashMap<Waypoint, Pair<List<Candidate>, List<Candidate>>> checkForCTECandidatesChanges(Competitor c, Iterable<GPSFix> fixes) {
        calculateCrossTrackErrors(c, fixes);
        LinkedHashMap<Waypoint, Pair<List<Candidate>, List<Candidate>>> result = new LinkedHashMap<>();
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            result.put(w, new Pair<List<Candidate>, List<Candidate>>(new ArrayList<Candidate>(), new ArrayList<Candidate>()));
        }
        for (GPSFix fix : fixes) {
            // CAndidate from fix before gets removed and added!!
            GPSFix fixBefore = crossTrackErrors.get(c).lowerKey(fix);
            GPSFix fixAfter = crossTrackErrors.get(c).higherKey(fix);
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
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
                    if (fixAfter != null && crossTrackErrorSignChanges(fix, fixAfter, w, c, true)) {
                        newCandidates.add(Arrays.asList(fix, fixAfter));
                    }
                    if (fixBefore != null && crossTrackErrorSignChanges(fix, fixBefore, w, c, true)) {
                        newCandidates.add(Arrays.asList(fixBefore, fix));
                    }
                    if (fixAfter != null && crossTrackErrorSignChanges(fix, fixAfter, w, c, false)) {
                        newCandidates.add(Arrays.asList(fix, fixAfter, new GPSFixImpl(null, null)));
                    }
                    if (fixBefore != null && crossTrackErrorSignChanges(fix, fixBefore, w, c, false)) {
                        newCandidates.add(Arrays.asList(fixBefore, fix, new GPSFixImpl(null, null)));
                    }
                }
                for (List<GPSFix> canFixes : newCandidates) {
                    Boolean portMark = canFixes.size() != 3;
                    Candidate newCan = createCandidate(c, canFixes.get(0), canFixes.get(1), w, portMark);
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
            TimePoint t = fix.getTimePoint();
            crossTrackErrors.get(c).put(fix, new LinkedHashMap<Waypoint, Pair<Double, Double>>());
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                List<Mark> marks = new ArrayList<Mark>();
                if (w.getControlPoint() instanceof ControlPointWithTwoMarks) {
                    Pair<Mark, Mark> pair = race.getPortAndStarboardMarks(t, w);
                    marks.add(pair.getA());
                    marks.add(pair.getB());
                } else {
                    marks.add(w.getMarks().iterator().next());
                }
                Pair<Double, Double> ctes;
                Position fixPos = fix.getPosition();
                Position markPos = race.getOrCreateTrack(marks.get(0)).getEstimatedPosition(t, false);
                Bearing crossingBearing = race.getCrossingBearing(w, t);
                Double cte1 = null;
                Double cte2 = null;
                if (crossingBearing != null) {
                    cte1 = fixPos.crossTrackError(markPos, crossingBearing).getMeters();
                    if (passingInstructions.get(w) == PassingInstruction.Gate) {
                        cte2 = fixPos.crossTrackError(race.getOrCreateTrack(marks.get(1)).getEstimatedPosition(t, false), crossingBearing).getMeters();

                    }
                }
                ctes = new Pair<Double, Double>(cte1, cte2);
                crossTrackErrors.get(c).get(fix).put(w, ctes);
            }
        }
    }

    private boolean crossTrackErrorSignChanges(GPSFix fix, GPSFix fix2, Waypoint w, Competitor c, boolean portMark) {
        Double cte = portMark ? crossTrackErrors.get(c).get(fix).get(w).getA() : crossTrackErrors.get(c).get(fix).get(w).getB();
        Double cte2 = portMark ? crossTrackErrors.get(c).get(fix2).get(w).getA() : crossTrackErrors.get(c).get(fix2).get(w).getB();
        if (cte != null && cte2 != null && (cte < 0) != (cte2 <= 0)) {
            return true;
        }
        return false;
    }

    private Candidate createCandidate(Competitor c, GPSFix fix1, GPSFix fix2, Waypoint w, Boolean portMark) {
        double cte1;
        double cte2;
        if (portMark) {
            cte1 = crossTrackErrors.get(c).get(fix1).get(w).getA();
            cte2 = crossTrackErrors.get(c).get(fix2).get(w).getA();
        } else {
            cte1 = crossTrackErrors.get(c).get(fix1).get(w).getB();
            cte2 = crossTrackErrors.get(c).get(fix2).get(w).getB();
        }
        TimePoint start = fix1.getTimePoint();
        long differenceInMillis = fix2.getTimePoint().asMillis() - start.asMillis();
        double ratio = (Math.abs(cte1) / (Math.abs(cte1) + Math.abs(cte2)));
        TimePoint t = start.plus((long) (differenceInMillis * ratio));
        Position p = race.getTrack(c).getEstimatedPosition(t, true);
        Mark m = null;
        if (passingInstructions.get(w) == PassingInstruction.Gate) {
            Pair<Mark, Mark> marks = race.getPortAndStarboardMarks(t, w);
            m = portMark ? marks.getA() : marks.getB();
        } else if (passingInstructions.get(w) == PassingInstruction.Line) {
            m = lineMarks.get(w);
        } else {
            m = w.getMarks().iterator().next();
        }
        Distance d = calculateDistance(p, m, t);
        double cost = getDistanceLikelyhood(w, t, d);
        return new Candidate(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t, portMark), passesInTheRightDirection(w, cte1,
                cte2, portMark), "CTE");
    }

    private boolean isOnCorrectSideOfWaypoint(Waypoint w, Position p, TimePoint t, boolean portMark) {
        boolean result = true;
        if (passingInstructions.get(w) == PassingInstruction.Line) {
            List<Position> pos = new ArrayList<>();
            for (Mark m : w.getMarks()) {
                Position po = race.getOrCreateTrack(m).getEstimatedPosition(t, false);
                if (po == null) {
                    return true;
                }
                pos.add(po);
            }
            Bearing diff1 = pos.get(0).getBearingGreatCircle(p).getDifferenceTo(pos.get(0).getBearingGreatCircle(pos.get(1)));
            Bearing diff2 = pos.get(1).getBearingGreatCircle(p).getDifferenceTo(pos.get(1).getBearingGreatCircle(pos.get(0)));
            if (Math.abs(diff1.getDegrees()) > 90 || Math.abs(diff2.getDegrees()) > 90) {
                result = false;
            }

        } else {

            Mark m = null;
            if (passingInstructions.get(w) == PassingInstruction.Port || passingInstructions.get(w) == PassingInstruction.Starboard
                    || passingInstructions.get(w) == PassingInstruction.FixedBearing) {
                m = w.getMarks().iterator().next();
            }
            if (passingInstructions.get(w) == PassingInstruction.Gate) {
                Pair<Mark, Mark> pair = race.getPortAndStarboardMarks(t, w);
                m = portMark ? pair.getA() : pair.getB();
            }
            if (m != null) {
                result = p.crossTrackError(race.getOrCreateTrack(m).getEstimatedPosition(t, true), race.getCrossingBearing(w, t).add(new DegreeBearingImpl(90))).getMeters() < 0;
            }
        }
        return result;
    }

    private boolean passesInTheRightDirection(Waypoint w, double cte1, double cte2, boolean portMark) {
        boolean result = true;
        if (passingInstructions.get(w) == PassingInstruction.Port || (passingInstructions.get(w) == PassingInstruction.Gate && portMark)) {
            result = cte1 > cte2 ? true : false;
        } else if (passingInstructions.get(w) == PassingInstruction.Starboard || (passingInstructions.get(w) == PassingInstruction.Gate && !portMark)) {
            result = cte1 < cte2 ? true : false;
        } else if (passingInstructions.get(w) == PassingInstruction.Line) {
            result = cte1 > cte2 ? true : false;
        }
        return result;
    }

    private Double getDistanceLikelyhood(Waypoint w, TimePoint t, Distance distance) {
        Distance legLength = getLegLength(t, w);
        if (legLength != null) {
            double result = 1 / (strictness * Math.abs(distance.getMeters() / legLength.getMeters()) + 1);
            // Auch NormalVerteilung?
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

    private Distance calculateDistance(Position p, Mark m, TimePoint t) {
        Distance distance = null;
        if (!lineMarks.containsValue(m)) {
            Position markPosition = race.getOrCreateTrack(m).getEstimatedPosition(t, false);
            distance = markPosition != null ? p.getDistance(markPosition) : null;
        } else {
            Waypoint w = null;
            for (Waypoint way : lineMarks.keySet()) {
                if (lineMarks.get(way) == m) {
                    w = way;
                    break;
                }
            }
            Pair<Mark, Mark> pos = race.getPortAndStarboardMarks(t, w);
            Position p1 = race.getOrCreateTrack(pos.getA()).getEstimatedPosition(t, false);
            Position p2 = race.getOrCreateTrack(pos.getB()).getEstimatedPosition(t, false);
            distance = (p1 != null && p2 != null) ? p.getDistanceToLine(p1, p2) : p1 != null ? p.getDistance(p1) : p2 != null ? p.getDistance(p1) : null;
        }
        return distance;
    }
}
