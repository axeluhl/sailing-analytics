package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;

public class CandidateChooser implements AbstractCandidateChooser {

    @Override
    public ArrayList<TimePoint> getMarkPasses(ArrayList<Candidate> candidates, Candidate start, Candidate end,
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions,
            ArrayList<TrackedLeg> legs) {

        // Create Edges
        ArrayList<Edge> edges = new ArrayList<>();
        for (Candidate c1 : candidates) {
            for (Candidate c2 : candidates) {
                if (c1.getID() < c2.getID() && c2.getTimePoint().after(c1.getTimePoint())) {
                    edges.add(new Edge(c1, c2, getEstimatedTimeBetweenWaypoint(c1, c2, markPositions, legs)));
                }
            }
        }

        // Find cheapest Edges
        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<>();
        Edge startingEdge = new Edge(start, start, 0);

        while (!candidateWithParent.keySet().contains(end)) {
            Edge newCheapestEdge = startingEdge;

            for (Edge e : edges) {
                if (candidateWithParent.keySet().contains(e.getStart()) && e.getCost() < newCheapestEdge.getCost()
                        || newCheapestEdge == startingEdge) {
                    newCheapestEdge = e;
                }
            }
            if (!candidateWithParent.keySet().contains(newCheapestEdge.getEnd())) {
                candidateWithParent.put(newCheapestEdge.getEnd(), newCheapestEdge.getStart());
            }
            edges.remove(newCheapestEdge);
        }
        // Find shortest Path to Start
        ArrayList<TimePoint> markPasses = new ArrayList<>();
        Candidate current = end;
        while (!(current == start)) {
            current = candidateWithParent.get(current);
            markPasses.add(current.getTimePoint());
        }
        markPasses.remove(start.getTimePoint());
        return markPasses;
    }

    private double getEstimatedTimeBetweenWaypoint(Candidate c1, Candidate c2,
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions,
            ArrayList<TrackedLeg> legs) {
        double time = 0;
        TimePoint tp = null;

        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (Waypoint w : markPositions.keySet()) {
            waypoints.add(w);
        }
        tp = c1.getTimePoint().plus(c2.getTimePoint().minus(c1.getTimePoint().asMillis()).asMillis() / 2);

        for (int i = c1.getID(); i < c2.getID(); i++) {
            if (i == 0) {
                time = time + 120000;
            } else {
                if (i == waypoints.size()) {
                    time = time + 120000;
                } else {
                    double averageSpeed = 7;
                    LegType lt;

                    try {
                        lt = legs.get(i - 1).getLegType(tp);
                        if (lt.equals(LegType.UPWIND)) {
                            averageSpeed = 5;
                        }
                        if (lt.equals(LegType.DOWNWIND)) {
                            averageSpeed = 9;
                        }
                    } catch (NoWindException e1) {

                    }

                    if (markPositions.get(waypoints.get(0)).get(0).containsKey(c1.getTimePoint())) {
                        tp = c1.getTimePoint();
                    } else {
                        if (markPositions.get(waypoints.get(0)).get(0).containsKey(c2.getTimePoint())) {
                            tp = c2.getTimePoint();
                        } 
                    }

                    double distance = 0;
                    try {
                        distance = markPositions.get(waypoints.get(i - 1)).get(0).get(tp)
                                .getDistance(markPositions.get(waypoints.get(i)).get(0).get(tp)).getNauticalMiles();
                        time = time + distance / averageSpeed;
                    } catch (NullPointerException e) {
                        time = c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis();
                        break;
                    }
                }
            }
        }

        return time;
    }
}
