package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private final double penaltyForSkipped;
    double timeEstimationOrStartAnalysis;

    public Edge(Candidate start, Candidate end, double timeEstimationOrStartAnalysis) {
        this.start = start;
        this.end = end;
        penaltyForSkipped = 0.3;
        this.timeEstimationOrStartAnalysis = timeEstimationOrStartAnalysis;
    }
    public String getIDs(){
        return start.getID()+"-"+end.getID();
    }
    public double getLikelyhood() {
        return 1-(start.getLikelyhood() * end.getLikelyhood() * timeEstimationOrStartAnalysis) + penaltyForSkipped * (end.getID()-start.getID())-1;
    }
    public Candidate getStart() {
        return start;
    }
    public Candidate getEnd() {
        return end;
    }
}