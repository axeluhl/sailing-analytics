package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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

    // TODO Parallelization?

    private final int strictnessOfDistanceBasedProbability = 8;
    private final double penaltyForWrongSide = 0.7;
    private final double penaltyForWrongDirection = 0.7;
    private final double penaltyForDistanceCandidates = 0.7;

    private static final Logger logger = Logger.getLogger(CandidateFinderImpl.class.getName());
    private Map<Competitor, LinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>> distanceCache = new LinkedHashMap<>();
    private Map<Competitor, LinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>> xteCache = new LinkedHashMap<>();

    private Map<Competitor, Map<Waypoint, Map<List<GPSFix>, Candidate>>> xteCandidates = new HashMap<>();
    private Map<Competitor, Map<Waypoint, Map<GPSFix, Candidate>>> distanceCandidates = new HashMap<>();
    private final DynamicTrackedRace race;
    private final double penaltyForSkipping = 1 - Edge.getPenaltyForSkipping();
    private final Map<Waypoint, PassingInstruction> passingInstructions = new LinkedHashMap<>();
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
        Waypoint firstWaypointOfRace = course.getFirstWaypoint();
        Waypoint lastWaypointOfRace = course.getLastWaypoint();
        for (Competitor c : raceDefinition.getCompetitors()) {
            xteCache.put(c, new LimitedLinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>(25));
            distanceCache.put(c, new LimitedLinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>(25));
            xteCandidates.put(c, new HashMap<Waypoint, Map<List<GPSFix>, Candidate>>());
            distanceCandidates.put(c, new HashMap<Waypoint, Map<GPSFix, Candidate>>());
        }
        for (Waypoint w : waypoints) {
            processNewWaypoint(w, firstWaypointOfRace, lastWaypointOfRace);
        }
    }

    private class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;
        private int limit;

        public LimitedLinkedHashMap(int limit) {
            super();
            this.limit = limit;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > limit;
        }
    }

    @Override
    public Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c) {
        Set<GPSFix> fixes = getAllFixes(c);
        distanceCache.get(c).clear();
        xteCache.get(c).clear();
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            xteCandidates.get(c).get(w).clear();
            distanceCandidates.get(c).get(w).clear();
        }
        return getCandidateDeltas(c, fixes);
    }

    @Override
    public Map<Competitor, List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Map<Mark, List<GPSFix>> markFixes) {
        // TODO Right now creates on time stretch between the 2 outside markfixes
        Map<Competitor, List<GPSFix>> affectedFixes = new HashMap<>();
        TimePoint start = null;
        TimePoint end = null;
        for (Entry<Mark, List<GPSFix>> fixes : markFixes.entrySet()) {
            for (GPSFix fix : fixes.getValue()) {
                Util.Pair<TimePoint, TimePoint> timePoints = race.getOrCreateTrack(fixes.getKey())
                        .getEstimatedPositionTimePeriodAffectedBy(fix);
                TimePoint newStart = timePoints.getA();
                TimePoint newEnd = timePoints.getB();
                start = start == null || start.after(newStart) ? newStart : start;
                end = end == null || end.before(newEnd) ? newEnd : end;
            }
        }
        for (Competitor c : race.getRace().getCompetitors()) {
            List<GPSFix> comFixes = new ArrayList<>();
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
            GPSFix comFix = track.getFirstFixAtOrAfter(start);
            if (comFix != null) {
                if (end != null) {
                    while (comFix != null && !comFix.getTimePoint().after(end)) {
                        comFixes.add(comFix);
                        comFix = track.getFirstFixAfter(comFix.getTimePoint());
                    }
                } else {
                    while (comFix != null) {
                        comFixes.add(comFix);
                        comFix = track.getFirstFixAfter(comFix.getTimePoint());
                    }
                }
            }
            if (!comFixes.isEmpty()) {
                affectedFixes.put(c, comFixes);
            }
        }
        return affectedFixes;
    }

    @Override
    public Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes) {

        List<Candidate> newCans = new ArrayList<>();
        List<Candidate> wrongCans = new ArrayList<>();
        Util.Pair<List<Candidate>, List<Candidate>> distanceCandidates = checkForDistanceCandidateChanges(c, fixes,
                race.getRace().getCourse().getWaypoints());
        Util.Pair<List<Candidate>, List<Candidate>> xteCandidates = checkForXTECandidatesChanges(c, fixes, race
                .getRace().getCourse().getWaypoints());
        logger.finest(distanceCandidates.getA().size() + " new Distance Candidates, " + xteCandidates.getA().size()
                + " new XTE Candidates, " + distanceCandidates.getB().size() + " removed distance Candidates and "
                + xteCandidates.getB().size() + " removed XTE Candidates.");
        newCans.addAll(xteCandidates.getA());
        newCans.addAll(distanceCandidates.getA());
        wrongCans.addAll(xteCandidates.getB());
        wrongCans.addAll(distanceCandidates.getB());
        return new Util.Pair<Iterable<Candidate>, Iterable<Candidate>>(newCans, wrongCans);
    }

    private Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> invalidateAfterCourseChange(int indexOfChange) {
        Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> result = new HashMap<>();
        Course course = race.getRace().getCourse();
        for (Competitor c : race.getRace().getCompetitors()) {
            distanceCache.get(c).clear();
            xteCache.get(c).clear();
        }
        List<Waypoint> changedWaypoints = new ArrayList<>();
        for (Waypoint w : course.getWaypoints()) {
            if (course.getIndexOfWaypoint(w) > indexOfChange - 2) {
                changedWaypoints.add(w);
            }
        }
        for (Competitor c : race.getRace().getCompetitors()) {
            List<Candidate> badCans = new ArrayList<>();
            List<Candidate> newCans = new ArrayList<>();
            for (Waypoint w : changedWaypoints) {
                Map<List<GPSFix>, Candidate> xteCans = xteCandidates.get(c).get(w);
                badCans.addAll(xteCans.values());
                xteCans.clear();
                ;
                Map<GPSFix, Candidate> distanceCans = distanceCandidates.get(c).get(w);
                badCans.addAll(distanceCans.values());
                distanceCans.clear();
            }
            Set<GPSFix> allFixes = getAllFixes(c);
            newCans.addAll(checkForDistanceCandidateChanges(c, allFixes, changedWaypoints).getA());
            newCans.addAll(checkForXTECandidatesChanges(c, allFixes, changedWaypoints).getA());
            result.put(c, new Util.Pair<List<Candidate>, List<Candidate>>(newCans, badCans));
        }
        return result;
    }

    @Override
    public Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> updateWaypoints(
            Iterable<Waypoint> addedWaypoints, Iterable<Waypoint> removedWaypoints, Integer smallestIndex) {
        Course course = race.getRace().getCourse();
        for (Waypoint w : addedWaypoints) {
            processNewWaypoint(w, course.getFirstWaypoint(), course.getLastWaypoint());
        }
        Map<Competitor, List<Candidate>> removedWaypointCandidates = removeWaypoints(removedWaypoints);
        Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> newAndUpdatedCandidates = invalidateAfterCourseChange(smallestIndex);
        for (Entry<Competitor, List<Candidate>> entry : removedWaypointCandidates.entrySet()) {
            newAndUpdatedCandidates.get(entry.getKey()).getB().addAll(entry.getValue());
        }
        return newAndUpdatedCandidates;
    }

    private Map<Competitor, List<Candidate>> removeWaypoints(Iterable<Waypoint> waypoints) {
        // TODO Clear caches?
        Map<Competitor, List<Candidate>> result = new HashMap<>();
        for (Competitor c : race.getRace().getCompetitors()) {
            result.put(c, new ArrayList<Candidate>());
        }
        for (Waypoint w : waypoints) {
            passingInstructions.remove(w);
            for (Entry<Competitor, List<Candidate>> entry : result.entrySet()) {
                Competitor c = entry.getKey();
                List<Candidate> badCans = entry.getValue();
                Map<Waypoint, Map<List<GPSFix>, Candidate>> xteCans = xteCandidates.get(c);
                badCans.addAll(xteCans.get(w).values());
                xteCans.remove(w);
                Map<Waypoint, Map<GPSFix, Candidate>> distanceCans = distanceCandidates.get(c);
                badCans.addAll(distanceCans.get(w).values());
                distanceCans.remove(w);
            }
        }
        return result;
    }

    private void processNewWaypoint(Waypoint w, Waypoint firstWaypoint, Waypoint lastWaypoint) {
        PassingInstruction instruction = w.getPassingInstructions();
        if ((w.equals(firstWaypoint) || w.equals(lastWaypoint)) && instruction == PassingInstruction.Gate) {
            instruction = PassingInstruction.Line;
        }
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
        for (Competitor c : race.getRace().getCompetitors()) {
            xteCandidates.get(c).put(w, new HashMap<List<GPSFix>, Candidate>());
            distanceCandidates.get(c).put(w, new HashMap<GPSFix, Candidate>());
        }
    }

    private Set<GPSFix> getAllFixes(Competitor c) {
        Set<GPSFix> fixes = new TreeSet<GPSFix>(comp);
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
        try {
            track.lockForRead();
            for (GPSFix fix : track.getFixes(
                    race.getStartOfTracking()==null?TimePoint.BeginningOfTime:race.getStartOfTracking(),
                    /* fromInclusive */ true,
                    race.getEndOfTracking()==null?TimePoint.EndOfTime:race.getEndOfTracking(),
                    /* toInclusive */ true)) {
                fixes.add(fix);
            }
        } finally {
            track.unlockAfterRead();
        }
        return fixes;
    }

    /**
     * For each fix the distance to each waypoint is calculated. Then the fix is checked for being a candidate.
     */
    private Util.Pair<List<Candidate>, List<Candidate>> checkForDistanceCandidateChanges(Competitor c,
            Iterable<GPSFix> fixes, Iterable<Waypoint> waypoints) {
        Util.Pair<List<Candidate>, List<Candidate>> result = new Util.Pair<List<Candidate>, List<Candidate>>(
                new ArrayList<Candidate>(), new ArrayList<Candidate>());
        TreeSet<GPSFix> affectedFixes = new TreeSet<GPSFix>(comp);
        GPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
        for (GPSFix fix : fixes) {
            affectedFixes.add(fix);
            GPSFix fixBefore;
            GPSFix fixAfter;
            try {
                track.lockForRead();
                TimePoint timePoint = fix.getTimePoint();
                fixBefore = track.getLastFixBefore(timePoint);
                fixAfter = track.getFirstFixAfter(timePoint);
            } finally {
                track.unlockAfterRead();
            }
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
            GPSFix fixBefore;
            GPSFix fixAfter;
            try {
                track.lockForRead();
                TimePoint timePoint = fix.getTimePoint();
                fixBefore = track.getLastFixBefore(timePoint);
                fixAfter = track.getFirstFixAfter(timePoint);
            } finally {
                track.unlockAfterRead();
            }
            if (fixBefore != null && fixAfter != null) {
                Map<Waypoint, List<Distance>> fixDistances = getDistances(c, fix);
                Map<Waypoint, List<Distance>> fixDistancesBefore = getDistances(c, fixBefore);
                Map<Waypoint, List<Distance>> fixDistancesAfter = getDistances(c, fixAfter);
                for (Waypoint w : waypoints) {
                    Boolean wasCan = false;
                    Boolean isCan = false;
                    Candidate oldCan = null;
                    Double probability = null;
                    List<Distance> waypointDistances = fixDistances.get(w);
                    List<Distance> waypointDistancesBefore = fixDistancesBefore.get(w);
                    List<Distance> waypointDistancesAfter = fixDistancesAfter.get(w);
                    // due to course changes, waypoints that exist in the waypoints collection may not have a corresponding
                    // key in passingInstructions' key set which is the basis for the waypoints for which getDistances(...)
                    // computes results; so we have to check for null here:
                    if (waypointDistances != null && waypointDistancesBefore != null && waypointDistancesAfter != null) {
                        Iterator<Distance> disIter = waypointDistances.iterator();
                        Iterator<Distance> disBeforeIter = waypointDistancesBefore.iterator();
                        Iterator<Distance> disAfterIter = waypointDistancesAfter.iterator();
                        boolean portMark = true;
                        while (disIter.hasNext() && disBeforeIter.hasNext() && disAfterIter.hasNext()) {
                            Distance dis = disIter.next();
                            Distance disBefore = disBeforeIter.next();
                            Distance disAfter = disAfterIter.next();
                            if (dis != null && disBefore != null && disAfter != null) {
                                if (Math.abs(dis.getMeters()) < Math.abs(disBefore.getMeters())
                                        && Math.abs(dis.getMeters()) < Math.abs(disAfter.getMeters())) {
                                    t = fix.getTimePoint();
                                    p = fix.getPosition();
                                    Double newProbability = getDistanceBasedProbability(w, t, dis);
                                    if (newProbability != null) {
                                        newProbability *= isOnCorrectSideOfWaypoint(w, p, t, portMark) ? penaltyForDistanceCandidates
                                                : penaltyForDistanceCandidates * penaltyForWrongSide;
                                        if (newProbability > penaltyForSkipping
                                                && (probability == null || newProbability > probability)) {
                                            isCan = true;
                                            probability = newProbability;
                                        }
                                    }
                                }
                            }
                            portMark = false;
                        }
                        oldCan = distanceCandidates.get(c).get(w).get(fix);
                        if (oldCan != null) {
                            wasCan = true;
                        }
                        if (!wasCan && isCan) {
                            Candidate newCan = new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t,
                                    probability, w);
                            distanceCandidates.get(c).get(w).put(fix, newCan);
                            result.getA().add(newCan);
                            logger.finest("Added distance" + newCan.toString() + "for " + c);
                        } else if (wasCan && !isCan) {
                            distanceCandidates.get(c).get(w).remove(fix);
                            result.getB().add(oldCan);
                        } else if (wasCan && isCan && oldCan.getProbability() != probability) {
                            Candidate newCan = new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t,
                                    probability, w);
                            distanceCandidates.get(c).get(w).put(fix, newCan);
                            result.getA().add(newCan);
                            logger.finest("Added distance" + newCan.toString() + "for " + c);
                            result.getB().add(oldCan);
                        }
                    }
                }
            }
        }
        return result;
    }

    private Map<Waypoint, List<Distance>> getDistances(Competitor c, GPSFix fix) {
        // TODO Possibly for specific waypoints
        Map<Waypoint, List<Distance>> result = distanceCache.get(c).get(fix);
        if (result == null) {
            // Else calculate distances and put them into the cache
            result = new LinkedHashMap<>();
            for (Waypoint w : passingInstructions.keySet()) {
                List<Distance> distances = calculateDistance(fix.getPosition(), w, fix.getTimePoint());
                result.put(w, distances);
            }
            distanceCache.get(c).put(fix, result);
        }
        return result;
    }

    /**
     * For each fix the cross-track error(s) to each waypoint are calculated. Then all Util.Pairs of fixes are checked
     * for being a candidate.
     * 
     * @param waypointAsList
     */
    private Util.Pair<List<Candidate>, List<Candidate>> checkForXTECandidatesChanges(Competitor c,
            Iterable<GPSFix> fixes, Iterable<Waypoint> waypoints) {
        Util.Pair<List<Candidate>, List<Candidate>> result = new Util.Pair<List<Candidate>, List<Candidate>>(
                new ArrayList<Candidate>(), new ArrayList<Candidate>());
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
        for (GPSFix fix : fixes) {
            TimePoint t = fix.getTimePoint();
            GPSFix fixBefore;
            GPSFix fixAfter;
            track.lockForRead();
            try {
                fixBefore = track.getLastFixBefore(t);
                fixAfter = track.getFirstFixAfter(t);
            } finally {
                track.unlockAfterRead();
            }
            Map<Waypoint, List<Distance>> xtesBefore = null;
            Map<Waypoint, List<Distance>> xtesAfter = null;
            TimePoint tBefore = null;
            TimePoint tAfter = null;
            if (fixBefore != null) {
                xtesBefore = getXTE(c, fixBefore);
                tBefore = fixBefore.getTimePoint();
            }
            if (fixAfter != null) {
                xtesAfter = getXTE(c, fixAfter);
                tAfter = fixAfter.getTimePoint();
            }
            Map<Waypoint, List<Distance>> xtes = getXTE(c, fix);
            for (Waypoint w : waypoints) {
                List<List<GPSFix>> oldCandidates = new ArrayList<>();
                Map<List<GPSFix>, Candidate> newCandidates = new HashMap<List<GPSFix>, Candidate>();
                Map<List<GPSFix>, Candidate> waypointCandidates = xteCandidates.get(c).get(w);
                for (List<GPSFix> fixPair : waypointCandidates.keySet()) {
                    if (fixPair.contains(fix)) {
                        oldCandidates.add(fixPair);
                    }
                }
                List<Distance> wayPointXTEs = xtes.get(w);
                int size = wayPointXTEs.size();
                if (size > 0) {
                    Double xte = wayPointXTEs.get(0).getMeters();
                    if (xte == 0) {
                        newCandidates.put(Arrays.asList(fix, fix), createCandidate(c, 0, 0, t, t, w, true));
                    } else {
                        if (fixAfter != null && xtesAfter != null && !xtesAfter.get(w).isEmpty()) {
                            Double xteAfter = xtesAfter.get(w).get(0).getMeters();
                            if (xteAfter != null && xte < 0 != xteAfter <= 0) {
                                newCandidates.put(Arrays.asList(fix, fixAfter),
                                        createCandidate(c, xte, xteAfter, t, tAfter, w, true));
                            }
                        }
                        if (fixBefore != null && !xtesBefore.get(w).isEmpty()) {
                            Double xteBefore = xtesBefore.get(w).get(0).getMeters();
                            if (xte < 0 != xteBefore <= 0) {
                                newCandidates.put(Arrays.asList(fixBefore, fix),
                                        createCandidate(c, xteBefore, xte, tBefore, t, w, true));
                            }
                        }
                    }
                }
                if (size > 1) {
                    Double xte = wayPointXTEs.get(1).getMeters();
                    if (xte == 0) {
                        newCandidates.put(Arrays.asList(fix, fix), createCandidate(c, 0, 0, t, t, w, false));
                    } else {
                        if (fixAfter != null && xtesAfter != null) {
                            Double xteAfter = xtesAfter.get(w).get(1).getMeters();
                            if (xte < 0 != xteAfter <= 0) {
                                newCandidates.put(Arrays.asList(fix, fixAfter),
                                        createCandidate(c, xte, xteAfter, t, tAfter, w, false));
                            }
                        }
                        if (fixBefore != null) {
                            Double xteBefore = xtesBefore.get(w).get(1).getMeters();
                            if (xte < 0 != xteBefore <= 0) {
                                newCandidates.put(Arrays.asList(fixBefore, fix),
                                        createCandidate(c, xteBefore, xte, tBefore, t, w, false));
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
                                logger.finest("Added XTE " + newCan.toString() + "for " + c);
                                waypointCandidates.put(canFixes, newCan);
                            }
                        }
                    } else {
                        if (newCan.getProbability() > penaltyForSkipping) {
                            result.getA().add(newCan);
                            logger.finest("Added XTE " + newCan.toString() + "for " + c);
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
        return result;
    }

    private Map<Waypoint, List<Distance>> getXTE(Competitor c, GPSFix fix) {
        Map<Waypoint, List<Distance>> result = xteCache.get(c).get(fix);
        if (result == null) {
            result = new HashMap<>();
            Position p = fix.getPosition();
            TimePoint t = fix.getTimePoint();
            for (Waypoint w : passingInstructions.keySet()) {
                List<Distance> distances = new ArrayList<>();
                result.put(w, distances);
                for (Util.Pair<Position, Bearing> crossingInfo : getCrossingInformation(w, t)) {
                    if (crossingInfo.getA() != null && crossingInfo.getB() != null) {
                        distances.add(p.crossTrackError(crossingInfo.getA(), crossingInfo.getB()));
                    }
                }
            }
            xteCache.get(c).put(fix, result);
        }
        return result;
    }

    /**
     * Calculates the cross-track error of each fix to the position and crossing bearing of each waypoint. Gates have
     * two of these and lines always go from the port mark to the starboard mark.
     * 
     * 
     * /* private void calculateCrossTrackErrors(Competitor c, Iterable<GPSFix> fixes, Iterable<Waypoint>
     * waypointsToCalculate) { for (GPSFix fix : fixes) { Position fixPos = fix.getPosition(); TimePoint t =
     * fix.getTimePoint(); Map<Waypoint, List<Double>> waypointXTE = new HashMap<Waypoint, List<Double>>();
     * crossTrackErrors.get(c).put(fix, waypointXTE); for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
     * List<Double> xte = new ArrayList<>(); waypointXTE.put(w, xte); for (Util.Pair<Position, Bearing> crossingInfo :
     * getCrossingInformation(w, t)) { xte.add(fixPos.crossTrackError(crossingInfo.getA(),
     * crossingInfo.getB()).getMeters()); } } } }
     */

    private Candidate createCandidate(Competitor c, double xte1, double xte2, TimePoint t1, TimePoint t2, Waypoint w,
            Boolean portMark) {
        long differenceInMillis = t2.asMillis() - t1.asMillis();
        double ratio = (Math.abs(xte1) / (Math.abs(xte1) + Math.abs(xte2)));
        TimePoint t = t1.plus((long) (differenceInMillis * ratio));
        Position p = race.getTrack(c).getEstimatedPosition(t, false);
        List<Distance> distances = calculateDistance(p, w, t);
        Distance d = portMark ? distances.get(0) : distances.get(1);
        double cost = getDistanceBasedProbability(w, t, d);
        cost = isOnCorrectSideOfWaypoint(w, p, t, portMark) ? cost : cost * penaltyForWrongSide;
        cost = passesInTheRightDirection(w, xte1, xte2, portMark) ? cost : cost * penaltyForWrongDirection;
        return new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, cost, w);
    }

    /**
     * Determines whether a candidate is on the correct side of a waypoint. This is defined by the crossing information.
     * The cross-track error of <code>p</code> to the crossing Position and the crossing Bearing rotated by 90deg need
     * to be negative. If the passing instructions are {@link PassingInstruction#Line}, it checks whether the boat
     * passed between the two marks.
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
            Bearing diff1 = pos.get(0).getBearingGreatCircle(p)
                    .getDifferenceTo(pos.get(0).getBearingGreatCircle(pos.get(1)));
            Bearing diff2 = pos.get(1).getBearingGreatCircle(p)
                    .getDifferenceTo(pos.get(1).getBearingGreatCircle(pos.get(0)));
            if (Math.abs(diff1.getDegrees()) > 90 || Math.abs(diff2.getDegrees()) > 90) {
                result = false;
            }

        } else {
            Mark m = null;
            if (instruction == PassingInstruction.Port || instruction == PassingInstruction.Starboard
                    || instruction == PassingInstruction.FixedBearing) {
                m = w.getMarks().iterator().next();
            } else if (instruction == PassingInstruction.Gate) {
                Util.Pair<Mark, Mark> pair = getPortAndStarboardMarks(t, w);
                m = portMark ? pair.getA() : pair.getB();
            }
            if (m != null) {
                Util.Pair<Position, Bearing> crossingInfo = Util.get(getCrossingInformation(w, t), portMark ? 0 : 1);
                result = p.crossTrackError(crossingInfo.getA(), crossingInfo.getB().add(new DegreeBearingImpl(90)))
                        .getMeters() < 0;
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
    private boolean passesInTheRightDirection(Waypoint w, double xte1, double xte2, boolean portMark) {
        boolean result = true;
        PassingInstruction instruction = passingInstructions.get(w);
        if (instruction == PassingInstruction.Port || instruction == PassingInstruction.Line
                || (instruction == PassingInstruction.Gate && portMark)) {
            result = xte1 > xte2 ? true : false;
        } else if (instruction == PassingInstruction.Starboard || (instruction == PassingInstruction.Gate && !portMark)) {
            result = xte1 < xte2 ? true : false;
        }
        return result;
    }

    /**
     * @return a probability based on the distance to <code>w</code> and the average leg lengths before and after.
     */
    private Double getDistanceBasedProbability(Waypoint w, TimePoint t, Distance distance) {
        Distance legLength = getLegLength(t, w);
        if (legLength != null) {
            double result = 1 / (strictnessOfDistanceBasedProbability/* Raising this will make is stricter */
                    * Math.abs(distance.getMeters() / legLength.getMeters()) + 1);
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
    private List<Distance> calculateDistance(Position p, Waypoint w, TimePoint t) {
        List<Distance> distances = new ArrayList<>();
        PassingInstruction instruction = passingInstructions.get(w);
        boolean singleMark = false;
        switch (instruction) {
        case Port:
            singleMark = true;
            break;
        case Starboard:
            singleMark = true;
            break;
        case FixedBearing:
            singleMark = true;
            break;
        case Gate:
            Util.Pair<Mark, Mark> posGate = getPortAndStarboardMarks(t, w);
            if (posGate.getA() != null) {
                Position portGatePosition = race.getOrCreateTrack(posGate.getA()).getEstimatedPosition(t, false);
                distances.add(portGatePosition != null ? p.getDistance(portGatePosition) : null);
            } else {
                distances.add(null);
            }
            if (posGate.getB() != null) {
                Position starboardGatePosition = race.getOrCreateTrack(posGate.getB()).getEstimatedPosition(t, false);
                distances.add(starboardGatePosition != null ? p.getDistance(starboardGatePosition) : null);
            } else {
                distances.add(null);
            }
            break;
        case Line:
            Util.Pair<Mark, Mark> posLine = getPortAndStarboardMarks(t, w);
            if (posLine.getA() != null && posLine.getB() != null) {
                Position portLinePosition = race.getOrCreateTrack(posLine.getA()).getEstimatedPosition(t, false);
                Position starboardLinePosition = race.getOrCreateTrack(posLine.getB()).getEstimatedPosition(t, false);
                distances.add((portLinePosition != null && starboardLinePosition != null) ? p.getDistanceToLine(
                        portLinePosition, starboardLinePosition) : portLinePosition != null ? p
                        .getDistance(portLinePosition) : starboardLinePosition != null ? p
                        .getDistance(portLinePosition) : null);
            }
            break;
        case Offset:
            // TODO
            break;
        case None:
            break;
        default:
            break;
        }
        if (singleMark) {
            Position markPosition = race.getOrCreateTrack(w.getMarks().iterator().next())
                    .getEstimatedPosition(t, false);
            distances.add(markPosition != null ? p.getDistance(markPosition) : null);
        }
        return distances;
    }

    /**
     * @return all possible ways to pass a waypoint, described as a position and a bearing. The line out of those two
     *         must be crossed.
     */
    private Iterable<Util.Pair<Position, Bearing>> getCrossingInformation(Waypoint w, TimePoint t) {
        List<Util.Pair<Position, Bearing>> result = new ArrayList<>();
        PassingInstruction instruction = passingInstructions.get(w);
        if (instruction == PassingInstruction.Line) {
            Util.Pair<Mark, Mark> marks = getPortAndStarboardMarks(t, w);
            Position portPosition = null;
            Bearing b = null;
            Mark portMark = marks.getA();
            Mark starBoardMark = marks.getB();
            if (portMark != null && starBoardMark != null) {
                portPosition = race.getOrCreateTrack(portMark).getEstimatedPosition(t, false);
                Position starboardPosition = race.getOrCreateTrack(starBoardMark).getEstimatedPosition(t, false);
                if (portPosition != null && starboardPosition != null) {
                    b = portPosition.getBearingGreatCircle(starboardPosition);
                    result.add(new Util.Pair<Position, Bearing>(portPosition, b));
                }
            }
        } else if (instruction == PassingInstruction.Gate) {
            Position before = race.getApproximatePosition(race.getTrackedLegFinishingAt(w).getLeg().getFrom(), t);
            Position after = race.getApproximatePosition(race.getTrackedLegStartingAt(w).getLeg().getTo(), t);
            Util.Pair<Mark, Mark> pos = getPortAndStarboardMarks(t, w);
            Mark portMark = pos.getA();
            if (portMark != null) {
                Position portPosition = race.getOrCreateTrack(portMark).getEstimatedPosition(t, false);
                Bearing crossingPort = before.getBearingGreatCircle(portPosition).middle(
                        after.getBearingGreatCircle(portPosition));
                result.add(new Util.Pair<Position, Bearing>(portPosition, crossingPort));
            }
            Mark starboardMark = pos.getB();
            if (starboardMark != null) {
                Position starboardPosition = race.getOrCreateTrack(starboardMark).getEstimatedPosition(t, false);
                Bearing crossingStarboard = before.getBearingGreatCircle(starboardPosition).middle(
                        after.getBearingGreatCircle(starboardPosition));
                result.add(new Util.Pair<Position, Bearing>(starboardPosition, crossingStarboard));
            }
        } else {
            Bearing b = null;
            Position p;
            if (instruction == PassingInstruction.FixedBearing) {
                b = w.getFixedBearing();
            } else {
                // TODO Mark at start or end of race
                Bearing before = race.getTrackedLegFinishingAt(w).getLegBearing(t);
                Bearing after = race.getTrackedLegStartingAt(w).getLegBearing(t);
                if (before != null && after != null) {
                    b = before.middle(after.reverse());
                }
            }
            p = race.getOrCreateTrack(w.getMarks().iterator().next()).getEstimatedPosition(t, false);
            result.add(new Util.Pair<Position, Bearing>(p, b));
        }
        return result;
    }

    /**
     * @return the marks of a waypoint with two marks in the order port, starboard (when approching the waypoint from
     *         the direction of the waypoint beforehand.
     */
    private Util.Pair<Mark, Mark> getPortAndStarboardMarks(TimePoint t, Waypoint w) {
        List<Position> markPositions = new ArrayList<Position>();
        for (Mark mark : w.getMarks()) {
            final Position estimatedMarkPosition = race.getOrCreateTrack(mark).getEstimatedPosition(t, /* extrapolate */ false);
            if (estimatedMarkPosition == null) {
                return new Util.Pair<Mark, Mark>(null, null);
            }
            markPositions.add(estimatedMarkPosition);
        }
        final List<Leg> legs = race.getRace().getCourse().getLegs();
        final int indexOfWaypoint = race.getRace().getCourse().getIndexOfWaypoint(w);
        if (indexOfWaypoint < 0) {
            return new Util.Pair<Mark, Mark>(null, null);
        }
        final boolean isStartLine = indexOfWaypoint == 0;
        final Bearing legDeterminingDirectionBearing = race.getTrackedLeg(
                legs.get(isStartLine ? 0 : indexOfWaypoint - 1)).getLegBearing(t);
        if (legDeterminingDirectionBearing == null) {
            return new Util.Pair<Mark, Mark>(null, null);
        }
        Distance crossTrackErrorOfMark0OnLineFromMark1ToNextWaypoint = markPositions.get(0).crossTrackError(
                markPositions.get(1), legDeterminingDirectionBearing);
        final Mark starboardMarkWhileApproachingLine;
        final Mark portMarkWhileApproachingLine;
        if (crossTrackErrorOfMark0OnLineFromMark1ToNextWaypoint.getMeters() < 0) {
            portMarkWhileApproachingLine = Util.get(w.getMarks(), 0);
            starboardMarkWhileApproachingLine = Util.get(w.getMarks(), 1);
        } else {
            portMarkWhileApproachingLine = Util.get(w.getMarks(), 1);
            starboardMarkWhileApproachingLine = Util.get(w.getMarks(), 0);
        }
        return new Util.Pair<Mark, Mark>(portMarkWhileApproachingLine, starboardMarkWhileApproachingLine);
    }

}
