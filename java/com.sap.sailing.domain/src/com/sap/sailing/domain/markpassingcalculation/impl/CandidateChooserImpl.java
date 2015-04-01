package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
    // TODO what is the meaning of this constant?
    private static final int MILLISECONDS_BEFORE_STARTTIME = 5000;
    private final static double MINIMUM_PROBABILITY = 1 - Edge.getPenaltyForSkipping();

    private static final Logger logger = Logger.getLogger(CandidateChooserImpl.class.getName());

    private Map<Competitor, Map<Waypoint, MarkPassing>> currentMarkPasses = new HashMap<>();
    private Map<Competitor, Map<Candidate, Set<Edge>>> allEdges = new HashMap<>();
    private Map<Competitor, Set<Candidate>> candidates = new HashMap<>();
    private Map<Competitor, NavigableSet<Candidate>> fixedPassings = new HashMap<>();
    private Map<Competitor, Integer> suppressedPassings = new HashMap<>();
    private TimePoint raceStartTime;
    
    /**
     * An artificial proxy candidate that comes before the start mark passing. Its time point is set to
     * {@link #MILLISECONDS_BEFORE_STARTTIME} milliseconds before the race start time or <code>null</code>
     * in case the race start time is not known.
     */
    private final CandidateWithSettableTime start;
    private final CandidateWithSettableWaypointIndex end;
    private final DynamicTrackedRace race;

    public CandidateChooserImpl(DynamicTrackedRace race) {
        this.race = race;
        raceStartTime = race.getStartOfRace() != null ? race.getStartOfRace().minus(MILLISECONDS_BEFORE_STARTTIME) : null;
        start = new CandidateWithSettableTime(/* Index */0, raceStartTime, /* Probability */1, /* Waypoint */null);
        end = new CandidateWithSettableWaypointIndex(race.getRace().getCourse()
                .getIndexOfWaypoint(race.getRace().getCourse().getLastWaypoint()) + 2, /* TimePoint */null,
                /* Probability */1, /* Waypoint */null);
        candidates = new HashMap<>();
        List<Candidate> startAndEnd = Arrays.asList(start, end);
        for (Competitor c : race.getRace().getCompetitors()) {
            candidates.put(c, new TreeSet<Candidate>());
            currentMarkPasses.put(c, new HashMap<Waypoint, MarkPassing>());
            TreeSet<Candidate> fixedPasses = new TreeSet<Candidate>(new Comparator<Candidate>() {
                @Override
                public int compare(Candidate o1, Candidate o2) {
                    return o1.getOneBasedIndexOfWaypoint() - o2.getOneBasedIndexOfWaypoint();
                }
            });
            fixedPassings.put(c, fixedPasses);
            allEdges.put(c, new HashMap<Candidate, Set<Edge>>());
            fixedPasses.addAll(startAndEnd);
            addCandidates(c, startAndEnd);
        }
    }

    @Override
    public void calculateMarkPassDeltas(Competitor c, Iterable<Candidate> newCans, Iterable<Candidate> oldCans) {
       TimePoint startOfRace = race.getStartOfRace();
        if (startOfRace != null) {
            if (raceStartTime == null || !startOfRace.minus(MILLISECONDS_BEFORE_STARTTIME).equals(raceStartTime)) {
                raceStartTime = startOfRace.minus(MILLISECONDS_BEFORE_STARTTIME);
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
        end.setOneBasedWaypointIndex(end.getOneBasedIndexOfWaypoint()-Util.size(waypoints));
    }
    
    @Override
    public void addWaypoints(Iterable<Waypoint> waypoints) {
        end.setOneBasedWaypointIndex(end.getOneBasedIndexOfWaypoint()+Util.size(waypoints));
    }

    @Override
    public void setFixedPassing(Competitor c, Integer zeroBasedIndexOfWaypoint, TimePoint t) {
        Candidate fixedCan = new CandidateImpl(zeroBasedIndexOfWaypoint + 1, t, 1, Util.get(race.getRace().getCourse().getWaypoints(), zeroBasedIndexOfWaypoint));
        NavigableSet<Candidate> fixed = fixedPassings.get(c);
        if (!fixed.add(fixedCan)) {
            Candidate old = fixed.ceiling(fixedCan);
            fixed.remove(old);
            removeCandidates(c, Arrays.asList(old));
            fixed.add(fixedCan);
        }
        addCandidates(c, Arrays.asList(fixedCan));
        findShortestPath(c);
    }

    @Override
    public void removeFixedPassing(Competitor c, Integer zeroBasedIndexOfWaypoint) {
        Candidate toRemove = null;
        for (Candidate can : fixedPassings.get(c)) {
            if (can.getOneBasedIndexOfWaypoint() - 1 == zeroBasedIndexOfWaypoint) {
                toRemove = can;
                break;
            }
        }
        fixedPassings.get(c).remove(toRemove);
        removeCandidates(c, Arrays.asList(toRemove));
        findShortestPath(c);
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
        final Boolean isGateStart = race.isGateStart();
        Map<Candidate, Set<Edge>> edges = allEdges.get(c);
        for (Candidate newCan : newCandidates) {
            for (Candidate oldCan : candidates.get(c)) {
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

                final double estimatedDistanceProbability;
                final double startTimingProbability;
                if (early == start) {
                    // An edge starting at the start proxy node. If the late candidate is for a start mark passing,
                    // determine a probability not based on distance traveled but based on the
                    // time difference between scheduled start time and candidate's time point. If the "late" candidate
                    // is not for the start mark/line, meaning that mark passings including the actual start are
                    // skipped, as usual use getDistanceEstimationBasedProbability assuming a start mark passing at
                    // the race's start time.
                    if (isGateStart == Boolean.TRUE || start.getTimePoint() == null) { // TODO for gate start read gate timing and scale probability accordingly
                        startTimingProbability = 1; // no start time point known; all candidate time points equally likely
                        estimatedDistanceProbability = 1; // can't tell distance sailed either because we don't know the start time
                    } else {
                        // no gate start and we know the race start time
                        if (late.getWaypoint() == race.getRace().getCourse().getFirstWaypoint()) {
                            // no skips; going from the start proxy node to a candidate for the start mark passing;
                            // calculate the probability for the start being the start given its timing and multiply
                            // with the estimation for the distance-based probability:
                            final Duration timeGapBetweenStartOfRaceAndCandidateTimePoint = early.getTimePoint()
                                    .plus(MILLISECONDS_BEFORE_STARTTIME).until(late.getTimePoint());
                            // Being MILLISECONDS_BEFORE_STARTTIME off means a probability of 1/2; being twice this time
                            // off means 1/3, and so on
                            startTimingProbability = (double) MILLISECONDS_BEFORE_STARTTIME
                                    / (double) (MILLISECONDS_BEFORE_STARTTIME + Math
                                            .abs(timeGapBetweenStartOfRaceAndCandidateTimePoint.asMillis()));
                            estimatedDistanceProbability = 1;
                        } else {
                            startTimingProbability = 0.1; // can't really tell how well the start time was matched when
                                                          // we don't have a start candidate
                            estimatedDistanceProbability = late == end ? 1 : getDistanceEstimationBasedProbability(c, early, late);
                        }
                    }
                } else {
                    startTimingProbability = 1; // no penalty for any start time difference because this edge doesn't cover a start
                    if (late == end) {
                        // final edge; we don't know anything about distances for the end proxy node
                        estimatedDistanceProbability = 1;
                    } else {
                        estimatedDistanceProbability = getDistanceEstimationBasedProbability(c, early, late);
                    }
                }
                // If one of the candidates is fixed, the edge is always created unless they travel backwards in time.
                // Otherwise the edge is only created if the distance estimation, which can be calculated as long as the
                // candidates are not the proxy and or start is close enough to the actual distance sailed.
                final NavigableSet<Candidate> fixed = fixedPassings.get(c);
                // TODO this comparison does not exactly implement the condition "if distance is more likely than skipping"
                if (travelingForwardInTimeOrUnknown(early, late) &&
                        (fixed.contains(early) || fixed.contains(late) || estimatedDistanceProbability > MINIMUM_PROBABILITY)) {
                    addEdge(edges, new Edge(early, late, startTimingProbability * estimatedDistanceProbability, race.getRace().getCourse().getNumberOfWaypoints()));
                }
            }
        }
    }

    private boolean travelingForwardInTimeOrUnknown(Candidate early, Candidate late) {
        return early.getTimePoint() == null || late.getTimePoint() == null || early.getTimePoint().before(late.getTimePoint());
    }

    private void addEdge(Map<Candidate, Set<Edge>> edges, Edge e) {
        logger.finest("Adding "+ e.toString());
        Set<Edge> edgeSet = edges.get(e.getStart());
        if (edgeSet == null) {
            edgeSet = new HashSet<>();
            edges.put(e.getStart(), edgeSet);
        }
        edgeSet.add(e);
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
            NavigableSet<Util.Pair<Edge, Double>> currentEdgesCheapestFirst = new TreeSet<>(new Comparator<Util.Pair<Edge, Double>>() {
                @Override
                public int compare(Util.Pair<Edge, Double> o1, Util.Pair<Edge, Double> o2) {
                    int result = o1.getB().compareTo(o2.getB());
                    return result != 0 ? result : o1.getA().compareTo(o2.getA());
                }
            });
            Map<Candidate, Util.Pair<Candidate, Double>> candidateWithParentAndSmallestTotalCost = new HashMap<>();
            int indexOfEndOfFixedInterval = endOfFixedInterval.getOneBasedIndexOfWaypoint();

            boolean endFound = false;
            currentEdgesCheapestFirst.add(new Util.Pair<Edge, Double>(new Edge(new CandidateImpl(-1, null, /* estimated distance probability */ 1, null), startOfFixedInterval,
                    0, race.getRace().getCourse().getNumberOfWaypoints()), 0.0));
            while (!endFound) {
                Util.Pair<Edge, Double> cheapestEdgeWithCost = currentEdgesCheapestFirst.pollFirst();
                Edge currentCheapestEdge = cheapestEdgeWithCost.getA();
                Double currentCheapestCost = cheapestEdgeWithCost.getB();
                // If the shortest path to this candidate is already known the new edge is not added.
                if (!candidateWithParentAndSmallestTotalCost.containsKey(currentCheapestEdge.getEnd())) {
                    // The cheapest edge taking us to currentCheapestEdge.getEnd() is found. Remember it.
                    candidateWithParentAndSmallestTotalCost.put(currentCheapestEdge.getEnd(), new Util.Pair<Candidate, Double>(
                            currentCheapestEdge.getStart(), currentCheapestCost));
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Added "+ currentCheapestEdge + "as cheapest edge for " + c);
                    }
                    endFound = currentCheapestEdge.getEnd() == endOfFixedInterval;
                    if (!endFound) {
                        // the end of the segment was not yet found; add edges leading away from
                        // currentCheapestEdge.getEnd(), summing up their cost with the cost required
                        // to reach currentCheapestEdge.getEnd()
                        Set<Edge> edgesForNewCandidate = allCompetitorEdges.get(currentCheapestEdge.getEnd());
                        for (Edge e : edgesForNewCandidate) {
                            int oneBasedIndexOfEndOfEdge = e.getEnd().getOneBasedIndexOfWaypoint();
                            // only add edge if it stays within the current segment, not exceeding
                            // the next fixed mark passing
                            if (oneBasedIndexOfEndOfEdge <= indexOfEndOfFixedInterval
                                    && (oneBasedIndexOfEndOfEdge < oneBasedIndexOfSuppressedWaypoint || e.getEnd() == end)) {
                                currentEdgesCheapestFirst.add(new Util.Pair<Edge, Double>(e, currentCheapestCost + e.getCost()));
                            }
                        }
                    }
                }
            }
            Candidate marker = candidateWithParentAndSmallestTotalCost.get(endOfFixedInterval).getA();
            while (marker.getOneBasedIndexOfWaypoint() > 0) {
                mostLikelyCandidates.add(marker);
                marker = candidateWithParentAndSmallestTotalCost.get(marker).getA();
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
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Updating MarkPasses for " + c + " in case "+race.getRace().getName());
            }
            race.updateMarkPassings(c, newMarkPassings);
        }
    }

    /**
     * The distance between waypoints is used to estimate the distance that should be covered between these two
     * candidates. This estimation is then compared to the distance actually sailed. A distance smaller than the
     * estimation is (aside from a small tolerance) impossible, a distance larger get increasingly unlikely.
     */
    private double getDistanceEstimationBasedProbability(Competitor c, Candidate c1, Candidate c2) {
        final double result;
        assert c1.getOneBasedIndexOfWaypoint() < c2.getOneBasedIndexOfWaypoint();
        assert c2 != end;
        Waypoint first;
        final TimePoint middleOfc1Andc2 = new MillisecondsTimePoint(c1.getTimePoint().plus(c2.getTimePoint().asMillis()).asMillis() / 2);
        if (c1.getOneBasedIndexOfWaypoint() == 0) {
            first = race.getRace().getCourse().getFirstWaypoint();
        } else {
            first = c1.getWaypoint();
        }
        final Waypoint second = c2.getWaypoint();
        Distance totalGreatCircleDistance = getTotalGreatCircleDistanceBetweenWaypoints(first, second, middleOfc1Andc2);
        Distance actualDistanceTraveled = race.getTrack(c).getDistanceTraveled(c1.getTimePoint(), c2.getTimePoint());
        result = getProbabilityOfActualDistanceGivenGreatCircleDistance(totalGreatCircleDistance, actualDistanceTraveled);
        return result;
    }

    /**
     * Based on a direct great-circle distance between waypoints and an actual distance sailed, determines how likely it
     * is that this distance sailed could have happened between those waypoints. For a reaching leg, this would be based
     * on a straight comparison of the numbers. However, with upwind and downwind legs and boats not going from mark to
     * mark on a great circle segment, distances sailed will exceed the great line circle distances.
     * <p>
     * 
     * A smaller distance than great circle from mark to mark is very unlikely, somewhere between the distance estimated
     * and double that is likely and anything greater than that gradually becomes unlikely.
     * 
     * @return a number between 0 and 1 with 1 representing a "fair chance" that the actual distance sailed could have
     *         been sailed for the given great circle distance; 1 is returned for actual distances being in the range of
     *         1..2 times the great circle distance. Actual distances outside this interval reduce probability linearly
     *         for smaller distances (gradient 3.5) and varies with the square root for distances that exceed twice the
     *         great circle distance.
     */
    private double getProbabilityOfActualDistanceGivenGreatCircleDistance(Distance totalGreatCircleDistance,
            Distance actualDistanceTraveled) {
        final double result;
        double differenceInMeters = actualDistanceTraveled.getMeters() - totalGreatCircleDistance.getMeters();
        double ratio = differenceInMeters / totalGreatCircleDistance.getMeters();
        // A smaller distance than great circle from mark to mark is very unlikely, somewhere between the distance
        // estimated and double that is likely and anything greater than that gradually becomes unlikely
        if (ratio < 0) {
            // TODO shouldn't these factors be constants in the class header for easy fine-tuning?
            result = 3.5 * ratio + 1;
        } else if (ratio > 1) {
            result = Math.sqrt((-ratio + 3) / 2);
        } else {
            result = 1;
        }
        return result;
    }

    private Distance getTotalGreatCircleDistanceBetweenWaypoints(Waypoint first, final Waypoint second,
            final TimePoint timePoint) {
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
                totalGreatCircleDistance = totalGreatCircleDistance.add(leg.getGreatCircleDistance(timePoint));
            }
        }
        return totalGreatCircleDistance;
    }

    private void addCandidates(Competitor c, Iterable<Candidate> newCandidates) {
        for (Candidate can : newCandidates) {
            candidates.get(c).add(can);
        }
        createNewEdges(c, newCandidates);
    }

    private void removeCandidates(Competitor c, Iterable<Candidate> wrongCandidates) {
        for (Candidate can : wrongCandidates) {
            logger.finest("Removing all edges containing " + can.toString() + "of "+ c);
            candidates.get(c).remove(can);
            Map<Candidate, Set<Edge>> edges = allEdges.get(c);
            edges.remove(can);
            for (Set<Edge> set : edges.values()) {
                for (Iterator<Edge> i = set.iterator(); i.hasNext();) {
                    final Edge e = i.next();
                    if (e.getStart().equals(can) || e.getEnd().equals(can)) {
                        i.remove();
                    }
                }
            }
        }
    }

    private class CandidateWithSettableTime extends CandidateImpl {
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

    private class CandidateWithSettableWaypointIndex extends CandidateImpl {
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
