package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
                if (c1.getID() == markPositions.size() + 1 || c2.getID() == markPositions.size() + 1) {
                    edges.add(new Edge(c1, c2));
                } else {
                    if (c1.getID() < c2.getID() && c2.getTimePoint().after(c1.getTimePoint())&& averageSpeed(c1, c2, markPositions, legs)) {
                        edges.add(new Edge(c1, c2));
                    }
                }
            }
        }

        // Find cheapest Edges
        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<>();
        Edge startingEdge = new Edge(start, start);

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

    private boolean averageSpeed(Candidate c1, Candidate c2,
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions, List<String> legs) {

        double distance = 0;
        TimePoint tp = c2.getTimePoint();
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (Waypoint w : markPositions.keySet()) {
            waypoints.add(w);
        }

        // TODO Get real speeds!!
        double minimumSpeed = 5;
        double maximumSpeed = 9;
        double averageSpeed = 7;
        double delta = maximumSpeed-minimumSpeed;
        
        if(c1.getID() == 0 && c2.getID() == 1 && c2.getTimePoint().asMillis()-c1.getTimePoint().asMillis() < 300000){
           return true;
        }
        for (int i = c1.getID(); i < c2.getID(); i++) {
            if (!(i == 0)) {
                distance = distance
                        + markPositions.get(waypoints.get(i - 1)).get(0).get(tp)
                                .getDistance(markPositions.get(waypoints.get(i)).get(0).get(tp)).getNauticalMiles();
            }
        }
        double time = c2.getTimePoint().asMillis() - c1.getTimePoint().asMillis();
        double speed = distance / (time / 3600000);
        if(Math.abs(speed-averageSpeed)<delta){
            return true;
        }
        return false;
    }
}
