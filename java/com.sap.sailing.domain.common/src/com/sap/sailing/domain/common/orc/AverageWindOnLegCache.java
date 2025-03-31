package com.sap.sailing.domain.common.orc;

import java.util.function.Function;

import com.sap.sailing.domain.common.Wind;

public interface AverageWindOnLegCache {
    /**
     * Caches the legs by their {@link Object#equals(Object) equality and allows for one wind reading per such leg.
     * If you want to map differently-scaled "variants" of the same original leg (e.g., because of different distances
     * traveled on that leg) as the same key then you'll have to come up with a wrapper around your leg that
     * has a definition of equality using only the underlying original leg for comparison.
     */
    <L extends ORCPerformanceCurveLeg> Wind getAverageWind(L leg, Function<L, Wind> averageWindForLegSupplier);
}
