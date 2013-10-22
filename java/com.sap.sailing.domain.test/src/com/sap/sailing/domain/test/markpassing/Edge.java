package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private int skippedWaypoints;

    public Edge(Candidate start, Candidate end) {
        this.start = start;
        this.end = end;
        skippedWaypoints = end.getID() - start.getID() - 1;
    }
    public double getCost() {
        return start.getCost() + end.getCost() + (skippedWaypoints * 1500); //TODO Add estimation for time passed between waypoints
    }
    public Candidate getStart(){
        return start;
    }
    public Candidate getEnd(){
        return end;
    }
}