package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
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
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

/**
 * The standard implemantation of {@link CandidateFinder}. There are two kinds of {@link Candidate}s. First of all,
 * every time a competitor passes the crossing-bearing of a waypoint, a candidate is created using linear interpolation
 * to estimate the exact time the bearing was crossed. Secondly, all local distance minima to a waypoint are candidates.
 * The probability of a candidate depends on its distance , whether it is on the right side and if it passes in the
 * right direction of its waypoint.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateFinderImpl implements CandidateFinder {
    // TODO Datenstrukturen beschreiben
    private static final Logger logger = Logger.getLogger(CandidateFinderImpl.class.getName());
    private Map<Competitor, TreeMap<GPSFix, Map<Waypoint, List<Double>>>> crossTrackErrors = new HashMap<>();
    private Map<Competitor, TreeMap<GPSFix, Map<Mark, Distance>>> distances = new HashMap<>();
    // Distance are calculated to each mark. Since the distance to a line relies on both marks, these line marks are
    // used to represent them.
    private Map<Waypoint, Mark> lineMarks = new HashMap<>();
    private Set<Mark> marks = new TreeSet<Mark>(new Comparator<Mark>() {
        @Override
        public int compare(Mark o1, Mark o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });
    private Map<Competitor, Map<Waypoint, Map<List<GPSFix>, Candidate>>> cteCandidates = new HashMap<>();
    private Map<Competitor, Map<Waypoint, Map<GPSFix, Candidate>>> distanceCandidates = new HashMap<>();
    private final DynamicTrackedRace race;
    private final double penaltyForSkipping = 1 - Edge.getPenaltyForSkipping();
    private final Map<Waypoint, PassingInstruction> passingInstructions = new HashMap<>();
    private final Comparator<GPSFix> comp = new Comparator<GPSFix>() {
        @Override
        public int compare(GPSFix arg0, GPSFix arg1) {
            return arg0.getTimePoint().compareTo(arg1.getTimePoint());
        }
    };

    public CandidateFinderImpl(DynamicTrackedRace race) {
        this.race = race;
        RaceDefinition raceDefinition = race.getRace();
        Course course = raceDefinition.getCourse();
        Iterable<Waypoint> waypoints = course.getWaypoints();
        Waypoint firstWaypoint = course.getFirstWaypoint();
        Waypoint lastWaypoint = course.getLastWaypoint();
        for (Competitor c : raceDefinition.getCompetitors()) {
            crossTrackErrors.put(c, new TreeMap<GPSFix, Map<Waypoint, List<Double>>>(comp));
            distances.put(c, new TreeMap<GPSFix, Map<Mark, Distance>>(comp));
            HashMap<Waypoint, Map<List<GPSFix>, Candidate>> competitorXTECandidates = new HashMap<Waypoint, Map<List<GPSFix>, Candidate>>();
            HashMap<Waypoint, Map<GPSFix, Candidate>> competitorDistanceCandidates = new HashMap<Waypoint, Map<GPSFix, Candidate>>();
            cteCandidates.put(c, competitorXTECandidates);
            distanceCandidates.put(c, competitorDistanceCandidates);
            for (Waypoint w : waypoints) {
                competitorXTECandidates.put(w, new HashMap<List<GPSFix>, Candidate>());
                competitorDistanceCandidates.put(w, new HashMap<GPSFix, Candidate>());
            }
        }
        for (Waypoint w : waypoints) {
            PassingInstruction instruction = w.getPassingInstructions();
            if (instruction == PassingInstruction.None || instruction == null) {
                if (w.equals(firstWaypoint) || w.equals(lastWaypoint)) {
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
    }

    @Override
    public Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c) {
        Set<GPSFix> fixes = new TreeSet<GPSFix>(comp);
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
        try {
            track.lockForRead();
            for (GPSFix fix : track.getFixes()) {
                fixes.add(fix);
            }
        } finally {
            track.unlockAfterRead();
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
    public Map<Competitor, List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> markFixes) {
        Map<Competitor, List<GPSFix>> affectedFixes = new HashMap<>();
        for (Competitor c : race.getRace().getCompetitors()) {
            affectedFixes.put(c, new ArrayList<GPSFix>());
        }
        for (GPSFix fix : markFixes) {
            Pair<TimePoint, TimePoint> timePoints = race.getOrCreateTrack(mark).getEstimatedPositionTimePeriodAffectedBy(fix);
            for (Entry<Competitor, List<GPSFix>> c : affectedFixes.entrySet()) {
                DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c.getKey());
                GPSFix comFix = track.getFirstFixAtOrAfter(timePoints.getA());
                List<GPSFix> fixes = c.getValue();
                TimePoint end = timePoints.getB();
                TimePoint fixTimePoint = comFix.getTimePoint();
                if (end != null) {
                    while (comFix != null && !fixTimePoint.after(end)) {
                        fixes.add(comFix);
                        comFix = track.getFirstFixAfter(fixTimePoint);
                    }
                } else {
                    while (comFix != null) {
                        fixes.add(comFix);
                        comFix = track.getFirstFixAfter(fixTimePoint);
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
        Map<Waypoint, Pair<List<Candidate>, List<Candidate>>> distanceCandidates = checkForDistanceCandidateChanges(c, fixes);
        Map<Waypoint, Pair<List<Candidate>, List<Candidate>>> cteCandidates = checkForXTECandidatesChanges(c, fixes);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            Pair<List<Candidate>, List<Candidate>> waypointXTECans = cteCandidates.get(w);
            Pair<List<Candidate>, List<Candidate>> waypointDistanceCandidates = distanceCandidates.get(w);
            newCans.addAll(waypointXTECans.getA());
            newCans.addAll(waypointDistanceCandidates.getA());
            wrongCans.addAll(waypointXTECans.getB());
            wrongCans.addAll(waypointDistanceCandidates.getB());
        }
        if (newCans.size() != 0 || wrongCans.size() != 0) {
            logger.finest(newCans.size() + " new Candidates and " + wrongCans.size() + " removed Candidates for " + c);
        }
        return new Pair<Iterable<Candidate>, Iterable<Candidate>>(newCans, wrongCans);
    }

    /**
     * For each fix the distance to each waypoint is calculated. Then the fix is checked for being a candidate.
     */
    private Map<Waypoint, Pair<List<Candidate>, List<Candidate>>> checkForDistanceCandidateChanges(Competitor c, Iterable<GPSFix> fixes) {
        Map<Waypoint, Pair<List<Candidate>, List<Candidate>>> result = new HashMap<>();
        calculatesDistances(c, fixes);
        Iterable<Waypoint> waypoints = race.getRace().getCourse().getWaypoints();
        for (Waypoint w : waypoints) {
            result.put(w, new Pair<List<Candidate>, List<Candidate>>(new ArrayList<Candidate>(), new ArrayList<Candidate>()));
        }
        Set<GPSFix> affectedFixes = new TreeSet<GPSFix>(comp);
        TreeMap<GPSFix, Map<Mark, Distance>> competitorDistances = distances.get(c);
        for (GPSFix fix : fixes) {
            affectedFixes.add(fix);
            GPSFix fixBefore = competitorDistances.lowerKey(fix);
            GPSFix fixAfter = competitorDistances.higherKey(fix);
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
            GPSFix fixBefore = competitorDistances.lowerKey(fix);
            GPSFix fixAfter = competitorDistances.higherKey(fix);
            for (Waypoint w : waypoints) {
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
                    Distance dis = competitorDistances.get(fix).get(m);
                    Distance disBefore = fixBefore != null ? competitorDistances.get(fixBefore).get(m) : null;
                    Distance disAfter = fixAfter != null ? competitorDistances.get(fixAfter).get(m) : null;
                    if (dis == null || disBefore == null || disAfter == null) {
                        continue;
                    }
                    if (disBefore != null && disAfter != null && dis.getMeters() < disBefore.getMeters() && dis.getMeters() < disAfter.getMeters()) {
                        t = fix.getTimePoint();
                        p = fix.getPosition();
                        if (cost != null) { // Candidate for both marks of a gate, the more likely one is chosen
                            Double newCost = getDistanceBasedProbability(w, t, dis);
                            if (newCost == null || newCost * 0.8 < cost) {
                                continue;
                            }
                        }
                        cost = getDistanceBasedProbability(w, t, dis);
                        if (cost == null) {
                            continue;
                        }
                        cost *= 0.8;
                        if (cost > penaltyForSkipping) {
                            isCan = true;
                            if (passingInstructions.get(w) == PassingInstruction.Gate) {
                                Pair<Mark, Mark> pair = getPortAndStarboardMarks(t, w);
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
                for (Entry<GPSFix, Candidate> possibleCan : distanceCandidates.get(c).get(w).entrySet()) {
                    if (possibleCan.getKey().equals(fix)) {
                        wasCan = true;
                        oldCan = possibleCan.getValue();
                        break;
                    }
                }
                if (!wasCan && isCan) {
                    Candidate newCan = new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t, portMark),
                    /* correct Direction */true, "Distance");
                    distanceCandidates.get(c).get(w).put(fix, newCan);
                    result.get(w).getA().add(newCan);
                } else if (wasCan && !isCan) {
                    distanceCandidates.get(c).get(w).remove(fix);
                    result.get(w).getB().add(oldCan);
                } else if (wasCan && isCan && oldCan.getProbability() != cost) {
                    Candidate newCan = new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t, portMark),
                    /* correct Direction */true, "Distance");
                    distanceCandidates.get(c).get(w).put(fix, newCan);
                    result.get(w).getA().add(newCan);
                    result.get(w).getB().add(oldCan);
                }
            }
        }
        return result;
    }

    /**
     * Calculates the distance from each fix to each mark or line.
     */
    private void calculatesDistances(Competitor c, Iterable<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            HashMap<Mark, Distance> fixDistances = new HashMap<Mark, Distance>();
            distances.get(c).put(fix, fixDistances);
            for (Mark m : marks) {
                Distance distance = calculateDistance(fix.getPosition(), m, fix.getTimePoint());
                fixDistances.put(m, distance);
            }
        }
    }

    /**
     * For each fix the cross-track error(s) to each waypoint are calculated. Then all pairs of fixes are checked for
     * being a candidate.
     */
    private Map<Waypoint, Pair<List<Candidate>, List<Candidate>>> checkForXTECandidatesChanges(Competitor c, Iterable<GPSFix> fixes) {
        calculateCrossTrackErrors(c, fixes);
        Map<Waypoint, Pair<List<Candidate>, List<Candidate>>> totalResult = new HashMap<>();
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            totalResult.put(w, new Pair<List<Candidate>, List<Candidate>>(new ArrayList<Candidate>(), new ArrayList<Candidate>()));
        }
        for (GPSFix fix : fixes) {
            TimePoint t = fix.getTimePoint();
            TreeMap<GPSFix, Map<Waypoint, List<Double>>> competitorXTEs = crossTrackErrors.get(c);
            GPSFix fixBefore = competitorXTEs.lowerKey(fix);
            GPSFix fixAfter = competitorXTEs.higherKey(fix);
            Map<Waypoint, List<Double>> xtesBefore = null;
            Map<Waypoint, List<Double>> xtesAfter = null;
            TimePoint tBefore = null;
            TimePoint tAfter = null;
            if (fixBefore != null) {
                xtesBefore = competitorXTEs.get(fixBefore);
                tBefore = fixBefore.getTimePoint();
            }
            if (fixAfter != null) {
                xtesAfter = competitorXTEs.get(fixAfter);
                tAfter = fixAfter.getTimePoint();
            }
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                Pair<List<Candidate>, List<Candidate>> result = totalResult.get(w);
                List<List<GPSFix>> oldCandidates = new ArrayList<>();
                Map<List<GPSFix>, Candidate> newCandidates = new HashMap<List<GPSFix>, Candidate>();
                Map<List<GPSFix>, Candidate> waypointCandidates = cteCandidates.get(c).get(w);
                for (List<GPSFix> fixPair : waypointCandidates.keySet()) {
                    if (fixPair.contains(fix)) {
                        oldCandidates.add(fixPair);
                    }
                }
                List<Double> xtes = competitorXTEs.get(fix).get(w);
                int size = xtes.size();
                if (size > 0) {
                    Double xte = xtes.get(0);
                    if (xte == 0) {
                        newCandidates.put(Arrays.asList(fix, fix), createCandidate(c, 0, 0, t, t, w, true));
                    } else {
                        if (fixAfter != null) {
                            Double xteAfter = xtesAfter.get(w).get(0);
                            if (xte < 0 != xteAfter <= 0) {
                                newCandidates.put(Arrays.asList(fix, fixAfter), createCandidate(c, xte, xteAfter, t, tAfter, w, true));
                            }
                        }
                        if (fixBefore != null) {
                            Double xteBefore = xtesBefore.get(w).get(0);
                            if (xte < 0 != xteBefore <= 0) {
                                newCandidates.put(Arrays.asList(fixBefore, fix), createCandidate(c, xteBefore, xte, tBefore, t, w, true));
                            }
                        }
                    }
                }
                if (size > 1) {
                    Double xte = xtes.get(1);
                    if (xte == 0) {
                        newCandidates.put(Arrays.asList(fix, fix), createCandidate(c, 0, 0, t, t, w, false));
                    } else {
                        if (fixAfter != null) {
                            Double xteAfter = xtesAfter.get(w).get(1);
                            if (xte < 0 != xteAfter <= 0) {
                                newCandidates.put(Arrays.asList(fix, fixAfter), createCandidate(c, xte, xteAfter, t, tAfter, w, false));
                            }
                        }
                        if (fixBefore != null) {
                            Double xteBefore = xtesBefore.get(w).get(1);
                            if (xte < 0 != xteBefore <= 0) {
                                newCandidates.put(Arrays.asList(fixBefore, fix), createCandidate(c, xteBefore, xte, tBefore, t, w, false));
                            }
                        }
                    }
                }
                for (Entry<List<GPSFix>, Candidate> candidateWithFixes : newCandidates.entrySet()) {
                    Candidate newCan = candidateWithFixes.getValue();
                    List<GPSFix> canFixes = candidateWithFixes.getKey();
                    if (oldCandidates.contains(canFixes)) {
                        oldCandidates.remove(canFixes);
                        Candidate oldCan = waypointCandidates.get(canFixes);
                        if (newCan.compareTo(oldCan) != 0) {
                            result.getB().add(oldCan);
                            waypointCandidates.remove(canFixes);
                            if (newCan.getProbability() > penaltyForSkipping) {
                                result.getA().add(newCan);
                                waypointCandidates.put(canFixes, newCan);
                            }
                        }
                    } else {
                        if (newCan.getProbability() > penaltyForSkipping) {
                            result.getA().add(newCan);
                            waypointCandidates.put(canFixes, newCan);
                        }
                    }
                }
                for (List<GPSFix> badCanFixes : oldCandidates) {
                    result.getB().add(waypointCandidates.get(badCanFixes));
                    waypointCandidates.remove(badCanFixes);
                }
            }
        }
        return totalResult;
    }

    /**
     * Calculates the cross-track error of each fix to the position and crossing bearing of each waypoint. Gates have
     * two of these and lines always go from the port mark to the starboard mark.
     */
    private void calculateCrossTrackErrors(Competitor c, Iterable<GPSFix> fixes) {
        for (GPSFix fix : fixes) {
            Position fixPos = fix.getPosition();
            TimePoint t = fix.getTimePoint();
            Map<Waypoint, List<Double>> waypointXTE = new HashMap<Waypoint, List<Double>>();
            crossTrackErrors.get(c).put(fix, waypointXTE);
            for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                List<Double> xte = new ArrayList<>();
                waypointXTE.put(w, xte);
                for (Pair<Position, Bearing> crossingInfo : getCrossingBearing(w, t)) {
                    xte.add(fixPos.crossTrackError(crossingInfo.getA(), crossingInfo.getB()).getMeters());
                }
            }
        }
    }

    private Candidate createCandidate(Competitor c, double cte1, double cte2, TimePoint t1, TimePoint t2, Waypoint w, Boolean portMark) {
        long differenceInMillis = t2.asMillis() - t1.asMillis();
        double ratio = (Math.abs(cte1) / (Math.abs(cte1) + Math.abs(cte2)));
        TimePoint t = t1.plus((long) (differenceInMillis * ratio));
        Position p = race.getTrack(c).getEstimatedPosition(t, false);
        Mark m = null;
        PassingInstruction instruction = passingInstructions.get(w);
        if (instruction == PassingInstruction.Gate) {
            Pair<Mark, Mark> marks = getPortAndStarboardMarks(t, w);
            m = portMark ? marks.getA() : marks.getB();
        } else if (instruction == PassingInstruction.Line) {
            m = lineMarks.get(w);
        } else {
            m = w.getMarks().iterator().next();
        }
        Distance d = calculateDistance(p, m, t);
        double cost = getDistanceBasedProbability(w, t, d);
        return new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w, isOnCorrectSideOfWaypoint(w, p, t, portMark), passesInTheRightDirection(w, cte1,
                cte2, portMark), "CTE");
    }

    /**
     * Determines whether a candidate is on the correct side of a waypoint. This is defined by the crossing information.
     * The cross-track error of <code>p</code> to the crossing Position and the crossing Bearing rotated by 90° need to
     * be negative. If the passing Instructions are line, it checks whether the boat passed between the two marks.
     */
    private boolean isOnCorrectSideOfWaypoint(Waypoint w, Position p, TimePoint t, boolean portMark) {
        boolean result = true;
        PassingInstruction instruction = passingInstructions.get(w);
        if (instruction == PassingInstruction.Line) {
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
            if (instruction == PassingInstruction.Port || instruction == PassingInstruction.Starboard || instruction == PassingInstruction.FixedBearing) {
                m = w.getMarks().iterator().next();
            }
            if (instruction == PassingInstruction.Gate) {
                Pair<Mark, Mark> pair = getPortAndStarboardMarks(t, w);
                m = portMark ? pair.getA() : pair.getB();
            }
            if (m != null) {
                Pair<Position, Bearing> crossingInfo = Util.get(getCrossingBearing(w, t), portMark ? 0 : 1);
                result = p.crossTrackError(crossingInfo.getA(), crossingInfo.getB().add(new DegreeBearingImpl(90))).getMeters() < 0;
            }
        }
        return result;
    }

    /**
     * Determines whether a candidate passes a waypoint in the right direction. Can only be applied to XTE-Candidates.
     * For marks passed on port, the cross-track error should switch from positive to negative and vice versa. Lines are
     * also from positive to negative as the cross-track error to a line is always positive when approaching it from the
     * correct side.
     */
    private boolean passesInTheRightDirection(Waypoint w, double cte1, double cte2, boolean portMark) {
        boolean result = true;
        PassingInstruction instruction = passingInstructions.get(w);
        if (instruction == PassingInstruction.Port || instruction == PassingInstruction.Line || (instruction == PassingInstruction.Gate && portMark)) {
            result = cte1 > cte2 ? true : false;
        } else if (instruction == PassingInstruction.Starboard || (instruction == PassingInstruction.Gate && !portMark)) {
            result = cte1 < cte2 ? true : false;
        }
        return result;
    }

    /**
     * @return a probability based on the distance to <code>w</code> and the average leg lengths before and after.
     */
    private Double getDistanceBasedProbability(Waypoint w, TimePoint t, Distance distance) {
        Distance legLength = getLegLength(t, w);
        if (legLength != null) {
            double result = 1 / (8/*Raising this will make is stricter*/* Math.abs(distance.getMeters() / legLength.getMeters()) + 1);
            // Auch NormalVerteilung?
            return result;
        }
        return null;
    }

    /**
     * @return an average of the estimated legs before and after <code>w</code>.
     */
    private Distance getLegLength(TimePoint t, Waypoint w) {
        Course course = race.getRace().getCourse();
        if (w == course.getFirstWaypoint()) {
            return race.getTrackedLegStartingAt(w).getGreatCircleDistance(t);
        } else if (w == course.getLastWaypoint()) {
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

    /**
     * If <code>m</code> is contained in <code>lineMarks</code>, the distance to that line is calculated, else simply
     * the distance to the m.
     */
    private Distance calculateDistance(Position p, Mark m, TimePoint t) {
        Distance distance = null;
        if (!lineMarks.containsValue(m)) {
            Position markPosition = race.getOrCreateTrack(m).getEstimatedPosition(t, false);
            distance = markPosition != null ? p.getDistance(markPosition) : null;
        } else {
            Waypoint w = null;
            for (Entry<Waypoint, Mark> way : lineMarks.entrySet()) {
                if (way.getValue() == m) {
                    w = way.getKey();
                    break;
                }
            }
            Pair<Mark, Mark> pos = getPortAndStarboardMarks(t, w);
            Position p1 = race.getOrCreateTrack(pos.getA()).getEstimatedPosition(t, false);
            Position p2 = race.getOrCreateTrack(pos.getB()).getEstimatedPosition(t, false);
            distance = (p1 != null && p2 != null) ? p.getDistanceToLine(p1, p2) : p1 != null ? p.getDistance(p1) : p2 != null ? p.getDistance(p1) : null;
        }
        return distance;
    }

    /**
     * @return all possible ways to pass a waypoint, described as a position and a bearing. The line out of those two
     *         muss be crossed.
     */
    private Iterable<Pair<Position, Bearing>> getCrossingBearing(Waypoint w, TimePoint t) {
        List<Pair<Position, Bearing>> result = new ArrayList<>();
        PassingInstruction instruction = w.getPassingInstructions();
        if (instruction == PassingInstruction.None || instruction == null) {
            Course course = race.getRace().getCourse();
            if (w.equals(course.getFirstWaypoint()) || w.equals(course.getLastWaypoint())) {
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
                }
            }
        }
        if (instruction == PassingInstruction.Line) {
            Pair<Mark, Mark> marks = getPortAndStarboardMarks(t, w);
            Position portPosition = null;
            Bearing b = null;
            Mark portMark = marks.getA();
            Mark startBoardMark = marks.getB();
            if (portMark != null && startBoardMark != null) {
                portPosition = race.getOrCreateTrack(portMark).getEstimatedPosition(t, false);
                Position starboardPosition = race.getOrCreateTrack(startBoardMark).getEstimatedPosition(t, false);
                if (portPosition != null && starboardPosition != null) {
                    b = portPosition.getBearingGreatCircle(starboardPosition);
                }
            }
            result.add(new Pair<Position, Bearing>(portPosition, b));
        } else if (instruction == PassingInstruction.Gate) {
            Position before = race.getApproximatePosition(race.getTrackedLegFinishingAt(w).getLeg().getFrom(), t);
            Position after = race.getApproximatePosition(race.getTrackedLegStartingAt(w).getLeg().getTo(), t);
            Pair<Mark, Mark> pos = getPortAndStarboardMarks(t, w);
            Mark portMark = pos.getA();
            if (portMark != null) {
                Position portPosition = race.getOrCreateTrack(portMark).getEstimatedPosition(t, false);
                Bearing crossingPort = before.getBearingGreatCircle(portPosition).middle(after.getBearingGreatCircle(portPosition));
                result.add(new Pair<Position, Bearing>(portPosition, crossingPort));
            }
            Mark starboardMark = pos.getB();
            if (starboardMark != null) {
                Position starboardPosition = race.getOrCreateTrack(starboardMark).getEstimatedPosition(t, false);
                Bearing crossingStarboard = before.getBearingGreatCircle(starboardPosition).middle(after.getBearingGreatCircle(starboardPosition));
                result.add(new Pair<Position, Bearing>(starboardPosition, crossingStarboard));
            }
        } else {
            Bearing b = null;
            Position p;
            if (instruction == PassingInstruction.FixedBearing) {
                b = w.getFixedBearing();
            } else {
                Bearing before = race.getTrackedLegFinishingAt(w).getLegBearing(t);
                Bearing after = race.getTrackedLegStartingAt(w).getLegBearing(t);
                if (before != null && after != null) {
                    b = before.middle(after.reverse());
                }
            }
            p = race.getOrCreateTrack(w.getMarks().iterator().next()).getEstimatedPosition(t, false);
            result.add(new Pair<Position, Bearing>(p, b));
        }
        return result;
    }

    /**
     * @return the marks of a waypoint with two marks in the order port, starboard (when approching the waypoint from
     *         the direction of the waypoint beforehand.
     */
    private Pair<Mark, Mark> getPortAndStarboardMarks(TimePoint t, Waypoint w) {
        List<Position> markPositions = new ArrayList<Position>();
        for (Mark mark : w.getMarks()) {
            final Position estimatedMarkPosition = race.getOrCreateTrack(mark).getEstimatedPosition(t, /* extrapolate */false);
            if (estimatedMarkPosition == null) {
                return new Pair<Mark, Mark>(null, null);
            }
            markPositions.add(estimatedMarkPosition);
        }
        final List<Leg> legs = race.getRace().getCourse().getLegs();
        final int indexOfWaypoint = race.getRace().getCourse().getIndexOfWaypoint(w);
        final boolean isStartLine = indexOfWaypoint == 0;
        final Bearing legDeterminingDirectionBearing = race.getTrackedLeg(legs.get(isStartLine ? 0 : indexOfWaypoint - 1)).getLegBearing(t);
        if (legDeterminingDirectionBearing == null) {
            return new Pair<Mark, Mark>(null, null);
        }
        Distance crossTrackErrorOfMark0OnLineFromMark1ToNextWaypoint = markPositions.get(0).crossTrackError(markPositions.get(1), legDeterminingDirectionBearing);
        final Mark starboardMarkWhileApproachingLine;
        final Mark portMarkWhileApproachingLine;
        if (crossTrackErrorOfMark0OnLineFromMark1ToNextWaypoint.getMeters() < 0) {
            portMarkWhileApproachingLine = Util.get(w.getMarks(), 0);
            starboardMarkWhileApproachingLine = Util.get(w.getMarks(), 1);
        } else {
            portMarkWhileApproachingLine = Util.get(w.getMarks(), 1);
            starboardMarkWhileApproachingLine = Util.get(w.getMarks(), 0);
        }
        return new Pair<Mark, Mark>(portMarkWhileApproachingLine, starboardMarkWhileApproachingLine);
    }
}
