package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.function.Supplier;

import com.sap.sailing.domain.markpassingcalculation.Candidate;

/**
 * Instead of eagerly demanding a calculated probability when this edge is constructed, instances of
 * this class accept a {@link Supplier} for the probability at construction time, leading to lazy
 * evaluation. The result will be cached after the first evaluation.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EdgeWithLazyProbability extends Edge {
    private double cachedProbability;
    private boolean probabilityIsCached;
    private final Supplier<Double> estimatedDistanceAndStartTimingProbabilitySupplier;

    public EdgeWithLazyProbability(Candidate start, Candidate end,
            Supplier<Double> estimatedDistanceAndStartTimingProbabilitySupplier, int numberOfWaypoints) {
        super(start, end, numberOfWaypoints);
        this.estimatedDistanceAndStartTimingProbabilitySupplier = estimatedDistanceAndStartTimingProbabilitySupplier;
    }

    @Override
    protected double getEstimatedDistanceAndStartTimingProbability() {
        if (!probabilityIsCached) {
            cachedProbability = estimatedDistanceAndStartTimingProbabilitySupplier.get();
            probabilityIsCached = true;
        }
        return cachedProbability;
    }
}
