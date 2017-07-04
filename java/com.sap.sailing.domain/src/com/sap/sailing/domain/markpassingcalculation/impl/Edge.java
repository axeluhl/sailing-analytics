package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.function.Supplier;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.markpassingcalculation.Candidate;

/**
 * Represent the passage between two {@link Candidate}s, <code>start</code> and <code>end</code>. {@link #getProbability()}
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

    /**
     * The probability-reducing factor for skipping a waypoint is {@link #PENALTY_FOR_SKIPPED} but is reduced to
     * {@link #PENALTY_FOR_SKIPPED_TO_END} when skipping to the end. The reason for preferring skips to the end proxy
     * node is that in live situations where the course hasn't been completed yet it is required to skip to the end. The
     * main probability comes from the probability of the product of the start node, end node and distance-based
     * probabilities.
     * <p>
     * 
     * Once used, the result is stored in {@link #estimatedDistanceAndStartTimingProbability}, and this field is set to
     * {@code null}.
     */
    private Supplier<Double> estimatedDistanceAndStartTimingProbabilitySupplier;
    
    /**
     * Either provided at construction time or calculated by the {@link #estimatedDistanceAndStartTimingProbabilitySupplier}.
     */
    private double estimatedDistanceAndStartTimingProbability;
    
    /**
     * The penalty for an edge's probability in case the edge skips one or more waypoints and does not
     * end at the end proxy node. Skipping a waypoint that is not the last means that either the waypoint
     * was passed in a very strange, unrecognized way or we got a tracker outage. This is both pretty
     * unlikely and is penalized by this factor being multiplied to the edge's general probability.<p>
     * 
     * This factor is raised to the n-th power for n waypoints skipped.
     */
    private final static double PENALTY_FOR_SKIPPED = 0.1;
    
    /**
     * Similar to {@link #PENALTY_FOR_SKIPPED}, but applied to edges that skip to the end proxy node. Such skips
     * are regular business while the race is still going on, and paths that are otherwise likely but don't lead
     * up to the end shall be possible and therefore must not be penalized harshly, receiving less of a penalty
     * (greater factor).<p>
     * 
     * As with {@link #PENALTY_FOR_SKIPPED}, this factor is raised to the n-th power for n waypoints skipped.
     */
    private final static double PENALTY_FOR_SKIPPED_TO_END = 0.5;

    private final int numberOfWaypoints;

    public Edge(Candidate start, Candidate end, Supplier<Double> estimatedDistanceAndStartTimingProbabilitySupplier, int numberOfWaypoints) {
        this.start = start;
        this.end = end;
        this.estimatedDistanceAndStartTimingProbabilitySupplier = estimatedDistanceAndStartTimingProbabilitySupplier;
        this.numberOfWaypoints = numberOfWaypoints;
    }
    
    public Edge(Candidate start, Candidate end, double estimatedDistanceAndStartTimingProbability, int numberOfWaypoints) {
        this.start = start;
        this.end = end;
        this.estimatedDistanceAndStartTimingProbability = estimatedDistanceAndStartTimingProbability;
        this.numberOfWaypoints = numberOfWaypoints;
    }
    
    public static double getPenaltyForSkipping() {
        return PENALTY_FOR_SKIPPED;
    }

    /**
     * The probability-reducing factor for skipping a waypoint is {@link #PENALTY_FOR_SKIPPED} but is reduced to
     * {@link #PENALTY_FOR_SKIPPED_TO_END} when skipping to the end. The reason for preferring skips to the end proxy node
     * is that in live situations where the course hasn't been completed yet it is required to skip to the end. The main
     * probability comes from the probability of the product of the start node, end node and distance-based
     * probabilities.
     */
    public double getProbability() {
        final double penalty = getEnd().getOneBasedIndexOfWaypoint() == numberOfWaypoints + 1 ? PENALTY_FOR_SKIPPED_TO_END : getPenaltyForSkipping();
        // See bug 3241 comment #38: only use the edge's end candidate's probability; the start candidate's probability is the
        // previous edge's end probability. The only probability we'll miss this way is that of the start proxy node and that is always 1.
        return getEnd().getProbability() * getEstimatedDistanceAndStartTimingProbability() *
                Math.pow(penalty, (getEnd().getOneBasedIndexOfWaypoint() - getStart().getOneBasedIndexOfWaypoint() - 1));
    }

    private double getEstimatedDistanceAndStartTimingProbability() {
        if (estimatedDistanceAndStartTimingProbabilitySupplier != null) {
            estimatedDistanceAndStartTimingProbability = estimatedDistanceAndStartTimingProbabilitySupplier.get();
            estimatedDistanceAndStartTimingProbabilitySupplier = null;
        }
        return estimatedDistanceAndStartTimingProbability;
    }

    public Candidate getStart() {
        return start;
    }

    public Candidate getEnd() {
        return end;
    }

    public String toString() {
        return "Edge from Waypoint " + start.getOneBasedIndexOfWaypoint() + " (" +
                start.getTimePoint()+") to " + end.getOneBasedIndexOfWaypoint() + " (" +
                end.getTimePoint()+"): " + getProbability();
    }

    @Override
    public int compareTo(Edge o) {
        return start != o.getStart() ? start.compareTo(o.getStart()) : end != o.getEnd() ? end.compareTo(o.getEnd()) : Double.compare(getProbability(), o.getProbability());
    }
}