package com.sap.sse.metering.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.sap.sse.common.Duration;
import com.sap.sse.concurrent.RunnableWithException;
import com.sap.sse.concurrent.RunnableWithResult;
import com.sap.sse.concurrent.RunnableWithResultAndException;
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
public class CompositeCPUMetricsImpl implements CompositeCPUMetrics {
    private ConcurrentMap<CPUMetrics, Boolean> componentCPUMetrics;
    private final CPUMeter localCPUMeter;
    
    public CompositeCPUMetricsImpl() {
        componentCPUMetrics = new ConcurrentHashMap<>();
        localCPUMeter = CPUMeter.create();
    }

    @Override
    public void add(CPUMetrics cpuMetrics) {
        componentCPUMetrics.put(cpuMetrics, true);
    }
    
    @Override
    public Map<String, Duration> getTotalCPUTimesByKey() {
        return getTimesByKey(CPUMetrics::getTotalCPUTimesByKey);
    }

    private Map<String, Duration> getTimesByKey(final Function<CPUMetrics, Map<String, Duration>> timesMethod) {
        final Map<String, Duration> result = new HashMap<>();
        for (final CPUMetrics componentMetric : getComponentMetrics()) {
            for (final Entry<String, Duration> e : timesMethod.apply(componentMetric).entrySet()) {
                result.merge(e.getKey(), e.getValue(), (sum, value)->sum.plus(value));
            }
        }
        return result;
    }

    @Override
    public Map<String, Duration> getTotalCPUTimesInUserModeByKey() {
        return getTimesByKey(CPUMetrics::getTotalCPUTimesInUserModeByKey);
    }

    private Iterable<CPUMetrics> getComponentMetrics() {
        final List<CPUMetrics> result = new ArrayList<>(componentCPUMetrics.size()+1);
        result.addAll(componentCPUMetrics.keySet());
        result.add(localCPUMeter);
        return result;
    }

    @Override
    public void runWithCPUMeter(Runnable runnable, String key) {
        localCPUMeter.runWithCPUMeter(runnable, key);
    }

    @Override
    public <T, E extends Throwable> T callWithCPUMeterWithException(RunnableWithResultAndException<T, E> callable, String key) throws E {
        return localCPUMeter.callWithCPUMeterWithException(callable, key);
    }

    @Override
    public <T> T callWithCPUMeter(RunnableWithResult<T> callable, String key) {
        return localCPUMeter.callWithCPUMeter(callable, key);
    }

    @Override
    public <E extends Exception> void runWithCPUMeter(RunnableWithException<E> runnableWithException, String key) throws E {
        localCPUMeter.runWithCPUMeter(runnableWithException, key);
    }
}
