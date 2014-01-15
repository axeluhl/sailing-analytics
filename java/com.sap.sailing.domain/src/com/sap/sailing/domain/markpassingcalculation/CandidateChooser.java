package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

/**
 * The standard implementation of {@link AbstractCandidateChooser}. First two proxy-candidates are created,  It makes creates {@link Edge}s out of the
 * {@link Candidate}s for each competitor, creating a DAG. The shortest path from A shortest path-algorithm is then used to find the most
 * likely sequence of {@link MarkPassing}s. An estimation of the time between two {@link Candidate}s and the distance
 * between their {@link Waypoint}s is used to further increase the edges correctness.
 * 
 * @author Nicolas Klose
 * 
 */
public class CandidateChooser implements AbstractCandidateChooser {

    private static final Logger logger = Logger.getLogger(CandidateChooser.class.getName());

    private LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> currentMarkPasses = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, List<Edge>> allEdges = new LinkedHashMap<>();
    private LinkedHashMap<Competitor, List<Candidate>> candidates = new LinkedHashMap<>();
    private TimePoint raceStartTime;
    private Candidate start;
    private Candidate end;
    private DynamicTrackedRace race;
    private double penaltyForSkipping = 1 - Edge.penaltyForSkipped;
    static double strictness = 200;
    private MockedPolarSheetDeliverer polar = new MockedPolarSheetDeliverer(); 
    

    public CandidateChooser(DynamicTrackedRace race) {
        logger.setLevel(Level.INFO);
        this.race = race;
        raceStartTime = race.getStartOfRace();
        start = new Candidate(0, raceStartTime, 1);
        end = new Candidate(
                race.getRace().getCourse().getIndexOfWaypoint(race.getRace().getCourse().getLastWaypoint()) + 2, null,
                1);
        candidates = new LinkedHashMap<>();
        for (Competitor c : race.getRace().getCompetitors()) {
            candidates.put(c, new ArrayList<Candidate>());
            currentMarkPasses.put(c, new LinkedHashMap<Waypoint, MarkPassing>());
            allEdges.put(c, new ArrayList<Edge>());
            addCandidates(Arrays.asList(start, end), c);
        }
    }

    @Override
    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getAllPasses() {
        return currentMarkPasses;
    }

    @Override
    public void calculateMarkPassDeltas(Competitor c, Pair<List<Candidate>, List<Candidate>> candidateDeltas) {
        if (race.getStartOfRace() != raceStartTime) {
            raceStartTime = race.getStartOfRace();
            for (Competitor com : allEdges.keySet()) {
                removeCandidates(Arrays.asList(start), com);
            }
            start = new Candidate(0, raceStartTime, 1);
            for (Competitor com : allEdges.keySet()) {
                addCandidates(Arrays.asList(start), com);
            }
        }
        removeCandidates(candidateDeltas.getB(), c);
        addCandidates(candidateDeltas.getA(), c);
        findShortestPath(c);
    }

    private void createNewEdges(Competitor co, List<Candidate> newCans) {
        for (Candidate newCan : newCans) {
            for (Candidate oldCan : candidates.get(co)) {
                Candidate early = newCan;
                Candidate late = oldCan;
                if (oldCan.getID() < newCan.getID()) {
                    early = oldCan;
                    late = newCan;
                }
                if (raceStartTime != null) {
                    if (late == end) {
                        allEdges.get(co).add(new Edge(early, late, 1));
                    } else if (!(early.getID() == late.getID()) && !late.getTimePoint().before(early.getTimePoint())
                            && estimatedTime(early, late) > penaltyForSkipping) {
                        allEdges.get(co).add(new Edge(early, late, estimatedTime(early, late)));
                    }
                } else {
                    if (early == start && late.getID() == 1
                            && numberOfCloseStarts(late.getTimePoint()) > penaltyForSkipping) {
                        allEdges.get(co).add(new Edge(early, late, numberOfCloseStarts(late.getTimePoint())));
                    } else if (late == end || early == start) {
                        allEdges.get(co).add(new Edge(early, late, 1));
                    } else if (!(early.getID() == late.getID()) && late.getTimePoint().after(early.getTimePoint())
                            && estimatedTime(early, late) > penaltyForSkipping) {
                        allEdges.get(co).add(new Edge(early, late, estimatedTime(early, late)));
                    }
                }
            }
        }
    }

    private void findShortestPath(Competitor co) {
        boolean changed = false;
        ArrayList<Edge> all = new ArrayList<>();
        for (Edge e : allEdges.get(co)) {
            all.add(e);
        }
        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<>();
        candidateWithParent.put(start, null);
        Edge newMostLikelyEdge = null;
        while (!candidateWithParent.containsKey(end)) {
            newMostLikelyEdge = null;
            for (Edge e : all) {
                if (candidateWithParent.containsKey(e.getStart())) {
                    if (newMostLikelyEdge == null) {
                        newMostLikelyEdge = e;
                    } else if (e.getProbability() < newMostLikelyEdge.getProbability()) {
                        newMostLikelyEdge = e;
                    }
                }
            }
            if (!candidateWithParent.containsKey(newMostLikelyEdge.getEnd())) {
                candidateWithParent.put(newMostLikelyEdge.getEnd(), newMostLikelyEdge.getStart());
            }
            all.remove(newMostLikelyEdge);
        }

        Candidate marker = candidateWithParent.get(end);
        while (!(marker == start)) {
            if (currentMarkPasses.get(co).get(marker.getWaypoint()) == null
                    || currentMarkPasses.get(co).get(marker.getWaypoint()).getTimePoint() != marker.getTimePoint()) {
                currentMarkPasses.get(co).put(marker.getWaypoint(),
                        new MarkPassingImpl(marker.getTimePoint(), marker.getWaypoint(), co));
                changed = true;
            }
            marker = candidateWithParent.get(marker);
        }
        if (changed) {
            logger.info("New MarkPasses for" + co);
            List<MarkPassing> markPassDeltas = new ArrayList<>();
            for (MarkPassing m : currentMarkPasses.get(co).values()) {
                markPassDeltas.add(m);
            }
            race.updateMarkPassings(co, markPassDeltas);
        }
    }

    @SuppressWarnings("unused")
    private void reEvaluateStartingEdges() {
        for (Competitor c : candidates.keySet()) {
            ArrayList<Edge> newEdges = new ArrayList<>();
            ArrayList<Edge> edgesToRemove = new ArrayList<>();
            for (Edge e : allEdges.get(c)) {
                if (e.getStart().getID() == 0 && e.getEnd().getID() == 1) {
                    newEdges.add(new Edge(e.getStart(), e.getEnd(), numberOfCloseStarts(e.getEnd().getTimePoint())));
                    edgesToRemove.add(e);
                }
            }
            for (Edge e : edgesToRemove) {
                allEdges.get(c).remove(e);
            }
            for (Edge e : newEdges) {
                allEdges.get(c).add(e);
            }
        }
    }

    private double numberOfCloseStarts(TimePoint t) {
        double numberOfCompetitors = 0;
        double totalTimeDifference = 0;
        for (Competitor c : candidates.keySet()) {

            double closestCandidate = -1;
            for (Candidate ca : candidates.get(c)) {
                if (ca.getID() == 1) {
                    if (closestCandidate == -1) {
                        closestCandidate = Math.abs(ca.getTimePoint().asMillis() - t.asMillis());
                    } else if (closestCandidate > Math.abs(ca.getTimePoint().asMillis() - t.asMillis())) {
                        closestCandidate = Math.abs(ca.getTimePoint().asMillis() - t.asMillis());
                    }
                }
            }
            if (closestCandidate != -1) {
                numberOfCompetitors++;
                totalTimeDifference = totalTimeDifference + closestCandidate;
            }
        }
        return 1 / (0.00001 * (totalTimeDifference / numberOfCompetitors) + 1);
    }

    private double estimatedTime(Candidate c1, Candidate c2) {

        double totalEstimatedTime = 0;
        Waypoint current;
        if (c1.getID() == 0) {
            current = race.getRace().getCourse().getFirstWaypoint();
        } else {
            current = c1.getWaypoint();
        }
        while (current != c2.getWaypoint()) {
            TrackedLeg leg = race.getTrackedLegStartingAt(current);
            totalEstimatedTime = totalEstimatedTime
                    + estimatedTimeOnLeg(
                            leg,
                            c1.getTimePoint().plus(
                                    1 / 2 * c2.getTimePoint().minus(c1.getTimePoint().asMillis()).asMillis()));
            current = leg.getLeg().getTo();
        }
        totalEstimatedTime = totalEstimatedTime * 3600000;
        double actualTime = c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis();
        double timeDiff = Math.abs(totalEstimatedTime - actualTime) / 1000;
        double sigmaSquared = 0.2;
        double factor = 1 / (Math.sqrt(sigmaSquared * 2 * Math.PI));
        double exponent = -(Math.pow(timeDiff / strictness, 2) / (2 * sigmaSquared));
        return factor * Math.pow(Math.E, exponent);

    }

    private double estimatedTimeOnLeg(TrackedLeg leg, TimePoint t) {

        try {
            if (leg.getLegType(t) == LegType.DOWNWIND) {
                return leg.getGreatCircleDistance(t).getNauticalMiles()
                        / polar.getDownwind(race.getWind(race.getApproximatePosition(leg.getLeg().getFrom(), t), t));
            }

            if (leg.getLegType(t) == LegType.UPWIND) {
                return leg.getGreatCircleDistance(t).getNauticalMiles()
                        / polar.getUpwind(race.getWind(race.getApproximatePosition(leg.getLeg().getFrom(), t), t));
            }
        } catch (NoWindException e) {
            Logger.getLogger(CandidateChooser.class.getName()).log(
                    Level.SEVERE,
                    "CandidateChooser threw exception " + e.getMessage()
                            + " while estimating the Time between two Candidates.");
        }

        return leg.getGreatCircleDistance(t).getNauticalMiles()
                / polar.getReaching(race.getWind(race.getApproximatePosition(leg.getLeg().getFrom(), t), t));

    }

    private void addCandidates(List<Candidate> newCandidates, Competitor co) {
        for (Candidate c : newCandidates) {
            if (!candidates.get(co).contains(c)) {
                candidates.get(co).add(c);
            }
            /*
             * if (c.getID() == 1 && race.getStartOfRace() == null) { reEvaluateStartingEdges(); }
             */
            // TODO Work without starting time
        }
        createNewEdges(co, newCandidates);
    }

    private void removeCandidates(List<Candidate> wrongCandidates, Competitor co) {
        for (Candidate c : wrongCandidates) {
            candidates.get(co).remove(c);
            List<Edge> toRemove = new ArrayList<>();
            for (Edge e : allEdges.get(co)) {
                if (e.getStart().equals(c) || e.getEnd().equals(c)) {
                 toRemove.add(e);   
                }
            }
            for(Edge e : toRemove){
                allEdges.get(co).remove(e);
            }
        }
    }
}
