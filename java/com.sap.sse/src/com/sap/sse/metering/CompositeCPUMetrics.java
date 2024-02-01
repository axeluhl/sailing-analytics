package com.sap.sse.metering;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.sap.sse.common.Duration;

/**
 * A "composite" pattern for {@link CPUMetrics}. It sums up the metrics of the component {@link CPUMetrics}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CompositeCPUMetrics extends CPUMetrics {
    void add(CPUMetrics cpuMetrics);
    
    Iterable<CPUMetrics> getComponentMetrics();

    @Override
    default Map<String, Duration> getTotalCPUTimesByKey() {
        final Supplier<Map<String, Duration>> timesMethod = CPUMetrics::getTotalCPUTimesByKey;
        final Map<String, Duration> result = new HashMap<>();
        for (final CPUMetrics componentMetric : getComponentMetrics()) {
            for (final Entry<String, Duration> e : componentMetric.getTotalCPUTimesByKey().entrySet()) {
                result.merge(e.getKey(), e.getValue(), (sum, value)->sum.plus(value));
            }
        }
        return result;
    }

    @Override
    default Map<String, Duration> getTotalCPUTimesInUserModeByKey() {
        // TODO Auto-generated method stub
        return null;
    }
}
