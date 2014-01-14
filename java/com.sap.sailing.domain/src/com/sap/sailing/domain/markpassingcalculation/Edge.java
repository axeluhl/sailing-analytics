package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Waypoint;

/**
 * Represent the passage between two {@link Candidate}s, <code>start</code> and <code>end</code>.
 * {@link #getProbability()} returns the probability of this passage being correct, consisting of the probability of
 * each Candidate, the probability of the passage (e.g. an estimate whether the time between the candidates matches the
 * distance between their {@link Waypoint}s), and a penalty for skipping waypoints, which is possible if a tracker fails
 * or a competitor aborts but should be avoided if possible.
 * 
 * @author Nicolas Klose
 * 
 */

public class Edge {
    private Candidate start;
    private Candidate end;
    static double penaltyForSkipped = 0.9;
    private double timeEstimationOrCloseStartsProbability;

    public Edge(Candidate start, Candidate end, double timeEstimationOrStartAnalysis) {
        this.start = start;
        this.end = end;
        this.timeEstimationOrCloseStartsProbability = timeEstimationOrStartAnalysis;
    }

    public String getIDs() {
        return start.getID() + "-" + end.getID();
    }
    
    public double getProbability() {
            return 1 - (start.getProbability() * end.getProbability() * timeEstimationOrCloseStartsProbability)
                    + 2*penaltyForSkipped * (end.getID() - start.getID()) - 1;
    }

    public Candidate getStart() {
        return start;
    }

    public Candidate getEnd() {
        return end;
    }
    public String toString(){
        return "From ID " + start.getID()+ " to "+end.getID()+": "+ (1 - (start.getProbability() * end.getProbability() * timeEstimationOrCloseStartsProbability)
                    + 2*penaltyForSkipped * (end.getID() - start.getID()) - 1);
    }
}