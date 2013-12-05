package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class CandidateChooser implements AbstractCandidateChooser {

    LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> currentMarkPasses = new LinkedHashMap<>();
    LinkedHashMap<Competitor, List<Edge>> allEdges = new LinkedHashMap<>();
    LinkedHashMap<Competitor, List<Candidate>> candidates = new LinkedHashMap<>();
    boolean raceHasStartTime;
    Candidate start;
    Candidate end;
    TrackedRace race;
    PolarSheetDeliverer polar = new PolarSheetDeliverer() {

        @Override
        public double getReaching(Wind w) {
            return 8;
        }

        @Override
        public double getUpwind(Wind w) {
            return 6;
        }

        @Override
        public double getDownwind(Wind w) {
            return 10;
        }

    };

    public CandidateChooser(TrackedRace race, LinkedHashMap<Competitor, List<Candidate>> candidates) {
        this.race = race;
        raceHasStartTime = race.getStartOfRace() == null ? false : true;
        start = new Candidate(0, race.getStartOfRace(), 1);
        end = new Candidate(
                race.getRace().getCourse().getIndexOfWaypoint(race.getRace().getCourse().getLastWaypoint()) + 2, null,
                1);
        this.candidates = candidates;
        for (Competitor c : candidates.keySet()) {
            currentMarkPasses.put(c, new LinkedHashMap<Waypoint, MarkPassing>());
            allEdges.put(c, new ArrayList<Edge>());
            this.candidates.get(c).add(start);
            this.candidates.get(c).add(end);
            allEdges.get(c).add(new Edge(start, end, 0));
            createNewEdges(c, this.candidates.get(c));
            findShortestPath(c);
        }
    }

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getAllMarkPasses() {
        return currentMarkPasses;
    }

    @Override
    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getMarkPassDeltas(
            LinkedHashMap<Competitor, Pair<List<Candidate>, List<Candidate>>> candidateDeltas) {
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> markPassDeltas = new LinkedHashMap<>();
        for (Competitor c : candidateDeltas.keySet()) {
            removeCandidates(candidateDeltas.get(c).first(), c);
            addCandidates(candidateDeltas.get(c).second(), c);
            LinkedHashMap<Waypoint, MarkPassing> deltas = findShortestPath(c);
            if(deltas.values()!=null){
                markPassDeltas.put(c, deltas);
            }
        }
        return markPassDeltas;
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
                if (raceHasStartTime) {
                    if (late == end) {
                        allEdges.get(co).add(new Edge(early, late, 1));
                    } else if (early == start && early.getTimePoint().before(late.getTimePoint())) {
                        allEdges.get(co).add(new Edge(early, late, 1)); // timeestimtion???
                    } else if (!(early.getID() == late.getID()) && late.getTimePoint().after(early.getTimePoint())) {
                        allEdges.get(co).add(new Edge(early, late, estimatedTime(early, late)));
                    }
                } else {
                    if (early == start && late.getID() == 1) {
                        allEdges.get(co).add(new Edge(early, late, numberOfCloseStarts(late.getTimePoint())));
                    } else if (late == end || early == start) {
                        allEdges.get(co).add(new Edge(early, late, 1));
                    } else if (!(early.getID() == late.getID()) && late.getTimePoint().after(early.getTimePoint())) {
                        allEdges.get(co).add(new Edge(early, late, estimatedTime(early, late)));
                    }
                }
            }
        }
    }

    private LinkedHashMap<Waypoint, MarkPassing> findShortestPath(Competitor co) {
        LinkedHashMap<Waypoint, MarkPassing> markPassDeltas = new LinkedHashMap<>();
        ArrayList<Edge> all = new ArrayList<>();
        for (Edge e : allEdges.get(co)) {
            all.add(e);
        }
        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<>();
        candidateWithParent.put(start, start);
        Edge newMostLikelyEdge = null;
        while (!candidateWithParent.keySet().contains(end)) {
            newMostLikelyEdge = null;
            for (Edge e : all) {
                if (candidateWithParent.keySet().contains(e.getStart())) {
                    if (newMostLikelyEdge == null) {
                        newMostLikelyEdge = e;
                    } else if (e.getProbability() < newMostLikelyEdge.getProbability()) {
                        newMostLikelyEdge = e;
                    }
                }
            }
            if (!candidateWithParent.keySet().contains(newMostLikelyEdge.getEnd())) {
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
                markPassDeltas.put(marker.getWaypoint(),
                        new MarkPassingImpl(marker.getTimePoint(), marker.getWaypoint(), co));
            }
            marker = candidateWithParent.get(marker);
        }
        return markPassDeltas;
    }

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

        double totalTime = 0;
        int i;
        Waypoint current;
        if (c1.getID() == 0) {
            current = race.getRace().getCourse().getFirstWaypoint();
            i = 2;
        } else {
            current = c1.getWaypoint();
            i = 1;
        }

        while (current != c2.getWaypoint()) {

            TrackedLeg leg = race.getTrackedLegStartingAt(current);

            totalTime = totalTime
                    + estimatedTimeOnLeg(
                            leg,
                            c1.getTimePoint().plus(
                                    (2 * (i - 1) / (2 * (c2.getID() - c1.getID())) * c2.getTimePoint()
                                            .minus(c1.getTimePoint().asMillis()).asMillis())));
            i++;
            current = leg.getLeg().getTo();
        }
        totalTime = totalTime * 3600000;
        double actualTime = c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis();
        double timeDiff = Math.abs(totalTime - actualTime) / 1000;
        return 1 - (Math.log10(timeDiff + 1) / 20);
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
        }

        return leg.getGreatCircleDistance(t).getNauticalMiles()
                / polar.getReaching(race.getWind(race.getApproximatePosition(leg.getLeg().getFrom(), t), t));

    }

    private void addCandidates(List<Candidate> newCandidates, Competitor co) {
        for (Candidate c : newCandidates) {
            candidates.get(co).add(c);
            if (c.getID() == 1) {
                reEvaluateStartingEdges();
            }
        }
        createNewEdges(co, newCandidates);
    }

    private void removeCandidates(List<Candidate> wrongCandidates, Competitor co) {
        for (Candidate c : wrongCandidates) {
            candidates.get(co).remove(c);
            for (Edge e : allEdges.get(co)) {
                if (e.getStart().equals(c) || e.getEnd().equals(c)) {
                    allEdges.remove(e);
                }
            }
        }
    }
}
