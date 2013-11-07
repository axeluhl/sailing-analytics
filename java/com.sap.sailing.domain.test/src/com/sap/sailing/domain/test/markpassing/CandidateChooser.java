package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;

public class CandidateChooser {


    public LinkedHashMap<Integer, TimePoint> getMarkPasses(ArrayList<Candidate> candidates, Candidate start,
            Candidate end, LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions,
            ArrayList<String> legs) {

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
        LinkedHashMap<Integer, TimePoint> markPasses = new LinkedHashMap<>();
        Candidate current = end;
        while (!(current == start)) {
            current = candidateWithParent.get(current);
            markPasses.put(current.getID(), current.getTimePoint());
        }
        return markPasses;
    }

    private double getEstimatedTimeBetweenWaypoint(Candidate c1, Candidate c2,
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions, ArrayList<String> legs) {
        double time = 0;
        TimePoint tp = null;

        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (Waypoint w : markPositions.keySet()) {
            waypoints.add(w);
        }
        //TODO Get real average speeds!!
        for (int i = c1.getID(); i < c2.getID()-1; i++) {
                if (!(i == waypoints.size()-1||i == 0)) {
                    double averageSpeed = 7;
                    String lt;
                    lt = legs.get(i - 1).toString();
                    if (lt.equals("UPWIND")) {
                        averageSpeed = 5;
                    }
                    if (lt.equals("DOWNWIND")) {
                        averageSpeed = 9;
                    }
                    if (markPositions.get(waypoints.get(0)).get(0).containsKey(c1.getTimePoint())) {
                        tp = c1.getTimePoint();
                    } else {
                        if (markPositions.get(waypoints.get(0)).get(0).containsKey(c2.getTimePoint())) {
                            tp = c2.getTimePoint();
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
            } else {time = time + 12000;}
        }
        return time;
    }
}