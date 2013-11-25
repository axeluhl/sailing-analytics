package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class CandidateChooser {

    LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> currentMarkPasses = new LinkedHashMap<>();
    LinkedHashMap<Competitor, ArrayList<Edge>> allEdges = new LinkedHashMap<>();
    LinkedHashMap<Competitor, ArrayList<Candidate>> candidates = new LinkedHashMap<>();
    Candidate start;
    Candidate end;
    ArrayList<TrackedLeg> legs;

    public CandidateChooser(TimePoint startOfTracking, Iterable<Competitor> competitors, ArrayList<TrackedLeg> legs) {
        this.legs = legs;
        end = new Candidate(legs.size() + 2, null, 1);
        start = new Candidate(0, startOfTracking, 1);
        for (Competitor c : competitors) {
            currentMarkPasses.put(c, new LinkedHashMap<Waypoint, MarkPassing>());
            allEdges.put(c, new ArrayList<Edge>());
            candidates.put(c, new ArrayList<Candidate>());
            candidates.get(c).add(start);
            candidates.get(c).add(end);
            allEdges.get(c).add(new Edge(start, end, 0));
        }
    }

    public void upDateLegs(ArrayList<TrackedLeg> legs) {
        this.legs = legs;
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
        Edge newMostLikelyEdge = null;
        while (!candidateWithParent.keySet().contains(end)) {
            newMostLikelyEdge = null;
            for (Edge e : all) {
                if (candidateWithParent.keySet().contains(e.getStart())) {
                    if (newMostLikelyEdge == null) {
                        newMostLikelyEdge = e;
                    } else if (e.getCost() < newMostLikelyEdge.getCost()) {
                        newMostLikelyEdge = e;
                    }
                }
            }
            if (!candidateWithParent.keySet().contains(newMostLikelyEdge.getEnd())) {
                candidateWithParent.put(newMostLikelyEdge.getEnd(), newMostLikelyEdge.getStart());
            }
            all.remove(newMostLikelyEdge);
        }
        currentMarkPasses.get(co).clear();
        Candidate marker = candidateWithParent.get(end);
        while (!(marker == start)) {
            currentMarkPasses.get(co).put(marker.getWaypoint(),
                    new MarkPassingImpl(marker.getTimePoint(), marker.getWaypoint(), co));
            marker = candidateWithParent.get(marker);
        }
    }

    private void createNewEdges(Competitor co, Candidate newCan) {
        for (Candidate oldCan : candidates.get(co)) {
            Candidate early = newCan;
            Candidate late = oldCan;
            if (oldCan.getID() < newCan.getID()) {
                early = oldCan;
                late = newCan;
            }
            if (early == start && late.getID() == 1) {
                allEdges.get(co).add(new Edge(early, late, numberOfCloseStarts(late.getTimePoint())));
            } else if (late == end) {
                allEdges.get(co).add(new Edge(early, late, 1));
            } else if (early == start) {
                allEdges.get(co).add(new Edge(early, late, estimatedTime(early, late)));
            } else if (!(early.getID() == late.getID()) && late.getTimePoint().after(early.getTimePoint())) {
                allEdges.get(co).add(new Edge(early, late, estimatedTime(early, late)));
            }
        }
    }

    private double numberOfCloseStarts(TimePoint t) {
        double numberOfSimilarStarts = 0;
        double numberOfCompetitors = 0;
        for (Competitor c : candidates.keySet()) {
            numberOfCompetitors++;
            for (Candidate ca : candidates.get(c)) {
                if (ca.getID() == 1 && Math.abs(ca.getTimePoint().asMillis() - t.asMillis()) < 30000) {
                    numberOfSimilarStarts++;
                    break;
                }
            }
        }
        return numberOfSimilarStarts/numberOfCompetitors;
    }

    private double estimatedTime(Candidate c1, Candidate c2) {

        double speedUpwind = 6;
        double speedDownwind = 10;
        double speedReaching = 8;
        TimePoint t = c2.getTimePoint();
        double totalTime = 0;
        for (int i = c1.getID(); i < c2.getID(); i++) {
            if (!(c1.getID() == 0)) {
                double legTime = 0;
                LegType l = LegType.REACHING;
                try {
                    l = legs.get(i - 1).getLegType(t);
                } catch (NoWindException e) {
                }
                double distance = legs.get(i - 1).getGreatCircleDistance(t).getNauticalMiles();
                switch (l) {
                case REACHING:
                    legTime = distance / speedReaching;
                    break;
                case UPWIND:
                    legTime = distance / speedUpwind;
                    break;
                case DOWNWIND:
                    legTime = distance / speedDownwind;
                    break;
                }
                totalTime = totalTime + legTime;
            }
        }
        totalTime = totalTime * 3600000;
        double actualTime = c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis();
        double timeDiff = Math.abs(totalTime - actualTime) / 1000;
        return 1-(Math.log10(timeDiff+1)/20);
    }
}
