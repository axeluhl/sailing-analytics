package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private final double penaltyForSkipped;
    double timeEstimation;

    public Edge(Candidate start, Candidate end, double timeEstimation) {
        this.start = start;
        this.end = end;
        penaltyForSkipped = 0.4;
        this.timeEstimation = timeEstimation;
    }
    public String getIDs(){
        return start.getID()+"-"+end.getID();
    }
    public double getCost() {
        return 1 -(start.getCost() * end.getCost() * timeEstimation *  Math.pow(penaltyForSkipped, (end.getID()-start.getID())-1));
    }
    public Candidate getStart() {
        return start;
    }
    public Candidate getEnd() {
        return end;
    }
}