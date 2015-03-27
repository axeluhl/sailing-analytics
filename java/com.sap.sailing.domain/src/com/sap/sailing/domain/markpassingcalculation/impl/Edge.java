package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sse.common.Util;

/**
 * Represent the passage between two {@link Candidate}s, <code>start</code> and <code>end</code>. {@link #getCost()}
 * returns the probability of this passage being correct, consisting of the probability of each Candidate, the
 * probability of the passage between them (an estimate whether the distance sailed between the candidates matches the
 * distance between their {@link Waypoint}s), and a penalty for skipping waypoints, which might happen if a tracker
 * fails or a competitor aborts but should be avoided if possible and is therefore unlikely.
 * 
 * @author Nicolas Klose
 * 
 */

public class Edge implements Comparable<Edge> {
    private final Candidate start;
    private final Candidate end;
    // TODO what is the meaning of this constant?
    private final static double penaltyForSkipped = 0.7;
    // TODO what is the meaning of this constant?
    private final static double penaltyForSkippedToEnd = 0.6;
    private final double estimatedDistanceProbability;
    private final Course course;

    public Edge(Candidate start, Candidate end, double estimatedDistanceProbability, Course course) {
        this.course = course;
        this.start = start;
        this.end = end;
        this.estimatedDistanceProbability = estimatedDistanceProbability;
    }

    public static double getPenaltyForSkipping() {
        return penaltyForSkipped;
    }

    /**
     * The cost for skipping a waypoint is 2*{@link #penaltyForSkipped} but is reduced to {@link #penaltyForSkippedToEnd} when
     * skipping to the end. The reason for preferring skips to the end proxy node is that in live situations where the course
     * hasn't been completed yet it is required to skip to the end. Additional cost comes from the inverse probability of
     * the product of the start node, end node and distance-based probabilities. If these probabilities multiply to zero,
     * an additional cost of 1.0 is added to any skip penalty.
     */
    public Double getCost() {
        double penalty = end.getOneBasedIndexOfWaypoint() == Util.size(course.getWaypoints()) + 1 ? penaltyForSkippedToEnd : penaltyForSkipped;
        return 1 - (start.getProbability() * end.getProbability() * estimatedDistanceProbability) + 2 * penalty
                * (end.getOneBasedIndexOfWaypoint() - start.getOneBasedIndexOfWaypoint() - 1);
    }

    public Candidate getStart() {
        return start;
    }

    public Candidate getEnd() {
        return end;
    }

    public String toString() {
        return "Edge from Waypoint " + start.getOneBasedIndexOfWaypoint() + " to " + end.getOneBasedIndexOfWaypoint() + ": " + getCost();
    }

    // TODO Only for Debugging!
    @Override
    public int compareTo(Edge o) {
        return start != o.getStart() ? start.compareTo(o.getStart()) : end != o.getEnd() ? end.compareTo(o.getEnd()) : getCost().compareTo(o.getCost());
    }
}