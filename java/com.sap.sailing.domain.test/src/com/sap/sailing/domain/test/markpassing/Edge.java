package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private final double penaltyForSkipped;
    double timeEstimationOrCloseStartsProbability;

    public Edge(Candidate start, Candidate end, double timeEstimationOrStartAnalysis) {
        this.start = start;
        this.end = end;
        penaltyForSkipped = 0.45;
        this.timeEstimationOrCloseStartsProbability = timeEstimationOrStartAnalysis;
    }
    public String getIDs(){
        return start.getID()+"-"+end.getID();
    }
    public double getProbability() {
        return 1-(start.getProbability() * end.getProbability() * timeEstimationOrCloseStartsProbability) + penaltyForSkipped * (end.getID()-start.getID())-1;
    }
    public Candidate getStart() {
        return start;
    }
    public Candidate getEnd() {
        return end;
    }
}