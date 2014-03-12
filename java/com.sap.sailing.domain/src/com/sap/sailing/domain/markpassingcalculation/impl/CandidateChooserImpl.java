package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

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

    private static final int MILLISECONDS_BEFORE_STARTTIME = 2000;
    private final static double MINIMUM_PROBABILITY = 1 - Edge.getPenaltyForSkipping();

    private static final Logger logger = Logger.getLogger(CandidateChooserImpl.class.getName());

    private Map<Competitor, Map<Waypoint, MarkPassing>> currentMarkPasses = new HashMap<>();
    private Map<Competitor, Map<Candidate, Set<Edge>>> allEdges = new HashMap<>();
    private Map<Competitor, Set<Candidate>> candidates = new HashMap<>();
    private TimePoint raceStartTime;
    private Candidate start;
    private final Candidate end;
    private final DynamicTrackedRace race;

    // TODO Locking

    public CandidateChooserImpl(DynamicTrackedRace race) {
        this.race = race;
        raceStartTime = race.getStartOfRace() != null ? race.getStartOfRace().minus(MILLISECONDS_BEFORE_STARTTIME) : null;
        start = new CandidateImpl(/* Index */0, raceStartTime, /* Probability */1, /* Waypoint */null, /* right Side */true,
        /* right Direction */true, "Proxy");
        end = new CandidateImpl(race.getRace().getCourse().getIndexOfWaypoint(race.getRace().getCourse().getLastWaypoint()) + 2, /* TimePoint */null,
        /* Probability */1, /* Waypoint */null, /* right Side */true, /* right Direction */true, "Proxy");
        candidates = new HashMap<>();
        List<Candidate> startAndEnd = Arrays.asList(start, end);
        for (Competitor c : race.getRace().getCompetitors()) {
            candidates.put(c, new TreeSet<Candidate>());
            currentMarkPasses.put(c, new HashMap<Waypoint, MarkPassing>());
            allEdges.put(c, new HashMap<Candidate, Set<Edge>>());
            addCandidates(c, startAndEnd);
        }
    }

    @Override
    public void calculateMarkPassDeltas(Competitor c, Iterable<Candidate> newCans, Iterable<Candidate> oldCans) {
        TimePoint startOfRace = race.getStartOfRace();
        if (startOfRace != null) {
            if (raceStartTime == null || !startOfRace.minus(MILLISECONDS_BEFORE_STARTTIME).equals(raceStartTime)) {
                raceStartTime = startOfRace.minus(MILLISECONDS_BEFORE_STARTTIME);
                for (Competitor com : candidates.keySet()) {
                    removeCandidates(Arrays.asList(start), com);
                }
                start = new CandidateImpl(0, raceStartTime, /* Probability */1, /* Waypoint */null, /* right Side */true,
                /* right Direction */true, "Proxy");
                for (Competitor com : allEdges.keySet()) {
                    addCandidates(com, Arrays.asList(start));
                }
            }
        }
        removeCandidates(oldCans, c);
        addCandidates(c, newCans);
        findShortestPath(c);
    }

    @Override
    public void removeWaypoints(Iterable<Waypoint> ways) {
        for (Competitor c : currentMarkPasses.keySet()) {
            for (Waypoint w : ways) {
                currentMarkPasses.get(c).remove(w);
            }
        }
    }

    private void createNewEdges(Competitor co, Iterable<Candidate> newCandidates) {
        Map<Candidate, Set<Edge>> edges = allEdges.get(co);
        for (Candidate newCan : newCandidates) {
            for (Candidate oldCan : candidates.get(co)) {
                final Candidate early;
                final Candidate late;
                if (oldCan.getOneBasedIndexOfWaypoint() < newCan.getOneBasedIndexOfWaypoint()) {
                    early = oldCan;
                    late = newCan;
                } else if (oldCan.getOneBasedIndexOfWaypoint() > newCan.getOneBasedIndexOfWaypoint()) {
                    late = oldCan;
                    early = newCan;
                } else {
                    continue;
                }
                // If a start time is given the probability of an Edge from start to an other candidate can be
                // evaluated.
                if (late == end) {
                    addEdge(edges, new Edge(early, late, 1, race.getRace().getCourse()));
                } else if (early == start && (raceStartTime == null || !early.getTimePoint().after(late.getTimePoint()))) {
                    addEdge(edges, new Edge(early, late, 1, race.getRace().getCourse()));
                } else if (late.getTimePoint().after(early.getTimePoint())) {
                    final double probability = getDistanceEstimationBasedProbability(co, early, late);
                    if (probability > MINIMUM_PROBABILITY) {
                        addEdge(edges, new Edge(early, late, probability, race.getRace().getCourse()));
                    }
                }
            }
        }
    }

    private void addEdge(Map<Candidate, Set<Edge>> edges, Edge e) {
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
     * {@link DynamicTrackedRace#updateMarkPassings(Competitor, Iterable) notified}.
     */
    private void findShortestPath(Competitor co) {
        boolean changed = false;
        NavigableSet<Pair<Edge, Double>> currentEdgesCheapestFirst = new TreeSet<>(new Comparator<Pair<Edge, Double>>() {
            @Override
            public int compare(Pair<Edge, Double> o1, Pair<Edge, Double> o2) {
                int result = o1.getB().compareTo(o2.getB());
                return result != 0 ? result : o1.getA().compareTo(o2.getA());
            }
        });
        Map<Candidate, Pair<Candidate, Double>> candidateWithParentAndSmallestTotalCost = new HashMap<>();
        candidateWithParentAndSmallestTotalCost.put(start, null);
        Map<Candidate, Set<Edge>> allCompetitorEdges = allEdges.get(co);
        insertSuccessors(0, currentEdgesCheapestFirst, allCompetitorEdges.get(start));
        boolean endFound = false;
        // All Candidates have an Edge to end, therefore this loop will always terminate
        while (!endFound) {
            Pair<Edge, Double> cheapestEdgeWithCost = currentEdgesCheapestFirst.pollFirst();
            Edge currentCheapestEdge = cheapestEdgeWithCost.getA();
            Double currentCheapestCost = cheapestEdgeWithCost.getB();
            // If the shortest path to this candidate is already known the new edge is not added.
            if (!candidateWithParentAndSmallestTotalCost.containsKey(currentCheapestEdge.getEnd())) {
                candidateWithParentAndSmallestTotalCost.put(currentCheapestEdge.getEnd(), new Pair<Candidate, Double>(currentCheapestEdge.getStart(), currentCheapestCost));
                Set<Edge> edgesForNewCandidate = allCompetitorEdges.get(currentCheapestEdge.getEnd());
                if (edgesForNewCandidate != null) {
                    insertSuccessors(currentCheapestCost, currentEdgesCheapestFirst, edgesForNewCandidate);
                } else {
                    assert currentCheapestEdge.getEnd() == end;
                }
                endFound = currentCheapestEdge.getEnd() == end;
            }
        }
        Candidate marker = candidateWithParentAndSmallestTotalCost.get(end).getA();
        Map<Waypoint, MarkPassing> currentPasses = currentMarkPasses.get(co);
        List<MarkPassing> newMarkPassings = new LinkedList<>();
        while (marker != start) {
            Waypoint markerWaypoint = marker.getWaypoint();
            MarkPassing markPassingForWaypoint = currentPasses.get(markerWaypoint);
            TimePoint markerTimePoint = marker.getTimePoint();
            if (markPassingForWaypoint == null || !markPassingForWaypoint.getTimePoint().equals(markerTimePoint)) {
                markPassingForWaypoint = new MarkPassingImpl(markerTimePoint, markerWaypoint, co);
                currentPasses.put(markerWaypoint, markPassingForWaypoint);
                changed = true;
            }
            newMarkPassings.add(0, markPassingForWaypoint);
            marker = candidateWithParentAndSmallestTotalCost.get(marker).getA();
        }
        if (changed) {
            logger.fine("Updating MarkPasses for " + co);
            race.updateMarkPassings(co, newMarkPassings);
        }
    }

    private void insertSuccessors(double costToReachStartOfSuccessors, SortedSet<Pair<Edge, Double>> currentEdgesCheapestFirst, Set<Edge> successors) {
        for (Edge e : successors) {
            currentEdgesCheapestFirst.add(new Pair<Edge, Double>(e, costToReachStartOfSuccessors + e.getCost()));
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
        assert c1 != start;
        assert c2 != end;
        Distance totalEstimatedDistance = new MeterDistance(0);
        Waypoint first;
        final TimePoint middleOfc1Andc2 = new MillisecondsTimePoint(c1.getTimePoint().plus(c2.getTimePoint().asMillis()).asMillis() / 2);
        if (c1.getOneBasedIndexOfWaypoint() == 0) {
            first = race.getRace().getCourse().getFirstWaypoint();
        } else {
            first = c1.getWaypoint();
        }
        boolean legsAreBetweenCandidates = false;
        for (Iterator<TrackedLeg> it = race.getTrackedLegs().iterator(); it.hasNext();) {
            TrackedLeg leg = it.next();
            Waypoint from = leg.getLeg().getFrom();
            if (from == c2.getWaypoint()) {
                break;
            }
            if (from == first) {
                legsAreBetweenCandidates = true;
            }
            if (legsAreBetweenCandidates) {
                totalEstimatedDistance = totalEstimatedDistance.add(leg.getGreatCircleDistance(middleOfc1Andc2));
            }
        }
        Distance actualDistance = race.getTrack(c).getDistanceTraveled(c1.getTimePoint(), c2.getTimePoint());
        double differenceInMeters = actualDistance.getMeters() - totalEstimatedDistance.getMeters();
        double differenceInPercent = differenceInMeters / totalEstimatedDistance.getMeters();
        // A smaller distance than estimated is very unlikely, somewhere between the distance estimated and double that
        // is likely and anything greater than that gradually becomes unlikely
        if (differenceInPercent < 0) {
            result = 3.5 * differenceInPercent + 1;
        } else if (differenceInPercent > 1) {
            result = Math.sqrt((-differenceInPercent + 3) / 2);
        } else {
            result = 1;
        }
        return result;
    }

    private void addCandidates(Competitor co, Iterable<Candidate> newCandidates) {
        for (Candidate c : newCandidates) {
            candidates.get(co).add(c);
        }
        createNewEdges(co, newCandidates);
    }

    private void removeCandidates(Iterable<Candidate> wrongCandidates, Competitor co) {
        for (Candidate c : wrongCandidates) {
            candidates.get(co).remove(c);
            Map<Candidate, Set<Edge>> edges = allEdges.get(co);
            edges.remove(c);
            for (Set<Edge> set : edges.values()) {
                for (Iterator<Edge> i = set.iterator(); i.hasNext();) {
                    final Edge e = i.next();
                    if (e.getStart().equals(c) || e.getEnd().equals(c)) {
                        i.remove();
                    }
                }
            }
        }
    }
}

/*
 * private void reEvaluateStartingEdges() { for (Competitor c : candidates.keySet()) { ArrayList<Edge> newEdges = new
 * ArrayList<>(); ArrayList<Edge> edgesToRemove = new ArrayList<>(); for (Edge e : allEdges.get(c)) { if
 * (e.getStart().getID() == 0 && e.getEnd().getID() == 1) { newEdges.add(new Edge(e.getStart(), e.getEnd(),
 * numberOfCloseStarts(e.getEnd().getTimePoint()))); edgesToRemove.add(e); } } for (Edge e : edgesToRemove) {
 * allEdges.get(c).remove(e); } for (Edge e : newEdges) { allEdges.get(c).add(e); } } }
 * 
 * private double numberOfCloseStarts(TimePoint t) { double numberOfCompetitors = 0; double totalTimeDifference = 0; for
 * (Competitor c : candidates.keySet()) { double closestCandidate = -1; for (Candidate ca : candidates.get(c)) { if
 * (ca.getID() == 1) { if (closestCandidate == -1) { closestCandidate = Math.abs(ca.getTimePoint().asMillis() -
 * t.asMillis()); } else if (closestCandidate > Math.abs(ca.getTimePoint().asMillis() - t.asMillis())) {
 * closestCandidate = Math.abs(ca.getTimePoint().asMillis() - t.asMillis()); } } } if (closestCandidate != -1) {
 * numberOfCompetitors++; totalTimeDifference = totalTimeDifference + closestCandidate; } } return 1 / (0.00001 *
 * (totalTimeDifference / numberOfCompetitors) + 1); }
 */
