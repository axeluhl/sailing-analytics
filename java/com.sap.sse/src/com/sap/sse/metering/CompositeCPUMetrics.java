package com.sap.sse.metering;

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
}
