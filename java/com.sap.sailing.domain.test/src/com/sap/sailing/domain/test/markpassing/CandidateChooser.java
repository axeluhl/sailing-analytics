package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;

public class CandidateChooser {
    
    ArrayList<Edge> current;
    ArrayList<Edge> all;
    ArrayList<Candidate> candidates;
    LinkedHashMap<Waypoint, DynamicGPSFixTrack<Mark, GPSFix>> waypointTracks;
    TimePoint startOfRace;
    
    public CandidateChooser(TimePoint startOfRace, LinkedHashMap<Waypoint, DynamicGPSFixTrack<Mark, GPSFix>> waypointTracks){
        this.startOfRace = startOfRace;
        this.waypointTracks = waypointTracks;
        candidates.add(new Candidate(0, startOfRace, 0));
        candidates.add(new Candidate(waypointTracks.size(), null, 0));
    }
    
    public void changeStartOfRace(){
        //TODO!!
    }
    
    public void addCandidate(){
        //TODO
    }
    public void removeCandidate(){
        //TODO
    }
    public void addMarkFix(){
        //TODO
    }
    
    
    
    ////////////////////////////

    public LinkedHashMap<Integer, TimePoint> getMarkPasses(ArrayList<Candidate> candidates, Candidate start,
            Candidate end, LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions) {

        // Create Edges
        ArrayList<Edge> edges = new ArrayList<>();
        for (Candidate c1 : candidates) {
            for (Candidate c2 : candidates) {
                if (c1.getID() == end.getID() || c2.getID() == end.getID()) {
                    edges.add(new Edge(c1, c2));
                } else {
                    if (c1.getID() < c2.getID() && c2.getTimePoint().after(c1.getTimePoint())&& averageSpeed(c1, c2, markPositions)) {
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
            LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPositions) {

        double distance = 0;
        TimePoint tp = c2.getTimePoint();
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (Waypoint w : markPositions.keySet()) {
            waypoints.add(w);
        }

        // TODO Get real speeds!!
        double minimumSpeed = 5;
        double maximumSpeed = 9;
        double averageSpeed = (maximumSpeed+minimumSpeed)/2;
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
