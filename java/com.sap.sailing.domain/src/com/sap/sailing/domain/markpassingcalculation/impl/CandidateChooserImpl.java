package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TimedComparator;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * The standard implementation of {@link CandidateChooser}. A graph is created, with each {@link Candidate} as a
 * vertices, all between two proxy Candidates, <code>start</code> and <code>end</code> using {@link Edge}s. These are
 * only created if the both the waypoints and the the timepoints are in chronological order and the distance-based
 * estimation is good enough. They are saved in <code>allEdges</code>, a map in which every candidate is the key to a
 * list of all the edges that start at this candidate. The shortest path between the proxy-Candidates is the most likely
 * sequence of {@link MarkPassing}s. Every time new candidates arrive, the start time of the race is checked. If it has
 * changed, the proxy start and all edges containing it are updated.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateChooserImpl implements CandidateChooser {
    /**
     * Earlier finish mark passings are to be preferred over later ones if they otherwise seem equally likely. While the
     * {@link #getProbabilityOfActualDistanceGivenGreatCircleDistance(Distance, Distance, double)} method should usually
     * assign an equal probability of 1.0 for edges whose distance is in the range of 1.0 and
     * {@link #MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH} times the leg length, for the finishing
     * leg the candidates that require more distance than the minimum distance required receive an increasing penalty.
     * The maximum penalty for finishing candidates that have required
     * {@link #MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH} times the leg distance is expressed by
     * this constant.
     */
    private static final double PENALTY_FOR_LATEST_FINISH_PASSING = 0.95;

    /**
     * Distance ratios of actual distance traveled and leg length above this threshold will receive
     * penalties on their probability. Ratios below 1.0 receive the ratio as the penalty.
     * See {@link #getDistanceEstimationBasedProbability(Competitor, Candidate, Candidate, Distance)}.
     */
    private static final double MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH = 2.0;

    /**
     * Start mark passings will be considered this much before the actual race start. The race start
     * as identified by {@link TrackedRace#getStartOfRace()} is therefore {@link #EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME}
     * after {@link #raceStartTime}.
     */
    private static final Duration EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME = Duration.ONE_SECOND.times(30);
    
    /**
     * The duration after which a start mark passing's probability is considered only 50%. A perfect start mark
     * passing happening exactly at the race start time has time-wise probability of 1.0. Another delay of this much
     * lets the probability drop to 1/3, and so on.
     */
    private static final Duration DELAY_AFTER_WHICH_PROBABILITY_OF_START_HALVES = Duration.ONE_MINUTE.times(5);
    
    /**
     * In order to save the expensive distance calculation we may try to rank down an edge based on an
     * outrageously low speed the competitor would have been sailing at, based on the known distance between
     * the waypoints and the time taken between the waypoints as provided by the candidates. Anything above
     * this constant is assumed to be reasonable and gets a probability of 1 (no "penalty"). As the speed
     * drops below this value, the ratio of the actual speed estimate and this constant defined the probability
     * which then is less than 1 and therefore constitutes a penalty. Should the result be below the
     * {@link Edge#getPenaltyForSkipping() skip limit} then it is not necessary to calculate the distance
     * actually sailed, saving a lot of computational effort.<p>
     * 
     * With the current selection of 1kt and a skip probability of 0.1 any speed estimated below 0.1kt will
     * lead to the edge being discarded.
     */
    private static final Speed MINIMUM_REASONABLE_SPEED = new KnotSpeedImpl(3);
    
    /**
     * Candidate filtering will be used for exclude Candidates which are very close from time perspective
     * and match certain criteria. See {@link addCandidates(Competitor c, Iterable<Candidate> newCandidates)} for details 
     */
    private static final Duration CANDIDATE_FILTER_TIME_WINDOW = Duration.ONE_SECOND.times(10);

    private static final Speed MAXIMUM_REASONABLE_SPEED = GPSFixTrack.DEFAULT_MAX_SPEED_FOR_SMOOTHING;

    private static final double MINIMUM_PROBABILITY = Edge.getPenaltyForSkipping();

    private static final Logger logger = Logger.getLogger(CandidateChooserImpl.class.getName());

    private Map<Competitor, Map<Waypoint, MarkPassing>> currentMarkPasses = new HashMap<>();
    
    /**
     * Methods operating on this collection and the collections embedded in it must be {@code synchronized} in order to
     * avoid overlapping operations. This will generally not limit concurrency further than usual, except for the
     * start-up phase where a background thread may be spawned by the constructor in case the
     * {@link MarkPassingCalculator#MarkPassingCalculator(DynamicTrackedRace, boolean, boolean)} constructor is
     * invoked with the {@code waitForInitialMarkPassingCalculation} parameter set to {@code false}. In this
     * case, mark passing calculation will be launched in the background and will not be waited for. This then
     * needs to be synchronized with the dynamic (re-)calculations triggered by fixes and other data popping in.
     */
    private Map<Competitor, Map<Candidate, Set<Edge>>> allEdges = new HashMap<>();
    
    private Map<Competitor, Set<Candidate>> candidates = new HashMap<>();
    private Map<Competitor, NavigableSet<Candidate>> fixedPassings = new HashMap<>();
    private ConcurrentHashMap<Competitor, Integer> suppressedPassings = new ConcurrentHashMap<>();
    
    /**
     * Set to {@link #EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME} milliseconds before the actual race start,
     * in case the actual start of race is known; {@code null} otherwise.
     */
    private TimePoint raceStartTime;
    private final WaypointPositionAndDistanceCache waypointPositionAndDistanceCache;
    
    /**
     * An artificial proxy candidate that comes before the start mark passing. Its time point is set to
     * {@link #EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME} milliseconds before the race start time or <code>null</code>
     * in case the race start time is not known.
     */
    private final CandidateWithSettableTime start;
    private final CandidateWithSettableWaypointIndex end;
    private final DynamicTrackedRace race;
    
    /**
     * Most data structures in this candidate chooser are keyed by {@link Competitor} objects. When a method iterates
     * over or manipulates such a per-competitor structure it must obtain the corresponding lock from here and
     * acquire the read/write lock respectively. See {@link #getCompetitorLock}. The map is initialized in the
     * constructor and populated for all the race's competitors already. From there on it is used in read-only
     * mode, so no synchronization is necessary.
     */
    private final HashMap<Competitor, NamedReentrantReadWriteLock> perCompetitorLocks;

    public CandidateChooserImpl(DynamicTrackedRace race) {
        this.perCompetitorLocks = new HashMap<>();
        this.race = race;
        waypointPositionAndDistanceCache = new WaypointPositionAndDistanceCache(race, Duration.ONE_MINUTE);
        final TimePoint startOfRaceWithoutInference = race.getStartOfRace(/* inferred */ false);
        raceStartTime = startOfRaceWithoutInference != null ? startOfRaceWithoutInference.
                minus(EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME) : null;
        start = new CandidateWithSettableTime(/* Index */0, raceStartTime, /* Probability */1, /* Waypoint */null);
        end = new CandidateWithSettableWaypointIndex(race.getRace().getCourse().getNumberOfWaypoints() + 1, /* TimePoint */null,
                /* Probability */1, /* Waypoint */null);
        candidates = new HashMap<>();
        List<Candidate> startAndEnd = Arrays.asList(start, end);
        for (Competitor c : race.getRace().getCompetitors()) {
            perCompetitorLocks.put(c, createCompetitorLock(c));
            candidates.put(c, Collections.synchronizedSet(new TreeSet<Candidate>()));
            final HashMap<Waypoint, MarkPassing> currentMarkPassesForCompetitor = new HashMap<Waypoint, MarkPassing>();
            currentMarkPasses.put(c, currentMarkPassesForCompetitor);
            // in case the tracked race already has mark passings, e.g., from another mark passing calculator,
            // ensure consistency of the currentMarkPasses map with the TrackedRace:
            for (final Waypoint w : race.getRace().getCourse().getWaypoints()) {
                final MarkPassing mp = race.getMarkPassing(c, w);
                if (mp != null) {
                    currentMarkPassesForCompetitor.put(w, mp);
                }
            }
            TreeSet<Candidate> fixedPasses = new TreeSet<Candidate>(new Comparator<Candidate>() {
                @Override
                public int compare(Candidate o1, Candidate o2) {
                    final int result;
                    if (o1 == null) {
                        if (o2 == null) {
                            result = 0;
                        } else {
                            result = -1;
                        }
                    } else if (o2 == null) {
                        result = 1;
                    } else {
                        result = o1.getOneBasedIndexOfWaypoint() - o2.getOneBasedIndexOfWaypoint();
                    }
                    return result;
                }
            });
            fixedPassings.put(c, fixedPasses);
            allEdges.put(c, new HashMap<Candidate, Set<Edge>>());
            fixedPasses.addAll(startAndEnd);
            addCandidates(c, startAndEnd);
            
        }
    }
    
    private NamedReentrantReadWriteLock createCompetitorLock(Competitor c) {
        return new NamedReentrantReadWriteLock("Competitor lock for "+c+" in candidate chooser "+this, /* fair */ false);
    }

    @Override
    public void calculateMarkPassDeltas(Competitor c, Iterable<Candidate> newCans, Iterable<Candidate> oldCans) {
        final TimePoint startOfRace = race.getStartOfRace(/* inference */ false);
        if (startOfRace != null) {
            final boolean startTimeUpdated;
            synchronized (this) { // protect raceStartTime check and update
                if (raceStartTime == null || !startOfRace.minus(EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME).equals(raceStartTime)) {
                    raceStartTime = startOfRace.minus(EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME);
                    startTimeUpdated = true;
                } else {
                    startTimeUpdated = false;
                }
            }
            if (startTimeUpdated) {
                List<Candidate> startList = new ArrayList<>();
                startList.add(start);
                for (Competitor com : candidates.keySet()) {
                    removeCandidates(com, startList);
                }
                start.setTimePoint(raceStartTime);
                for (Competitor com : allEdges.keySet()) {
                    addCandidates(com, startList);
                }
            }
        }
        removeCandidates(c, oldCans);
        addCandidates(c, newCans);
        findShortestPath(c);
    }

    @Override
    public void removeWaypoints(Iterable<Waypoint> waypoints) {
        for (Competitor c : currentMarkPasses.keySet()) {
            for (Waypoint w : waypoints) {
                currentMarkPasses.get(c).remove(w);
            }
        }
    }
    
    @Override
    public void updateEndProxyNodeWaypointIndex() {
        end.setOneBasedWaypointIndex(race.getRace().getCourse().getNumberOfWaypoints()+1);
    }

    @Override
    public void setFixedPassing(Competitor c, Integer zeroBasedIndexOfWaypoint, TimePoint t) {
        LockUtil.lockForWrite(perCompetitorLocks.get(c));
        try {
            Candidate fixedCan = new CandidateImpl(zeroBasedIndexOfWaypoint + 1, t, 1, Util.get(race.getRace().getCourse().getWaypoints(), zeroBasedIndexOfWaypoint));
            NavigableSet<Candidate> fixed = fixedPassings.get(c);
            if (fixed != null) { // can only set the mark passing if the competitor is still part of this race
                if (!fixed.add(fixedCan)) {
                    Candidate old = fixed.ceiling(fixedCan);
                    fixed.remove(old);
                    removeCandidates(c, Arrays.asList(old));
                    fixed.add(fixedCan);
                }
                addCandidates(c, Arrays.asList(fixedCan));
                findShortestPath(c);
            }
        } finally {
            LockUtil.unlockAfterWrite(perCompetitorLocks.get(c));
        }
    }

    @Override
    public void removeFixedPassing(Competitor c, Integer zeroBasedIndexOfWaypoint) {
        LockUtil.lockForWrite(perCompetitorLocks.get(c));
        try {
            Candidate toRemove = null;
            for (Candidate can : fixedPassings.get(c)) {
                if (can.getOneBasedIndexOfWaypoint() - 1 == zeroBasedIndexOfWaypoint) {
                    toRemove = can;
                    break;
                }
            }
            if (toRemove != null) {
                fixedPassings.get(c).remove(toRemove);
                removeCandidates(c, Arrays.asList(toRemove));
                findShortestPath(c);
            }
        } finally {
            LockUtil.unlockAfterWrite(perCompetitorLocks.get(c));
        }
    }

    @Override
    public void suppressMarkPassings(Competitor c, Integer zeroBasedIndexOfWaypoint) {
        suppressedPassings.put(c, zeroBasedIndexOfWaypoint);
        findShortestPath(c);
    }

    @Override
    public void stopSuppressingMarkPassings(Competitor c) {
        suppressedPassings.remove(c);
        findShortestPath(c);
    }

    private void createNewEdges(Competitor c, Iterable<Candidate> newCandidates) {
        assert perCompetitorLocks.get(c).isWriteLocked();
        final Boolean isGateStart = race.isGateStart();
        Map<Candidate, Set<Edge>> edges = allEdges.get(c);
        for (Candidate newCan : newCandidates) {
            final Set<Candidate> competitorCandidates = candidates.get(c); // TODO bug4221: use a filtered view of candidates
            synchronized (competitorCandidates) {
                for (Candidate oldCan : competitorCandidates) {
                    final Candidate early;
                    final Candidate late;
                    if (oldCan.getOneBasedIndexOfWaypoint() < newCan.getOneBasedIndexOfWaypoint()) {
                        early = oldCan;
                        late = newCan;
                    } else if (oldCan.getOneBasedIndexOfWaypoint() > newCan.getOneBasedIndexOfWaypoint()) {
                        late = oldCan;
                        early = newCan;
                    } else {
                        continue; // don't create edge from/to same waypoint
                    }
    
                    final Supplier<Double> estimatedDistanceProbabilitySupplier;
                    final double estimatedDistanceProbability;
                    final double startTimingProbability;
                    // when null, don't create an edge; when a valid distance, use this as the totalGreatCircleDistance value
                    final Distance ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping;
                    if (early == start) {
                        // An edge starting at the start proxy node. If the late candidate is for a start mark passing,
                        // determine a probability not based on distance traveled but based on the
                        // time difference between scheduled start time and candidate's time point. If the "late" candidate
                        // is not for the start mark/line, meaning that mark passings including the actual start are
                        // skipped, as usual use getDistanceEstimationBasedProbability assuming a start mark passing at
                        // the race's start time.
                        if (isGateStart == Boolean.TRUE || start.getTimePoint() == null) { // TODO for gate start read gate timing and scale probability accordingly
                            startTimingProbability = 1; // no start time point known; all candidate time points equally likely
                            estimatedDistanceProbability = 1.0;
                            ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping = Distance.NULL;
                            estimatedDistanceProbabilitySupplier = null; // can't tell distance sailed either because we don't know the start time
                        } else {
                            // no gate start and we know the race start time
                            if (late.getWaypoint() != null && late.getWaypoint() == race.getRace().getCourse().getFirstWaypoint()) {
                                // no skips; going from the start proxy node to a candidate for the start mark passing;
                                // calculate the probability for the start being the start given its timing and multiply
                                // with the estimation for the distance-based probability:
                                final Duration timeGapBetweenStartOfRaceAndCandidateTimePoint = early.getTimePoint()
                                        .plus(EARLY_STARTS_CONSIDERED_THIS_MUCH_BEFORE_STARTTIME).until(late.getTimePoint()).abs();
                                // Being DELAY_AFTER_WHICH_PROBABILITY_OF_START_HALVES off means a probability of 1/2; being twice this time
                                // off means 1/3, and so on
                                startTimingProbability = DELAY_AFTER_WHICH_PROBABILITY_OF_START_HALVES.divide(
                                        DELAY_AFTER_WHICH_PROBABILITY_OF_START_HALVES.plus(
                                                timeGapBetweenStartOfRaceAndCandidateTimePoint));
                                estimatedDistanceProbability = 1.0;
                                ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping = Distance.NULL;
                                estimatedDistanceProbabilitySupplier = null;
                            } else {
                                startTimingProbability = 1; // can't really tell how well the start time was matched when
                                                            // we don't have a start candidate
                                if (late == end) {
                                    estimatedDistanceProbability = 1.0;
                                    ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping = Distance.NULL;
                                    estimatedDistanceProbabilitySupplier = null;
                                } else {
                                    estimatedDistanceProbability = 0.0;
                                    ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping = getIgnoreDueToTimingInducedEstimatedSpeeds(c, early, late);
                                    estimatedDistanceProbabilitySupplier = ()->getDistanceEstimationBasedProbability(c, early, late, ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping);
                                }
                            }
                        }
                    } else {
                        startTimingProbability = 1; // no penalty for any start time difference because this edge doesn't cover a start
                        if (late == end) {
                            // final edge; we don't know anything about distances for the end proxy node
                            estimatedDistanceProbability = 1.0;
                            ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping = Distance.NULL;
                            estimatedDistanceProbabilitySupplier = null;
                        } else {
                            estimatedDistanceProbability = 0.0;
                            ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping = getIgnoreDueToTimingInducedEstimatedSpeeds(c, early, late);
                            estimatedDistanceProbabilitySupplier = ()->getDistanceEstimationBasedProbability(c, early, late, ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping);
                        }
                    }
                    // If one of the candidates is fixed, the edge is always created unless they travel backwards in time.
                    // Otherwise the edge is only created if the distance estimation, which can be calculated as long as the
                    // candidates are not the proxy and or start is close enough to the actual distance sailed.
                    final NavigableSet<Candidate> fixed = fixedPassings.get(c);
                    // TODO this comparison does not exactly implement the condition "if distance is more likely than skipping"
                    if (travelingForwardInTimeOrUnknown(early, late) &&
                            (fixed.contains(early) || fixed.contains(late) || ignoreEdgeDueToProbabilityLowerThanMinimumForSkipping != null)) {
                        final Edge edge;
                        if (estimatedDistanceProbabilitySupplier != null) {
                            edge = new Edge(early, late,
                                        ()->startTimingProbability * estimatedDistanceProbabilitySupplier.get(), race.getRace().getCourse().getNumberOfWaypoints());
                        } else {
                            edge = new Edge(early, late,
                                    startTimingProbability * estimatedDistanceProbability, race.getRace().getCourse().getNumberOfWaypoints());
                        }
                        addEdge(edges, edge);
                    }
                }
            }
        }
    }

    private boolean travelingForwardInTimeOrUnknown(Candidate early, Candidate late) {
        return early.getTimePoint() == null || late.getTimePoint() == null || early.getTimePoint().before(late.getTimePoint());
    }

    private void addEdge(Map<Candidate, Set<Edge>> edges, Edge e) {
        logger.finest(()->"Adding "+ e.toString());
        Set<Edge> edgeSet = edges.get(e.getStart());
        if (edgeSet == null) {
            edgeSet = new HashSet<>();
            edges.put(e.getStart(), edgeSet);
        }
        edgeSet.add(e); // FIXME what about edges that should replace an edge between the same two candidates? Will those edges somehow be removed?
    }

    /**
     * Calculates the most likely series of {@link MarkPassings} using the edges in {@link allEdges}. These are saved in
     * {@link #currentMarkPasses} and the {@link DynamicTrackedRace} is
     * {@link DynamicTrackedRace#updateMarkPassings(Competitor, Iterable) notified}.<p>
     * 
     * The algorithm works out optimal solutions between fixed mark passings. By default, start and end proxy
     * candidates are the only fixed elements. If more fixed elements are provided, the algorithm solves the
     * optimization problem separately for each segment and concatenates the solutions.
     */
    private void findShortestPath(Competitor c) {
        LockUtil.lockForWrite(perCompetitorLocks.get(c));
        try {
            Map<Candidate, Set<Edge>> allCompetitorEdges = allEdges.get(c);
            SortedSet<Candidate> mostLikelyCandidates = new TreeSet<>();
            NavigableSet<Candidate> fixedPasses = fixedPassings.get(c);
            Candidate startOfFixedInterval = fixedPasses.first();
            Candidate endOfFixedInterval = fixedPasses.higher(startOfFixedInterval);
            Integer zeroBasedIndexOfWaypoint = suppressedPassings.get(c);
            Integer oneBasedIndexOfSuppressedWaypoint = zeroBasedIndexOfWaypoint != null ? zeroBasedIndexOfWaypoint + 1 : end
                    .getOneBasedIndexOfWaypoint();
            while (endOfFixedInterval != null) {
                if (oneBasedIndexOfSuppressedWaypoint <= endOfFixedInterval.getOneBasedIndexOfWaypoint()) {
                    endOfFixedInterval = end;
                }
                NavigableSet<Util.Pair<Edge, Double>> currentEdgesMoreLikelyFirst = new TreeSet<>(new Comparator<Util.Pair<Edge, Double>>() {
                    @Override
                    public int compare(Util.Pair<Edge, Double> o1, Util.Pair<Edge, Double> o2) {
                        int result = o2.getB().compareTo(o1.getB());
                        return result != 0 ? result : o1.getA().compareTo(o2.getA());
                    }
                });
                Map<Candidate, Util.Pair<Candidate, Double>> candidateWithParentAndHighestTotalProbability = new HashMap<>();
                int indexOfEndOfFixedInterval = endOfFixedInterval.getOneBasedIndexOfWaypoint();
    
                boolean endFound = false;
                currentEdgesMoreLikelyFirst.add(new Util.Pair<Edge, Double>(new Edge(
                        new CandidateImpl(-1, null, /* estimated distance probability */ 1, null), startOfFixedInterval,
                        ()->1.0, race.getRace().getCourse().getNumberOfWaypoints()), 1.0));
                while (!endFound) {
                    Util.Pair<Edge, Double> mostLikelyEdgeWithProbability = currentEdgesMoreLikelyFirst.pollFirst();
                    if (mostLikelyEdgeWithProbability == null) {
                        endFound = true;
                    } else {
                        Edge currentMostLikelyEdge = mostLikelyEdgeWithProbability.getA();
                        Double currentHighestProbability = mostLikelyEdgeWithProbability.getB();
                        // If the shortest path to this candidate is already known the new edge is not added.
                        if (!candidateWithParentAndHighestTotalProbability.containsKey(currentMostLikelyEdge.getEnd())) {
                            // The most likely edge taking us to currentMostLikelyEdge.getEnd() is found. Remember it.
                            candidateWithParentAndHighestTotalProbability.put(currentMostLikelyEdge.getEnd(), new Util.Pair<Candidate, Double>(
                                    currentMostLikelyEdge.getStart(), currentHighestProbability));
                            logger.finest(()->"Added "+ currentMostLikelyEdge + " as most likely edge for " + c);
                            endFound = currentMostLikelyEdge.getEnd() == endOfFixedInterval;
                            if (!endFound) {
                                // the end of the segment was not yet found; add edges leading away from
                                // currentMostLikelyEdge.getEnd(), multiplying up their probabilities with the probability
                                // of reaching currentMostLikelyEdge.getEnd()
                                Set<Edge> edgesForNewCandidate = allCompetitorEdges.get(currentMostLikelyEdge.getEnd());
                                if (edgesForNewCandidate != null) {
                                    for (Edge e : edgesForNewCandidate) {
                                        int oneBasedIndexOfEndOfEdge = e.getEnd().getOneBasedIndexOfWaypoint();
                                        // only add edge if it stays within the current segment, not exceeding
                                        // the next fixed mark passing
                                        if (oneBasedIndexOfEndOfEdge <= indexOfEndOfFixedInterval
                                                && (oneBasedIndexOfEndOfEdge < oneBasedIndexOfSuppressedWaypoint || e.getEnd() == end)) {
                                            currentEdgesMoreLikelyFirst.add(new Util.Pair<Edge, Double>(e, currentHighestProbability * e.getProbability()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                final Pair<Candidate, Double> bestCandidateAndProbabilityForEndOfFixedInterval = candidateWithParentAndHighestTotalProbability.get(endOfFixedInterval);
                Candidate marker = bestCandidateAndProbabilityForEndOfFixedInterval == null ? null : bestCandidateAndProbabilityForEndOfFixedInterval.getA();
                while (marker != null && marker.getOneBasedIndexOfWaypoint() > 0) {
                    mostLikelyCandidates.add(marker);
                    marker = candidateWithParentAndHighestTotalProbability.get(marker).getA();
                }
                startOfFixedInterval = endOfFixedInterval;
                endOfFixedInterval = fixedPasses.higher(endOfFixedInterval);
            }
            boolean changed = false;
            Map<Waypoint, MarkPassing> currentPasses = currentMarkPasses.get(c);
            if (currentPasses.size() != mostLikelyCandidates.size()) {
                changed = true;
            } else {
                for (Candidate can : mostLikelyCandidates) {
                    MarkPassing currentPassing = currentPasses.get(can.getWaypoint());
                    if (currentPassing == null || currentPassing.getTimePoint().compareTo(can.getTimePoint()) != 0) {
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                currentPasses.clear();
                List<MarkPassing> newMarkPassings = new ArrayList<>();
                for (Candidate can : mostLikelyCandidates) {
                    if (can != start && can != end) {
                        MarkPassingImpl newMarkPassing = new MarkPassingImpl(can.getTimePoint(), can.getWaypoint(), c);
                        currentPasses.put(newMarkPassing.getWaypoint(), newMarkPassing);
                        newMarkPassings.add(newMarkPassing);
                    }
                }
                logger.fine(()->"Updating MarkPasses for " + c + " in case "+race.getRace().getName());
                race.updateMarkPassings(c, newMarkPassings);
            }
        } finally {
            LockUtil.unlockAfterWrite(perCompetitorLocks.get(c));
        }
    }

    /**
     * The timing of the candidates is put in relation to the distance between the waypoints they connect. This implies
     * a rough speed estimate. If this is completely "out of whack" (way too low or way too high), we can assume that if
     * ultimately carrying out the potentially expensive (for long-distance races) calculation of the distance sailed,
     * we would only find that the distance-based probability would be below the {@link #MINIMUM_PROBABILITY} threshold
     * such that that edge would be ignored.
     * 
     * @return {@code null} if the edge between {@code c1} and {@code c2} shall be ignored; the
     *         {@link #getMinimumTotalGreatCircleDistanceBetweenWaypoints(Waypoint, Waypoint, TimePoint) straight-line
     *         distance between the corresponding waypoints} otherwise. This distance can be used for a subsequent call
     *         to {@link #getDistanceEstimationBasedProbability(Competitor, Candidate, Candidate, Distance)} so it doesn't have
     *         to be re-calculated there.
     */
    private Distance getIgnoreDueToTimingInducedEstimatedSpeeds(Competitor c, Candidate c1, Candidate c2) {
        final boolean ignore;
        assert c1.getOneBasedIndexOfWaypoint() < c2.getOneBasedIndexOfWaypoint();
        assert c2 != end;
        final TimePoint middleOfc1Andc2 = new MillisecondsTimePoint(c1.getTimePoint().plus(c2.getTimePoint().asMillis()).asMillis() / 2);
        Waypoint first = getFirstWaypoint(c1);
        final Waypoint second = c2.getWaypoint();
        final Distance totalGreatCircleDistance = getMinimumTotalGreatCircleDistanceBetweenWaypoints(first, second, middleOfc1Andc2);
        if (totalGreatCircleDistance == null) {
            ignore = true; // no distance known; cannot tell, so ignore the edge
        } else {
            // Computing the distance traveled can be quite expensive, especially for candidates very far apart.
            // As a quick approximation let's look at how long the time between the candidates was and relate that to the minimum distance
            // between the waypoints. This leads to a speed estimation; if we take the minimum distance times two, we
            // get an upper bound for a reasonable distance sailed between the waypoints and therefore an estimation
            // for the maximum speed at which the competitor would have had to sail:
            Speed estimatedMaxSpeed = totalGreatCircleDistance.scale(2).inTime(c1.getTimePoint().until(c2.getTimePoint()));
            final double estimatedMinSpeedBasedProbability = Math.max(0, estimatedMaxSpeed.divide(MINIMUM_REASONABLE_SPEED));
            final double estimatedMaxSpeedBasedProbability = Math.max(0, MAXIMUM_REASONABLE_SPEED.divide(estimatedMaxSpeed));
            final double estimatedSpeedBasedProbabilityMinimum = Math.min(estimatedMaxSpeedBasedProbability, estimatedMinSpeedBasedProbability);
            ignore = estimatedSpeedBasedProbabilityMinimum < MINIMUM_PROBABILITY;
        }
        return ignore ? null : totalGreatCircleDistance;
    }

    /**
     * If the candidate has no waypoint associated, return the course's first waypoint; otherwise, return the candidates
     * {@link Candidate#getWaypoint() waypoint}.
     */
    private Waypoint getFirstWaypoint(Candidate candidate) {
        Waypoint first;
        if (candidate.getOneBasedIndexOfWaypoint() == 0) {
            first = race.getRace().getCourse().getFirstWaypoint();
        } else {
            first = candidate.getWaypoint();
        }
        return first;
    }

    /**
     * The distance between waypoints is used to estimate the distance that should be covered between these two
     * candidates. This estimation is then compared to the distance actually sailed. A distance smaller than the
     * estimation is (aside from a small tolerance) impossible, a distance larger get increasingly unlikely.
     * 
     * @param totalGreatCircleDistance
     *            the result of a previous
     *            {@link #getMinimumTotalGreatCircleDistanceBetweenWaypoints(Waypoint, Waypoint, TimePoint)} call for
     *            the two waypoints of {@code c1} and {@code c2}
     */
    private double getDistanceEstimationBasedProbability(Competitor c, Candidate c1, Candidate c2, Distance totalGreatCircleDistance) {
        final double result;
        assert c1.getOneBasedIndexOfWaypoint() < c2.getOneBasedIndexOfWaypoint();
        assert c2 != end;
        if (totalGreatCircleDistance == null) {
            result = 0.0; // no distance known; cannot tell
        } else {
            // Computing the distance traveled can be quite expensive, especially for candidates very far apart.
            // Let's first look at how long the time between the candidates was and relate that to the minimum distance
            // between the waypoints. This leads to a speed estimation; if we take the minimum distance times two, we
            // get an upper bound for a reasonable distance sailed between the waypoints and therefore an estimation
            // for the maximum speed at which the competitor would have had to sail:
            final Distance actualDistanceTraveled = race.getTrack(c).getDistanceTraveled(c1.getTimePoint(), c2.getTimePoint());
            final double probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength =
                    c2.getWaypoint() == race.getRace().getCourse().getLastWaypoint() ? PENALTY_FOR_LATEST_FINISH_PASSING : 1.0;
            result = getProbabilityOfActualDistanceGivenGreatCircleDistance(totalGreatCircleDistance, actualDistanceTraveled,
                    probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength);
        }
        return result;
    }

    /**
     * Based on a direct great-circle distance between waypoints and an actual distance sailed, determines how likely it
     * is that this distance sailed could have happened between those waypoints. For a reaching leg, this would be based
     * on a straight comparison of the numbers. However, with upwind and downwind legs and boats not going from mark to
     * mark on a great circle segment, distances sailed will exceed the great line circle distances.
     * <p>
     * 
     * A smaller distance than great circle from mark to mark is getting the more unlikely the shorter the distance is,
     * somewhere between the distance estimated and twice that is likely, and anything greater than that gradually
     * becomes unlikely.
     * <p>
     * 
     * Finishing legs are a special case. Here, we'd like to prefer an earlier candidate over a later one as long as the
     * earlier one still leads to a "reasonable" distance sailed, particularly if two such candidates are otherwise
     * equally highly likely. Therefore, this method accepts a parameter
     * {@code probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength} that configures a slight "slope" in
     * the interval that for non-finishing legs receives a constant probability of 1.0. This slope will give 1.0 for the
     * shortest possible distance and slightly less for the longest distance that for non-finishing legs would still
     * result in 1.0. The probabilities of even greater distances then starts contiguously at the end value of that
     * slope.
     * 
     * @return a number between 0 and 1 with 1 representing a "fair chance" that the actual distance sailed could have
     *         been sailed for the given great circle distance; 1 is returned for actual distances being in the range of
     *         1..2 times the great circle distance. Actual distances outside this interval reduce probability linearly
     *         for smaller distances (gradient 0.5) and varies with the one over the ratio for distances that exceed
     *         twice the great circle distance.
     */
    private double getProbabilityOfActualDistanceGivenGreatCircleDistance(Distance totalGreatCircleDistance, Distance actualDistanceTraveled,
            double probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength) {
        final double result;
        final double ratio = actualDistanceTraveled.getMeters() / totalGreatCircleDistance.getMeters();
        // A smaller distance than great circle from mark to mark is very unlikely, somewhere between the distance
        // estimated and double that is likely and anything greater than that gradually becomes unlikely
        if (ratio <= 1) {
            result = ratio;
        } else if (ratio <= MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH) {
            result = 1 - (1-probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength)*(ratio-1)/(MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH-1);
        } else {
            // start at probability probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength for ratio==MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH
            result = probabilityForMaxReasonableRatioBetweenDistanceTraveledAndLegLength/(ratio-MAX_REASONABLE_RATIO_BETWEEN_DISTANCE_TRAVELED_AND_LEG_LENGTH + 1.);
        }
        return result;
    }

    private Distance getMinimumTotalGreatCircleDistanceBetweenWaypoints(Waypoint first, final Waypoint second, final TimePoint timePoint) {
        Distance totalGreatCircleDistance = new MeterDistance(0);
        boolean legsAreBetweenCandidates = false;
        for (TrackedLeg leg : race.getTrackedLegs()) {
            Waypoint from = leg.getLeg().getFrom();
            if (from == second) {
                break;
            }
            if (from == first) {
                legsAreBetweenCandidates = true;
            }
            if (legsAreBetweenCandidates) {
                final Distance minimumDistanceToNextWaypoint = waypointPositionAndDistanceCache.getMinimumDistance(from, leg.getLeg().getTo(), timePoint);
                if (minimumDistanceToNextWaypoint == null) {
                    totalGreatCircleDistance = null;
                    break;
                } else {
                    // subtract twice the typical error margin of the position fixes of the marks, assuming that the leg could have been
                    // a little shorter in fact:
                    totalGreatCircleDistance = totalGreatCircleDistance.add(minimumDistanceToNextWaypoint).add(GPSFix.TYPICAL_HDOP.scale(-2));
                }
            }
        }
        return totalGreatCircleDistance;
    }

    /**
     * New candidates will be added. The list of incoming Candidates will be filtered.
     * TODO bug4221 Assumption that candidates are sorted by time
     * 
     */
    private void addCandidates(final Competitor competitor, final Iterable<Candidate> newCandidates) {
        // bug 4221 - filter candidates
        
        // --------- BLOCK 1 ----------- 
        // from TG only
        // filtered by waypoints
        /**/
        List<Candidate> filteredCandidates = new ArrayList<Candidate>();
        int sizeBefore = Util.size(newCandidates);

//        if (sizeBefore > 2) {
            Hashtable<Waypoint, ArrayList<Candidate>> organizedList = new Hashtable<Waypoint, ArrayList<Candidate>>();
            logger.finest("candidate count before candidate filtering: " + sizeBefore);
            
            // add all new candidates
            for (Candidate can : newCandidates) {
                Waypoint wp = can.getWaypoint();
                filteredCandidates.add(can);

                if (!(can instanceof DistanceCandidateImpl)) {
                    // not for XTE Candidates (yet)
                } else {
                    // filtering for Distance Candidates
                    ArrayList<Candidate> canWpList = organizedList.get(wp);
                    if (null == canWpList) {
                        canWpList = new ArrayList<Candidate>();
                        organizedList.put(wp, canWpList);
                    }
                    canWpList.add(can);
                }
            }
            // --------------------------------------------------------
            // list of organized Candidates ready for analyzing
            Set<Waypoint> keys = organizedList.keySet();
            Candidate lastCan = null;
            int deleteCnt = 0;
            for(Waypoint key: keys) {
                int wpCnt = 0;
                int innerDelCnt = 0;
                ArrayList<Candidate> canWpList = organizedList.get(key);
                Collections.sort(canWpList, TimedComparator.INSTANCE);
                wpCnt = canWpList.size();
                for (int j=0; j<canWpList.size(); j++) {
                    Candidate can = canWpList.get(j);
                    if (lastCan != null) {
//                        if (lastCan[0].getTimePoint().until(distCan.getTimePoint()).compareTo(CANDIDATE_FILTER_TIME_WINDOW) < 0) {
                        if (can.getTimePoint().until( lastCan.getTimePoint()).compareTo( CANDIDATE_FILTER_TIME_WINDOW ) < 0) {
                            // close enough
                            if (can.getProbability() > lastCan.getProbability()) {
                                // better than last one - delete last One
                                if (filteredCandidates.contains(lastCan)) {
                                    filteredCandidates.remove(lastCan);
                                }
                                innerDelCnt++;
                                deleteCnt++;
                            } else {
                                // delete actual one
                                innerDelCnt++;
                                deleteCnt++;
                                if (filteredCandidates.contains(can)) {
                                    filteredCandidates.remove(can);
                                }
                                continue;
                            }
                        }
                    }
                    lastCan = can;
                }
                logger.finest("count of entries in "+ key + " is: "+ wpCnt + " (toDelete:" + innerDelCnt + ")");
            }
            logger.finest(" would delete: " + deleteCnt);
            
            logger.warning("before: " + sizeBefore + " after candidate filtering: " + filteredCandidates.size() + " for " + competitor.getName());

//        }
                
        /**/
        //------ ENDE BLOCK 1 ---------------
        
        //------ BLOCK 2 ---------------
        //
        // there can be candidates for different waypoints, therefore not working this way
        /*
        List<Candidate> filteredCandidates = new ArrayList<>();
        int size = Util.size(newCandidates);
        
        if (size > 0) { //2) {
            logger.finest("candidate count before candidate filtering: " + size);
            
            Util.addAll(newCandidates, filteredCandidates);
            // list of organized Candidates ready for analyzing
            final int deleteCnt[] = new int[1];
            
            Collections.sort(filteredCandidates, TimedComparator.INSTANCE);
            
            final int innerDelCnt[] = new int[1];
            final Candidate lastCan[] = new Candidate[1];
            filteredCandidates.stream().filter(c->c instanceof DistanceCandidateImpl).forEach(distCan->{
                if (lastCan[0] != null) {
                    if (lastCan[0].getTimePoint().until(distCan.getTimePoint()).compareTo(CANDIDATE_FILTER_TIME_WINDOW) < 0) {
                        // close enough
                        innerDelCnt[0]++;
                        deleteCnt[0]++;
                        if (distCan.getProbability() > lastCan[0].getProbability()) {
                            // TODO maybe only remove if the lesser probability is small enough
                            // better than last one - delete last One
                            filteredCandidates.remove(lastCan[0]);
                            lastCan[0] = distCan;
                        } else {
                            // delete actual one
                            filteredCandidates.remove(distCan);
                        }
                    } else {
                        lastCan[0] = distCan;
                    }
                } else {
                    lastCan[0] = distCan;
                }
            });
//                logger.finest("count of entries in "+ waypointAndCandidateSet.getKey() + " is: "+ wpCnt + " (toDelete:" + innerDelCnt + ")");
            
            //logger.finest(" would delete: " + deleteCnt[0]);
            logger.warning("before: " + sizeBefore + " after candidate filtering: " + filteredCandidates.size() + " for " + competitor.getName());
        }
        //------ END BLOCK 2 ---------------
        */

        LockUtil.lockForWrite(perCompetitorLocks.get(competitor));
        try {
            
            // add new non filtered candidates
            for (Candidate can : filteredCandidates) {
                candidates.get(competitor).add(can);
            }
            
            // TODO here would be a good place to apply candidate filtering; let createNewEdges consider only those candidates passing the filter
           
            // new arriving candidates require a complete new calculation
            // - filtering candidates
            // - create edges
            
            createNewEdges(competitor, filteredCandidates);
        } finally {
            LockUtil.unlockAfterWrite(perCompetitorLocks.get(competitor));
        }
    }

    private synchronized void removeCandidates(Competitor c, Iterable<Candidate> wrongCandidates) {
        LockUtil.lockForWrite(perCompetitorLocks.get(c));
        try {
            for (Candidate can : wrongCandidates) {
                logger.finest(()->"Removing all edges containing " + can + "of "+ c);
                candidates.get(c).remove(can);
                Map<Candidate, Set<Edge>> edges = allEdges.get(c);
                edges.remove(can);
                for (Set<Edge> set : edges.values()) {
                    for (Iterator<Edge> i = set.iterator(); i.hasNext();) {
                        final Edge e = i.next();
                        if (e.getStart() == can || e.getEnd() == can) {
                            i.remove();
                        }
                    }
                }
            }
        } finally {
            LockUtil.unlockAfterWrite(perCompetitorLocks.get(c));
        }
    }

    private static class CandidateWithSettableTime extends CandidateImpl {
        private static final long serialVersionUID = -1792983349299883266L;
        private TimePoint variableTimePoint;
        
        public CandidateWithSettableTime(int oneBasedIndexOfWaypoint, TimePoint p, double distanceProbability, Waypoint w) {
            super(oneBasedIndexOfWaypoint, /* time point */ null, distanceProbability, w);
            this.variableTimePoint = p;
        }

        public void setTimePoint(TimePoint t) {
            variableTimePoint = t;
        }
        
        @Override
        public TimePoint getTimePoint() {
            return variableTimePoint;
        }
    }

    private static class CandidateWithSettableWaypointIndex extends CandidateImpl {
        private static final long serialVersionUID = 5868551535609781722L;
        private int variableOneBasedWaypointIndex;
        
        public CandidateWithSettableWaypointIndex(int oneBasedIndexOfWaypoint, TimePoint p, double distanceProbability, Waypoint w) {
            super(/* oneBasedIndexOfWaypoint */ -1, p, distanceProbability, w);
            this.variableOneBasedWaypointIndex = oneBasedIndexOfWaypoint;
        }

        public void setOneBasedWaypointIndex(int oneBasedWaypointIndex) {
            this.variableOneBasedWaypointIndex = oneBasedWaypointIndex;
        }
        
        @Override
        public int getOneBasedIndexOfWaypoint() {
            return variableOneBasedWaypointIndex;
        }
    }
}
