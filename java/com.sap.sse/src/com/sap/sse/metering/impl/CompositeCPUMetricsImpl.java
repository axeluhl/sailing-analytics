package com.sap.sse.metering.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import com.sap.sse.metering.CPUMeter;
import com.sap.sse.metering.CPUMetrics;
import com.sap.sse.metering.CompositeCPUMetrics;
import com.sap.sse.metering.ExecutorWithCPUMeter;

/**
 * Starts out with a single local {@link CPUMeter} that can is used for any CPU metering
 * performed through the {@link ExecutorWithCPUMeter} methods. Further component {@link CPUMetrics}
 * can be {@link #add(CPUMetrics) added} whose metrics collected will then be added to the
 * metrics collected locally.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompositeCPUMetricsImpl extends CPUMeterImpl implements CompositeCPUMetrics {
    private ConcurrentSkipListSet<CPUMetrics> componentCPUMetrics;
    
    public CompositeCPUMetricsImpl() {
        componentCPUMetrics = new ConcurrentSkipListSet<>();
    }

    @Override
    public void add(CPUMetrics cpuMetrics) {
        componentCPUMetrics.add(cpuMetrics);
    }

    @Override
    public Iterable<CPUMetrics> getComponentMetrics() {
        final List<CPUMetrics> result = new ArrayList<>(componentCPUMetrics.size()+1);
        result.addAll(componentCPUMetrics);
        result.add(this);
        return result;
    }
}
