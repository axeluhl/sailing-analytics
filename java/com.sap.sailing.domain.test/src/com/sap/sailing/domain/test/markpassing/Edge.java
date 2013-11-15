package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private int numberOfLegs;
    private final int penaltyForSkipped;

    public Edge(Candidate start, Candidate end) {
        this.start = start;
        this.end = end;
        numberOfLegs = end.getID() - start.getID();
        penaltyForSkipped = 1000;
    }
    public double getCost() {
        return start.getCost() + end.getCost() + (numberOfLegs-1)*penaltyForSkipped;
       
    }
    public Candidate getStart() {
        return start;
    }
    public Candidate getEnd() {
        return end;
    }
}