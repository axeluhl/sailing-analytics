package com.sap.sailing.domain.test.markpassing;

public class Edge {
    private Candidate start;
    private Candidate end;
    private int numberOfLegs;
    private final int penaltyForSkipped;
    double timeDifference;

    public Edge(Candidate start, Candidate end, double estimatedTimeBetweenWaypoints) {
        this.start = start;
        this.end = end;
        numberOfLegs = end.getID() - start.getID();
        timeDifference = Math.abs(estimatedTimeBetweenWaypoints
                - end.getTimePoint().minus(start.getTimePoint().asMillis()).asMillis());
        penaltyForSkipped = 1000;
    }
    public double getCost() {
        return start.getCost() + end.getCost() + timeDifference/1000 + (numberOfLegs-1)*penaltyForSkipped;
        //TODO Fix weight
       
    }
    public Candidate getStart() {
        return start;
    }
    public Candidate getEnd() {
        return end;
    }
}