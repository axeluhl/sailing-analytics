package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Waypoint;

/**
 * Represent the passage between two {@link Candidate}s, <code>start</code> and <code>end</code>.
 * {@link #getProbability()} returns the probability of this passage being correct, consisting of the probability of
 * each Candidate, the probability of the passage (e.g. an estimate whether the distance sailed between the candidates matches the
 * distance between their {@link Waypoint}s), and a penalty for skipping waypoints, which might happen if a tracker fails
 * or a competitor aborts but should be avoided if possible.
 * 
 * @author Nicolas Klose
 * 
 */

public class Edge implements Comparable<Edge> {
    private Candidate start;
    private Candidate end;
    private static int numberOfWaypoints;
    private static double penaltyForSkipped = 0.7;
    private static double penaltyForSkippedToEnd = 0.6;
    private double timeEstimationOrCloseStartsProbability;

    public Edge(Candidate start, Candidate end, double timeEstimationOrStartAnalysis) {
        this.start = start;
        this.end = end;
        this.timeEstimationOrCloseStartsProbability = timeEstimationOrStartAnalysis;
    }

    public static double getPenaltyForSkipping() {
        return penaltyForSkipped;
    }

    public static void setNumberOfWayoints(int number) {
        numberOfWaypoints = number;
    }

    public Double getProbability() {
        double penalty = end.getID() == numberOfWaypoints + 1 ? penaltyForSkippedToEnd : penaltyForSkipped;
        return 1-(start.getProbability() * end.getProbability() * timeEstimationOrCloseStartsProbability) + 2 * penalty * (end.getID() - start.getID() - 1);
    }

    public Candidate getStart() {
        return start;
    }

    public Candidate getEnd() {
        return end;
    }

    public String toString() {
        return "From ID " + start.getID() + " to " + end.getID() + ": " + getProbability();
    }

    @Override
    public int compareTo(Edge o) {
        return start != o.getStart() ? start.compareTo(o.getStart()) : end != o.getEnd() ? end.compareTo(o.getEnd()) : getProbability().compareTo(o.getProbability());
    }
}