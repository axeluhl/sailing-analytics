package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * The standard implemantation of {@link CandidateFinder}. There are two kinds of {@link Candidate}s. First of all,
 * every time a competitor passes the crossing-bearing of a waypoint, a candidate is created using linear interpolation
 * to estimate the exact time the bearing was crossed. Secondly, all local distance minima to a waypoint are candidates.
 * The probability of a candidate depends on its distance, whether it is on the right side and if it passes in the right
 * direction of its waypoint.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateFinderImpl implements CandidateFinder {
    // The higher this is, the closer the fixes have to be to waypoint to become a Candidate
    private final int STRICTNESS_OF_DISTANCE_BASED_PROBABILITY = 7;

    // All of the penalties are multiplied onto the probability of a Candidate. A value of 0 excludes Candidates that do
    // not fit, a value of 1 imposes no penalty on each criteria
    private static final double PENALTY_FOR_WRONG_SIDE = 0.8;
    
    /**
     * The penalty factor multiplied with a candidate's probability in case the competitor passes the line of an XTE
     * candidate with passing instructions other than {@link PassingInstruction#Line} in the wrong direction, e.g.,
     * passing a single mark that is to be passed to port in the wrong direction.
     */
    private static final double PENALTY_FOR_WRONG_DIRECTION = 0.7;
    
    /**
     * The penalty factor multiplied with a candidate's probability in case the competitor passes an XTE candidate for a
     * {@link PassingInstruction#Line line} in the wrong direction, e.g., passing a start line the wrong way.
     */
    private static final double PENALTY_FOR_LINE_PASSED_IN_WRONG_DIRECTION = 0.3;
    
    /**
     * Normally, for each distance candidate there should also be a proper XTE candidate that also tells about the
     * direction in which the boat crosses the line. XTE candidates are more precise and get a penalty in case the competitor
     * crosses the virtual line in the wrong direction: {@link #PENALTY_FOR_WRONG_DIRECTION}. The penalty a competitor should
     * get for not triggering a proper XTE candidate and only producing a candidate by getting near a mark and then
     * moving away again should be penalized at least as badly as {@link #PENALTY_FOR_WRONG_DIRECTION}.
     */
    private static final double PENALTY_FOR_DISTANCE_CANDIDATES = 0.9 * PENALTY_FOR_WRONG_DIRECTION;
    
    private static final double WORST_PENALTY_FOR_OTHER_COMPETITORS_BEING_FAR_FROM_START = 0.1;
    private static final double NUMBER_OF_HULL_LENGTHS_DISTANCE_FROM_START_AT_WHICH_WORST_PENALTY_APPLIES = 10;

    private static final Logger logger = Logger.getLogger(CandidateFinderImpl.class.getName());

    
    private Map<Competitor, LinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>> distanceCache = new LinkedHashMap<>();
    private Map<Competitor, LinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>> xteCache = new LinkedHashMap<>();

    private Map<Competitor, Map<Waypoint, Map<List<GPSFix>, Candidate>>> xteCandidates = new HashMap<>();
    private Map<Competitor, Map<Waypoint, Map<GPSFix, Candidate>>> distanceCandidates = new HashMap<>();
    private final DynamicTrackedRace race;
    private final double penaltyForSkipping = Edge.getPenaltyForSkipping();
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
        for (Competitor c : raceDefinition.getCompetitors()) {
            xteCache.put(c, new LimitedLinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>(25));
            distanceCache.put(c, new LimitedLinkedHashMap<GPSFix, Map<Waypoint, List<Distance>>>(25));
            xteCandidates.put(c, new HashMap<Waypoint, Map<List<GPSFix>, Candidate>>());
            distanceCandidates.put(c, new HashMap<Waypoint, Map<GPSFix, Candidate>>());
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
        synchronized (xteCandidates) {
            xteCandidates.get(c).clear();
        }
        synchronized (distanceCandidates) {
            distanceCandidates.get(c).clear();
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
                TimeRange timePoints = race.getOrCreateTrack(fixes.getKey()).getEstimatedPositionTimePeriodAffectedBy(fix);
                TimePoint newStart = timePoints.from();
                TimePoint newEnd = timePoints.to();
                start = start == null || start.after(newStart) ? newStart : start;
                end = end == null || end.before(newEnd) ? newEnd : end;
            }
        }
        for (Competitor c : race.getRace().getCompetitors()) {
            List<GPSFix> competitorFixes = new ArrayList<>();
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
            GPSFix comFix = track.getFirstFixAtOrAfter(start);
            if (comFix != null) {
                if (end != null) {
                    while (comFix != null && !comFix.getTimePoint().after(end)) {
                        competitorFixes.add(comFix);
                        distanceCache.get(c).remove(comFix);
                        xteCache.get(c).remove(comFix);
                        comFix = track.getFirstFixAfter(comFix.getTimePoint());
                    }
                } else {
                    while (comFix != null) {
                        competitorFixes.add(comFix);
                        distanceCache.get(c).remove(comFix);
                        xteCache.get(c).remove(comFix);
                        comFix = track.getFirstFixAfter(comFix.getTimePoint());
                    }
                }
            }
            if (!competitorFixes.isEmpty()) {
                affectedFixes.put(c, competitorFixes);
            }
        }
        return affectedFixes;
    }

    @Override
    public Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes) {
        List<Candidate> newCans = new ArrayList<>();
        List<Candidate> wrongCans = new ArrayList<>();
        Course course = race.getRace().getCourse();
        course.lockForRead();
        try {
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
        } finally {
            course.unlockAfterRead();
        }
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
        course.lockForRead();
        try {
            for (Waypoint w : course.getWaypoints()) {
                if (course.getIndexOfWaypoint(w) > indexOfChange - 2) {
                    changedWaypoints.add(w);
                }
            }
            for (Competitor c : race.getRace().getCompetitors()) {
                List<Candidate> badCans = new ArrayList<>();
                List<Candidate> newCans = new ArrayList<>();
                for (Waypoint w : changedWaypoints) {
                    Map<List<GPSFix>, Candidate> xteCans = getXteCandidates(c, w);
                    badCans.addAll(xteCans.values());
                    xteCans.clear();
                    Map<GPSFix, Candidate> distanceCans = getDistanceCandidates(c, w);
                    badCans.addAll(distanceCans.values());
                    distanceCans.clear();
                }
                Set<GPSFix> allFixes = getAllFixes(c);
                newCans.addAll(checkForDistanceCandidateChanges(c, allFixes, changedWaypoints).getA());
                newCans.addAll(checkForXTECandidatesChanges(c, allFixes, changedWaypoints).getA());
                result.put(c, new Util.Pair<List<Candidate>, List<Candidate>>(newCans, badCans));
            }
        } finally {
            course.unlockAfterRead();
        }

        return result;
    }

    @Override
    public Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> updateWaypoints(
            Iterable<Waypoint> addedWaypoints, Iterable<Waypoint> removedWaypoints, Integer smallestIndex) {
        Map<Competitor, List<Candidate>> removedWaypointCandidates = removeWaypoints(removedWaypoints);
        Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> newAndUpdatedCandidates = invalidateAfterCourseChange(smallestIndex);
        for (Entry<Competitor, List<Candidate>> entry : removedWaypointCandidates.entrySet()) {
            newAndUpdatedCandidates.get(entry.getKey()).getB().addAll(entry.getValue());
        }
        return newAndUpdatedCandidates;
    }

    private Map<Competitor, List<Candidate>> removeWaypoints(Iterable<Waypoint> waypoints) {
        Map<Competitor, List<Candidate>> result = new HashMap<>();
        for (Competitor c : race.getRace().getCompetitors()) {
            result.put(c, new ArrayList<Candidate>());
        }
        for (Waypoint w : waypoints) {
            passingInstructions.remove(w);
            for (Entry<Competitor, List<Candidate>> entry : result.entrySet()) {
                Competitor c = entry.getKey();
                List<Candidate> badCans = entry.getValue();
                badCans.addAll(getXteCandidates(c, w).values());
                synchronized (xteCandidates) {
                    xteCandidates.get(c).remove(w);
                }
                badCans.addAll(getDistanceCandidates(c, w).values());
                synchronized (distanceCandidates) {
                    distanceCandidates.get(c).remove(w);
                }
            }
        }
        return result;
    }

    private Map<GPSFix, Candidate> getDistanceCandidates(Competitor c, Waypoint w) {
        synchronized (distanceCandidates) {
            Map<GPSFix, Candidate> result = distanceCandidates.get(c).get(w);
            if (result == null) {
                result = new HashMap<>();
                distanceCandidates.get(c).put(w, result);
            }
            return result;
        }
    }

    private Map<List<GPSFix>, Candidate> getXteCandidates(Competitor c, Waypoint w) {
        synchronized (xteCandidates) {
            Map<List<GPSFix>, Candidate> result = xteCandidates.get(c).get(w);
            if (result == null) {
                result = new HashMap<>();
                xteCandidates.get(c).put(w, result);
            }
            return result;
        }
    }

    private PassingInstruction determinePassingInstructions(Waypoint w) {
        final Waypoint firstWaypoint = race.getRace().getCourse().getFirstWaypoint();
        final Waypoint lastWaypoint = race.getRace().getCourse().getLastWaypoint();
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
                    instruction = PassingInstruction.Single_Unknown;
                } else {
                    instruction = PassingInstruction.None;
                }
            }
        }
        return instruction;
    }

    private Set<GPSFix> getAllFixes(Competitor c) {
        Set<GPSFix> fixes = new TreeSet<GPSFix>(comp);
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(c);
        try {
            track.lockForRead();
            for (GPSFix fix : track.getFixes(
                    race.getStartOfTracking() == null ? TimePoint.BeginningOfTime : race.getStartOfTracking(),
                    /* fromInclusive */true,
                    race.getEndOfTracking() == null ? TimePoint.EndOfTime : race.getEndOfTracking(),
                    /* toInclusive */true)) {
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
                    double sidePenalty = 1;
                    Distance distance = null;
                    Double startProbabilityBasedOnOtherCompetitors = null;
                    double onCorrectSideOfWaypoint = 0.8;
                    List<Distance> waypointDistances = fixDistances.get(w);
                    List<Distance> waypointDistancesBefore = fixDistancesBefore.get(w);
                    List<Distance> waypointDistancesAfter = fixDistancesAfter.get(w);
                    // due to course changes, waypoints that exist in the waypoints collection may not have a
                    // corresponding
                    // key in passingInstructions' key set which is the basis for the waypoints for which
                    // getDistances(...)
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
                                        // FIXME why not generate the candidate here where we have all information at hand?
                                        final double newOnCorrectSideOfWaypointPenalty = getSidePenalty(w, p, t, portMark);
                                        newProbability *= newOnCorrectSideOfWaypointPenalty * PENALTY_FOR_DISTANCE_CANDIDATES;
                                        final Double newStartProbabilityBasedOnOtherCompetitors;
                                        if (isStartWaypoint(w)) {
                                            newStartProbabilityBasedOnOtherCompetitors = getProbabilityForStartCandidateBasedOnOtherCompetitorsBehavior(c, t);
                                            if (newStartProbabilityBasedOnOtherCompetitors != null) {
                                                newProbability *= newStartProbabilityBasedOnOtherCompetitors;
                                            }
                                        } else {
                                            newStartProbabilityBasedOnOtherCompetitors = null;
                                        }
                                        if (newProbability > penaltyForSkipping
                                                && (probability == null || newProbability > probability)) {
                                            isCan = true;
                                            probability = newProbability;
                                            sidePenalty = newOnCorrectSideOfWaypointPenalty;
                                            onCorrectSideOfWaypoint = newOnCorrectSideOfWaypointPenalty;
                                            distance = dis;
                                            startProbabilityBasedOnOtherCompetitors = newStartProbabilityBasedOnOtherCompetitors;
                                        }
                                    }
                                }
                            }
                            portMark = false;
                        }
                        oldCan = getDistanceCandidates(c, w).get(fix);
                        if (oldCan != null) {
                            wasCan = true;
                        }
                        if (!wasCan && isCan) {
                            Candidate newCan = new DistanceCandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1,
                                    t, probability, startProbabilityBasedOnOtherCompetitors, w, sidePenalty, distance);
                            getDistanceCandidates(c, w).put(fix, newCan);
                            result.getA().add(newCan);
                            logger.finest("Added distance" + newCan.toString() + "for " + c);
                        } else if (wasCan && !isCan) {
                            getDistanceCandidates(c, w).remove(fix);
                            result.getB().add(oldCan);
                        } else if (wasCan && isCan && oldCan.getProbability() != probability) {
                            Candidate newCan = new DistanceCandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1,
                                    t, probability, startProbabilityBasedOnOtherCompetitors, w, onCorrectSideOfWaypoint, distance);
                            getDistanceCandidates(c, w).put(fix, newCan);
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
            Course course = race.getRace().getCourse();
            course.lockForRead();
            try {
                for (Waypoint w : course.getWaypoints()) {
                    List<Distance> distances = calculateDistance(fix.getPosition(), w, fix.getTimePoint());
                    result.put(w, distances);
                }
            } finally {
                course.unlockAfterRead();
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
                Map<List<GPSFix>, Candidate> waypointCandidates = getXteCandidates(c, w);
                for (List<GPSFix> fixPair : waypointCandidates.keySet()) {
                    if (fixPair.contains(fix)) {
                        oldCandidates.add(fixPair);
                    }
                }
                List<Distance> wayPointXTEs = xtes.get(w);
                int size = wayPointXTEs == null ? 0 : wayPointXTEs.size();
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
            Course course = race.getRace().getCourse();
            course.lockForRead();
            try {
                for (Waypoint w : course.getWaypoints()) {
                    List<Distance> distances = new ArrayList<>();
                    result.put(w, distances);
                    for (Util.Pair<Position, Bearing> crossingInfo : getCrossingInformation(w, t)) {
                        if (crossingInfo.getA() != null && crossingInfo.getB() != null) {
                            distances.add(p.crossTrackError(crossingInfo.getA(), crossingInfo.getB()));
                        }
                    }
                }
            } finally {
                course.unlockAfterRead();
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

    /**
     * {@code xte1} and {@code xte2} are expected to have different signums or both be 0. The method
     * interpolates between the time points {@code t1} and {@code t2} according to where the XTE is
     * assumed to have crossed 0. The candidate is then generated for this interpolated time point.
     */
    private Candidate createCandidate(Competitor c, double xte1, double xte2, TimePoint t1, TimePoint t2, Waypoint w,
            Boolean portMark) {
        final long differenceInMillis = t2.asMillis() - t1.asMillis();
        final double ratio = (Math.abs(xte1) / (Math.abs(xte1) + Math.abs(xte2)));
        final TimePoint t = t1.plus((long) (differenceInMillis * ratio));
        final Position p = race.getTrack(c).getEstimatedPosition(t, false);
        final List<Distance> distances = calculateDistance(p, w, t);
        final Distance d = portMark ? distances.get(0) : distances.get(1);
        final double sidePenalty = getSidePenalty(w, p, t, portMark);
        double probability = getDistanceBasedProbability(w, t, d) * sidePenalty;
        final Double passesInTheRightDirectionProbability = passesInTheRightDirection(w, xte1, xte2, portMark);
        // null would mean "unknown"; no penalty for those cases
        probability = passesInTheRightDirectionProbability == null ? probability : probability * passesInTheRightDirectionProbability;
        final Double startProbabilityBasedOnOtherCompetitors;
        if (isStartWaypoint(w)) {
            // add a penalty for a start candidate if we don't know the start time, it's not a gate start and
            // at time point t the other competitors are largely not even close to the start waypoint;
            // this will make start candidates much more likely if many other competitors are also very close to the
            // start, and it will help ruling out those candidates where someone is practicing a start just for themselves
            startProbabilityBasedOnOtherCompetitors = getProbabilityForStartCandidateBasedOnOtherCompetitorsBehavior(c, t);
            if (startProbabilityBasedOnOtherCompetitors != null) {
                probability *= startProbabilityBasedOnOtherCompetitors;
            }
        } else {
            startProbabilityBasedOnOtherCompetitors = null;
        }
        return new XTECandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(w) + 1, t, probability,
                startProbabilityBasedOnOtherCompetitors, w, sidePenalty, passesInTheRightDirectionProbability);
    }
    
    public static class AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine {
        private final Distance absoluteGeometricDistance;
        private final Distance signedProjectedDistance;
        public AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine(Distance absoluteGeometricDistance,
                Distance signedProjectedDistance) {
            super();
            this.absoluteGeometricDistance = absoluteGeometricDistance;
            this.signedProjectedDistance = signedProjectedDistance;
        }
        public Distance getAbsoluteGeometricDistance() {
            return absoluteGeometricDistance;
        }
        public Distance getSignedProjectedDistance() {
            return signedProjectedDistance;
        }
        @Override
        public String toString() {
            return "AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine [absoluteGeometricDistance="
                    + absoluteGeometricDistance + ", signedProjectedDistance=" + signedProjectedDistance + "]";
        }
    }

    /**
     * For a fixed line start it is very likely that all competitors pass the start waypoint at about the same time, or
     * that only a few competitors lag behind, e.g., because of start time penalties. This requires everyone to at least
     * be pretty close to the start waypoint at the start time, of if we're looking at a competitor starting late, the
     * field would at least have little variance and be consistently on the course side of the start line.
     * <p>
     * 
     * Therefore, a start mark passing candidate is very likely when a large share of competitors is very close to the
     * start line. If the other competitors are not very close to the start line, a start may still be likely if the
     * others are on the course side and have similar distance to the start line, whereas it becomes pretty unlikely
     * when everyone is sailing around anywhere on the course or behind the line but not being in close proximity to the
     * line and not having similar distances to the start line while being on course side.
     * 
     * <ul>
     * <li>check distance variance of other competitors; low variance could mean they started jointly</li>
     * <li>for start *line*, consider the side on which the other competitors are; don't rule out a late start if other
     * competitors have low distance variance and are on course side of the line</li>
     * <li>generally consider distance of other competitors to line</li>
     * <li>dampen "outliers" such as trackers forgotten in harbor or trackers not currently sending (80% of competitors
     * that look like they are starting seems like a good guess)</li>
     * </ul>
     * 
     * Approach: compute signed distances to start line (positive meaning on course side). Sort by absolute distance and
     * compute a {@link #weight(double) weighted} average where the boats farthest away are considered less relevant than
     * the ones closer, leading to a natural outlier removal.<p>
     * 
     * For the special case of a late starter, consider the weighted variance of the signed distance from the start line.
     * A low variance with a positive average (meaning on course side) indicates that everyone else may have started around the
     * same time, making this a probable late starter's candidate. Therefore, low variance with positive average shall increase
     * a probability that was low because of a large weighted average distance.
     * 
     * @param t
     *            the time point at which to consider the other competitors behavior relative to the start waypoint
     * @return {@code null} if the {@link #race} has a gate start or the start time is known; a probability between 0..1
     *         (inclusive) where 0 means that based on all but {@code c}'s relation to the start line it seems
     *         completely unlikely that a candidate for {@code c} at time {@code t} could have been a start candidate; 1
     *         meaning that based on the other competitors' relation to the start line at time point {@code t} is seems
     *         a fact that this must have been the start.
     * 
     */
    private Double getProbabilityForStartCandidateBasedOnOtherCompetitorsBehavior(Competitor c, TimePoint t) {
        final Double result;
        if (race.getStartOfRace(/* inferred */ false) == null && race.isGateStart() != Boolean.TRUE) {
            final Waypoint start = race.getRace().getCourse().getFirstWaypoint();
            final Iterable<Pair<Position, Bearing>> crossingInformationForStart = getCrossingInformation(start, t);
            if (start == null) {
                result = 1.0;
            } else {
                // if the start waypoint's passing instructions are unknown, assume it's a line
                final boolean startIsLine = getPassingInstructions(start) == PassingInstruction.Line;
                final List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors = new ArrayList<>();
                for (final Competitor otherCompetitor : race.getRace().getCompetitors()) {
                    if (otherCompetitor != c) {
                        final DynamicGPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(otherCompetitor);
                        if (track != null) {
                            final Position estimatedPositionAtT = track.getEstimatedPosition(t, /* extrapolate */ true);
                            final Distance otherCompetitorsDistanceToStartAtT = getMinDistanceOrNull(calculateDistance(estimatedPositionAtT, start, t));
                            if (otherCompetitorsDistanceToStartAtT != null) {
                                final Distance crossTrackError;
                                if (startIsLine) {
                                    Pair<Position, Bearing> crossingInformationForStartLine = crossingInformationForStart.iterator().next();
                                    crossTrackError = estimatedPositionAtT.crossTrackError(crossingInformationForStartLine.getA(),
                                            crossingInformationForStartLine.getB());
                                } else {
                                    crossTrackError = null;
                                }
                                distancesToStartLineOfOtherCompetitors.add(new
                                        AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine(otherCompetitorsDistanceToStartAtT, crossTrackError));
                            }
                        }
                    }
                }
                result = getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(distancesToStartLineOfOtherCompetitors, startIsLine);
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * The method computes two probabilities of the candidate being a start: one based on the other competitors largely
     * being close to the start line, meaning a regular start where the competitor starts roughly at the same time as
     * everybody else; the other probability computed is based on the other competitors largely being in a similar
     * distance to the start line on the course side, suggesting that the competitor is starting late, but mostly
     * everybody else did start earlier and around the same time. The probability returned is the probability of
     * the start being the one <em>or</em> the other scenario which mathematically is represented by using the inverted
     * probabilities of the two start options, multiplying them and inverting the resulting probability.<p>
     * 
     * Access needs to be protected to satisfy Maven-based test cases in com.sap.sailing.domain.test bundle which is not a fragment
     */
    protected Double getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(
            final List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors, boolean startIsLine) {
        final Double result;
        // sort by the absolute distance to start line
        // boats within one hull length of the start line are great indicators that we're at the start:
        final Distance hullLength = race.getRace().getBoatClass().getHullLength();
        final Distance weightedAverageAbsoluteDistance;
        if (!distancesToStartLineOfOtherCompetitors.isEmpty()) {
            Collections.sort(distancesToStartLineOfOtherCompetitors, (a, b)->a.getAbsoluteGeometricDistance().compareTo(b.getAbsoluteGeometricDistance()));
            Distance weightedAbsoluteDistanceSum = Distance.NULL;
            int i=0;
            double weightSum = 0;
            for (final AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine d : distancesToStartLineOfOtherCompetitors) {
                final double weight = weight(((double) i)/distancesToStartLineOfOtherCompetitors.size());
                final Distance weightedDistance = d.getAbsoluteGeometricDistance().scale(weight);
                weightedAbsoluteDistanceSum = weightedAbsoluteDistanceSum.add(weightedDistance);
                weightSum += weight;
                i++;
            }
            weightedAverageAbsoluteDistance = weightedAbsoluteDistanceSum.scale(1. / weightSum);
            final double probabilityOfStartWithOthers = Math.max(WORST_PENALTY_FOR_OTHER_COMPETITORS_BEING_FAR_FROM_START, Math.min(1.0,
                    (1.-(1.-WORST_PENALTY_FOR_OTHER_COMPETITORS_BEING_FAR_FROM_START)/NUMBER_OF_HULL_LENGTHS_DISTANCE_FROM_START_AT_WHICH_WORST_PENALTY_APPLIES
                            * (weightedAverageAbsoluteDistance.divide(hullLength)-1))));
            if (startIsLine) {
                // Now look at the variance, particularly on course side: low variance may mean we are looking at a late starter
                // sort such that the boats with the least value (greatest negative amount) comes first and the last ~20% are largely ignored
                Collections.sort(distancesToStartLineOfOtherCompetitors, (a, b)->a.getSignedProjectedDistance().compareTo(b.getSignedProjectedDistance()));
                Distance weightedSignedDistanceSum = Distance.NULL;
                weightSum = 0;
                i=0;
                for (final AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine d : distancesToStartLineOfOtherCompetitors) {
                    final double weight = weight(((double) i)/distancesToStartLineOfOtherCompetitors.size());
                    weightedSignedDistanceSum  = weightedSignedDistanceSum.add(d.getSignedProjectedDistance().scale(
                            weight));
                    weightSum += weight;
                    i++;
                }
                final Distance weightedAverageSignedDistance = weightedSignedDistanceSum.scale(1. / weightSum);
                i=0;
                double signedDistanceVarianceSumInSquareMeters = 0;
                weightSum = 0;
                for (final AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine d : distancesToStartLineOfOtherCompetitors) {
                    final double differenceFromWeightedAverageInMeters = d.getSignedProjectedDistance().getMeters() - weightedAverageSignedDistance.getMeters();
                    final double weight = weight(((double) i)/distancesToStartLineOfOtherCompetitors.size());
                    signedDistanceVarianceSumInSquareMeters += weight*differenceFromWeightedAverageInMeters*differenceFromWeightedAverageInMeters;
                    weightSum += weight;
                    i++;
                }
                final double weightedVarianceInSquareMeters = signedDistanceVarianceSumInSquareMeters / weightSum;
                final double weightedVarianceRatioToSquaredHullLength = weightedVarianceInSquareMeters / hullLength.getMeters() / hullLength.getMeters();
                final double probabilityOfLateStart = Math.max(WORST_PENALTY_FOR_OTHER_COMPETITORS_BEING_FAR_FROM_START, Math.min(1.0,
                        (1.-(1.-WORST_PENALTY_FOR_OTHER_COMPETITORS_BEING_FAR_FROM_START)/NUMBER_OF_HULL_LENGTHS_DISTANCE_FROM_START_AT_WHICH_WORST_PENALTY_APPLIES
                                * (weightedVarianceRatioToSquaredHullLength-1))));
                result = 1. - (1.-probabilityOfLateStart) * (1.-probabilityOfStartWithOthers);
            } else {
                result = probabilityOfStartWithOthers;
            }
        } else {
            // no distance for any other competitor from the start line was available
            result = 1.0;
        }
        return result;
    }

    /**
     * Idea: sort and cut off the worst 20%. We played with weight functions for averaging the distances such that after sorting them
     * in increasing order the smaller ones get greater weight and the "outlier" ones towards the end of the sorted set receive small
     * weights. Still, if even the "best" (least) distances are far away, the result will be a low probability.
     * A function that could be helpful is this: -atan(20*(x-0.8)) + Pi/2)/Pi. The 0.8 represents the 80% starting from which the
     * weight declines sharply. The factor of 20 represents how sharp the decline is. x is between 0 and 1, representing the start
     * and the end of the sorted distance collection, respectively. Outputs are between 0 and 1, representing the weight to assign
     * to a distance while averaging.
     */
    private double weight(double relativePositionInDistanceCollectionInIncreasingOrder) {
        final double SHARPNESS_OF_DECLINE = 100;
        final double CENTER_OF_DECLINE = 0.8;
        return (-Math.atan(SHARPNESS_OF_DECLINE*(relativePositionInDistanceCollectionInIncreasingOrder-CENTER_OF_DECLINE))+Math.PI/2) / Math.PI;
    }

    private Distance getMinDistanceOrNull(List<Distance> distances) {
        Distance result = null;
        for (Distance d : distances) {
            if (d != null && (result == null || result.compareTo(d) > 0)) {
                result = d;
            }
        }
        return result;
    }

    private boolean isStartWaypoint(Waypoint w) {
        return race.getRace().getCourse().getFirstWaypoint() == w;
    }

    /**
     * Determines whether a candidate is on the correct side of a waypoint. This is defined by the crossing information.
     * The cross-track error of <code>p</code> to the crossing Position and the crossing Bearing rotated by 90deg need
     * to be negative. If the passing instructions are {@link PassingInstruction#Line}, it checks whether the boat
     * passed between the two marks.
     */
    private double getSidePenalty(Waypoint w, Position p, TimePoint t, boolean portMark) {
        boolean isOnRightSide = true;
        Distance onWrongSide = new MeterDistance(0);        
        PassingInstruction instruction = getPassingInstructions(w);
        if (instruction == PassingInstruction.Line) {
            List<Position> pos = new ArrayList<>();
            for (Mark m : w.getMarks()) {
                Position po = race.getOrCreateTrack(m).getEstimatedPosition(t, false);
                if (po == null) {
                    isOnRightSide = true;
                    break;
                }
                pos.add(po);
            }
            if (pos.size() != 2) {
                isOnRightSide = true;
            }
            Position leftMarkPos = pos.get(0);
            Position rightMarkPos = pos.get(1);
            Bearing diff1 = leftMarkPos.getBearingGreatCircle(p)
                    .getDifferenceTo(leftMarkPos.getBearingGreatCircle(rightMarkPos));
            Bearing diff2 = rightMarkPos.getBearingGreatCircle(p)
                    .getDifferenceTo(rightMarkPos.getBearingGreatCircle(leftMarkPos));
            if (Math.abs(diff1.getDegrees()) > 90 || Math.abs(diff2.getDegrees()) > 90) {
                isOnRightSide =  false;
                Distance leftDistance = p.getDistance(leftMarkPos);
                Distance rightDistance = p.getDistance(rightMarkPos);
                onWrongSide = leftDistance.getMeters() < rightDistance.getMeters() ? leftDistance : rightDistance;
            }
        } else {
            Mark m = null;
            if (instruction == PassingInstruction.Single_Unknown || instruction == PassingInstruction.Port || instruction == PassingInstruction.Starboard
                    || instruction == PassingInstruction.FixedBearing || instruction == PassingInstruction.Offset) {
                m = w.getMarks().iterator().next();
            } else if (instruction == PassingInstruction.Gate) {
                Util.Pair<Mark, Mark> pair = getPortAndStarboardMarks(t, w);
                m = portMark ? pair.getA() : pair.getB();
            }
            if (m != null) {
                Util.Pair<Position, Bearing> crossingInfo = Util.get(getCrossingInformation(w, t), portMark ? 0 : 1);
                isOnRightSide = p.crossTrackError(crossingInfo.getA(), crossingInfo.getB().add(new DegreeBearingImpl(90))).getMeters() < 0;
                if (isOnRightSide == false) {
                    onWrongSide = p.getDistance(crossingInfo.getA());
                }
            }
        }
        final double result;
        if (isOnRightSide == true) {
            result = 1;
        } else {
            //TODO There should be a constant (either the 1.5 or the -0.2) which controls how strict the the curve  is.
            result = Math.min(1.0, (1-PENALTY_FOR_WRONG_SIDE) *
                    // consider the possibility that both, mark and boat could have been GPSFix.TYPICAL_HDOP off and
                    // only start penalizing beyond this distance
                    Math.pow(1.5, -0.2 * onWrongSide.add(GPSFix.TYPICAL_HDOP.scale(-2.0)).getMeters())
                    + PENALTY_FOR_WRONG_SIDE);
        }
        return result;
    }

    /**
     * Determines whether a candidate passes a waypoint in the right direction. A probability of 1 is assigned in case
     * it does. Otherwise, depending on the passing instructions, {@link #PENALTY_FOR_WRONG_DIRECTION} or
     * {@link #PENALTY_FOR_LINE_PASSED_IN_WRONG_DIRECTION} is returned as a "probability penalty" for passing the
     * waypoint in the wrong direction. The penalty is worse if {@link PassingInstruction#Line lines} are passed in the
     * wrong direction. Can only be applied to XTE-Candidates. For marks passed on port, the cross-track error should
     * switch from positive to negative and vice versa. Lines are also from positive to negative as the cross-track
     * error to a line is always positive when approaching it from the correct side.
     * 
     * @return {@code null} in case the passing instructions aren't defined and therefore the correct direction is not
     *         known
     */
    private Double passesInTheRightDirection(Waypoint w, double xte1, double xte2, boolean portMark) {
        final Double result;
        PassingInstruction instruction = getPassingInstructions(w);
        if (instruction == PassingInstruction.Single_Unknown) {
            result = null;
        } else if (instruction == PassingInstruction.Port
                || (instruction == PassingInstruction.Gate && portMark)) {
            result = xte1 > xte2 ? 1 : PENALTY_FOR_WRONG_DIRECTION;
        } else if (instruction == PassingInstruction.Starboard || (instruction == PassingInstruction.Gate && !portMark)) {
            result = xte1 < xte2 ? 1 : PENALTY_FOR_WRONG_DIRECTION;
        } else if (instruction == PassingInstruction.Line) {
            result = xte1 > xte2 ? 1 : PENALTY_FOR_LINE_PASSED_IN_WRONG_DIRECTION;
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @return a probability based on the distance to <code>w</code> and the average leg lengths before and after.
     */
    private Double getDistanceBasedProbability(Waypoint w, TimePoint t, Distance distance) {
        final Double result;
        Distance legLength = getAverageLengthOfAdjacentLegs(t, w);
        if (legLength != null) {
            result = 1 / (STRICTNESS_OF_DISTANCE_BASED_PROBABILITY/* Raising this will make it stricter */
                    * Math.abs(distance.getMeters() / legLength.getMeters()) + 1);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @return an average of the estimated legs before and after <code>w</code>.
     */
    private Distance getAverageLengthOfAdjacentLegs(TimePoint t, Waypoint w) {
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
     * Calculates the distance from p to each mark of w at the timepoint t.
     */
    private List<Distance> calculateDistance(Position p, Waypoint w, TimePoint t) {
        final List<Distance> distances = new ArrayList<>();
        PassingInstruction instruction = getPassingInstructions(w);
        boolean singleMark = false;
        switch (instruction) {
        case Port:
        case Single_Unknown:
        case Starboard:
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
                        portLinePosition, starboardLinePosition).abs() : null);
            }
            break;
        case Offset:
            singleMark = true;
            // TODO Actually an Offset mark has two marks, only the first of which actually counts as being rounded. The
            // passing of the second mark is also of interest though.
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

    private PassingInstruction getPassingInstructions(Waypoint w) {
        final PassingInstruction result;
        if (passingInstructions.containsKey(w)) {
            result = passingInstructions.get(w);
        } else {
            result = determinePassingInstructions(w);
            passingInstructions.put(w, result);
        }
        return result;
    }

    /**
     * @return all possible ways to pass a waypoint, described as a position and a bearing. The line out of those two
     *         must be crossed.
     */
    private Iterable<Util.Pair<Position, Bearing>> getCrossingInformation(Waypoint w, TimePoint t) {
        List<Util.Pair<Position, Bearing>> result = new ArrayList<>();
        PassingInstruction instruction = getPassingInstructions(w);
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
                // TODO If the first of last waypoint is not a gate or line, this will lead to issues
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
     * @return the marks of a waypoint with two marks in the order port, starboard (when approaching the waypoint from
     *         the direction of the waypoint beforehand.
     */
    private Util.Pair<Mark, Mark> getPortAndStarboardMarks(TimePoint t, Waypoint w) {
        List<Position> markPositions = new ArrayList<Position>();
        for (Mark mark : w.getMarks()) {
            final Position estimatedMarkPosition = race.getOrCreateTrack(mark).getEstimatedPosition(t, /* extrapolate */
            false);
            if (estimatedMarkPosition == null) {
                return new Util.Pair<Mark, Mark>(null, null);
            }
            markPositions.add(estimatedMarkPosition);
        }
        if (markPositions.size() != 2){
            return new Util.Pair<Mark, Mark>(null, null);
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
