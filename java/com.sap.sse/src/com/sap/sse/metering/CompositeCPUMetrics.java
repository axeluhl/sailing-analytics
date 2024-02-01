package com.sap.sse.metering;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.sap.sse.common.Duration;
import com.sap.sse.metering.impl.CompositeCPUMetricsImpl;

/**
 * A "composite" pattern for {@link CPUMetrics}. It sums up the metrics of the component {@link CPUMetrics}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CompositeCPUMetrics extends CPUMeter {
    static CompositeCPUMetrics create() {
        return new CompositeCPUMetricsImpl();
    }
    
    /**
     * Adds another {@link CPUMetrics} component to this composite metrics, unless the {@code cpuMetrics}
     * have already been added earlier; this way, no duplicates can be created.
     */
    void add(CPUMetrics cpuMetrics);
    
    Iterable<CPUMetrics> getComponentMetrics();

    @Override
    default Map<String, Duration> getTotalCPUTimesByKey() {
        return getTimesByKey(CPUMetrics::getTotalCPUTimesByKey);
    }

    default Map<String, Duration> getTimesByKey(final Function<CPUMetrics, Map<String, Duration>> timesMethod) {
        final Map<String, Duration> result = new HashMap<>();
        for (final CPUMetrics componentMetric : getComponentMetrics()) {
            for (final Entry<String, Duration> e : timesMethod.apply(componentMetric).entrySet()) {
                result.merge(e.getKey(), e.getValue(), (sum, value)->sum.plus(value));
            }
        }
        return result;
    }

    @Override
    default Map<String, Duration> getTotalCPUTimesInUserModeByKey() {
        return getTimesByKey(CPUMetrics::getTotalCPUTimesInUserModeByKey);
    }
}
