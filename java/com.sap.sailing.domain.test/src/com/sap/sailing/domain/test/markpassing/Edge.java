package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private final int penaltyForSkipped;
    double timeEstimation;

    public Edge(Candidate start, Candidate end, double timeEstimation) {
        this.start = start;
        this.end = end;
        penaltyForSkipped = 1;
        this.timeEstimation = timeEstimation;
    }
    public String getIDs(){
        return start.getID()+"-"+end.getID();
    }
    public double getCost() {
        return start.getCost() + end.getCost() + ((end.getID()-start.getID())-1)*penaltyForSkipped;// + 10*timeEstimation;
    }
    public Candidate getStart() {
        return start;
    }
    public Candidate getEnd() {
        return end;
    }
}