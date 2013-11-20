package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class CandidateChooser {

    LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> currentMarkPasses = new LinkedHashMap<>();
    LinkedHashMap<Competitor, ArrayList<Edge>> allEdges = new LinkedHashMap<>();
    LinkedHashMap<Competitor, ArrayList<Candidate>> candidates = new LinkedHashMap<>();
    ArrayList<Double> legLengths;
    TimePoint startOfRace;
    Candidate start;
    Candidate end;

    public CandidateChooser(TimePoint startOfRace, Iterable<Competitor> competitors, ArrayList<Double> legLengths) {
        this.startOfRace = startOfRace;
        this.legLengths = legLengths;
        end = new Candidate(legLengths.size() + 2, null, 0);
        start = new Candidate(0, startOfRace, 0);
        for (Competitor c : competitors) {
            currentMarkPasses.put(c, new LinkedHashMap<Waypoint, MarkPassing>());
            allEdges.put(c, new ArrayList<Edge>());
            candidates.put(c, new ArrayList<Candidate>());
            candidates.get(c).add(start);
            candidates.get(c).add(end);
            allEdges.get(c).add(new Edge(start, end));
        }
    }

    public void upDateLegLengths(ArrayList<Double> legLengths) {
        this.legLengths = legLengths;
    }

    public void addCandidate(Candidate c, Competitor co) {
        candidates.get(co).add(c);
        createNewEdges(co, c);
        findShortestPath(co);
    }

    public void removeCandidate(Candidate c, Competitor co) {
        candidates.get(co).remove(c);
        for (Edge e : allEdges.get(co)) {
            if (e.getStart().equals(c) || e.getEnd().equals(c)) {
                allEdges.remove(e);
            }
        }
        if (currentMarkPasses.get(co).containsValue(c)) {
            findShortestPath(co);
        }
    }

    public MarkPassing getMarkPass(Competitor c, Waypoint w) {
        return currentMarkPasses.get(c).get(w);
    }

    private void findShortestPath(Competitor co) {

        ArrayList<Edge> all = new ArrayList<>();
        for (Edge e : allEdges.get(co)) {
            all.add(e);
        }
        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<>();
        candidateWithParent.put(start, start);
        Edge newCheapestEdge = null;
        while (!candidateWithParent.keySet().contains(end)) {
            newCheapestEdge = null;
            for (Edge e : all) {
                if (candidateWithParent.keySet().contains(e.getStart())) {
                    if (newCheapestEdge == null) {
                        newCheapestEdge = e;
                    } else if (e.getCost() < newCheapestEdge.getCost()) {
                        newCheapestEdge = e;
                    }
                }
            }
            if (!candidateWithParent.keySet().contains(newCheapestEdge.getEnd())) {
                candidateWithParent.put(newCheapestEdge.getEnd(), newCheapestEdge.getStart());
            }
            all.remove(newCheapestEdge);
        }
        currentMarkPasses.get(co).clear();
        Candidate marker = candidateWithParent.get(end);
        while (!(marker == start)) {
            currentMarkPasses.get(co).put(marker.getWaypoint(),
                    new MarkPassingImpl(marker.getTimePoint(), marker.getWaypoint(), co));
            marker = candidateWithParent.get(marker);
        }
    }

    private void createNewEdges(Competitor co, Candidate c) {
        for (Candidate ca : candidates.get(co)) {
            Candidate early = c;
            Candidate late = ca;
            if (ca.getID() < c.getID()) {
                early = ca;
                late = c;
            }
            if (late.getID() == end.getID()) {
                allEdges.get(co).add(new Edge(early, late));
            } else {
                if (!(early.getID() == late.getID()) && late.getTimePoint().after(early.getTimePoint())
                        && averageSpeed(early, late)) {
                    allEdges.get(co).add(new Edge(early, late));
                }
            }
        }
    }

    private void createAllEdges(Competitor co) {
        allEdges.get(co).clear();
        for (Candidate c1 : candidates.get(co)) {
            for (Candidate c2 : candidates.get(co)) {
                if (c1.getID() == end.getID() || c2.getID() == end.getID()) {
                    allEdges.get(co).add(new Edge(c1, c2));
                } else {
                    if (c1.getID() < c2.getID() && c2.getTimePoint().after(c1.getTimePoint()) && averageSpeed(c1, c2)) {
                        allEdges.get(co).add(new Edge(c1, c2));
                    }
                }
            }
        }
    }

    private boolean averageSpeed(Candidate c1, Candidate c2) {

        double distance = 0;
        double minimumSpeed = 2.5;
        double maximumSpeed = 5;
        double averageSpeed = (maximumSpeed + minimumSpeed) / 2;
        double delta = maximumSpeed - minimumSpeed;

        if (c1.getID() == 0 && c2.getID() == 1 && c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis() < 300000) {
            return true;
        }
        for (int i = c1.getID(); i < c2.getID(); i++) {
            if (!(i == 0)) {
                distance = distance + legLengths.get(i - 1);
            }
        }
        double time = c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis();
        double speed = distance / (time / 1000);
        if (Math.abs(speed - averageSpeed) < delta) {
            return true;
        }
        return false;
    }
}
