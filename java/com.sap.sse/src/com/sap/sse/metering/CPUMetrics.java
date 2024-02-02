package com.sap.sse.metering;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.sap.sse.common.Duration;
import com.sap.sse.concurrent.RunnableWithException;

/**
 * Exposes aggregated CPU metrics collected with an {@link ExecutorWithCPUMeter}. CPU metrics can be recorded
 * with specific "key" {@link String}s or with a default {@code null} key. They are automatically split in
 * user, system and total time spent.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CPUMetrics {
    Map<String, Duration> getTotalCPUTimesByKey();
    
    Map<String, Duration> getTotalCPUTimesInUserModeByKey();
    
    /**
     * @return the total CPU time measured by this meter, aggregated across all keys, and regardless of whether CPU time
     *         was spent in user or system mode
     */
    default Duration getTotalCPUTime() {
        Duration result = Duration.NULL;
        for (final Duration d : getTotalCPUTimesByKey().values()) {
            result = result.plus(d);
        }
        return result;
    }
    
    /**
     * @return the total CPU time measured by this meter spent in user mode, aggregated across all keys
     */
    default Duration getTotalCPUTimeInUserMode() {
        Duration result = Duration.NULL;
        for (final Duration d : getTotalCPUTimesInUserModeByKey().values()) {
            result = result.plus(d);
        }
        return result;
    }
    
    /**
     * @return the total CPU time measured by this meter spent in system mode, aggregated across all keys
     */
    default Duration getTotalCPUTimeInSystemMode() {
        return getTotalCPUTime().minus(getTotalCPUTimeInUserMode());
    }
    
    default Map<String, Duration> getTotalCPUTimesInSystemModeByKey() {
        final Map<String, Duration> result = new HashMap<>();
        for (final Entry<String, Duration> e: getTotalCPUTimesByKey().entrySet()) {
            result.put(e.getKey(), e.getValue().minus(getTotalCPUTimeInUserMode(e.getKey())));
        }
        return result;
    }

    /**
     * @param key
     *            the key used in any of {@link #runWithCPUMeter(Runnable, String)}, {@link #runWithCPUMeter(RunnableWithException, String)}, or
     *            {@link #callWithCPUMeterWithException(Callable, String)}; may be {@code null}
     * @return the total CPU time measured by this meter, aggregated for the {@code key} specified, and regardless of
     *         whether CPU time was spent in user or system mode
     */
    default Duration getTotalCPUTime(String key) {
        return getTotalCPUTimesByKey().get(key);
    }

    /**
     * @param key
     *            the key used in any of {@link #runWithCPUMeter(Runnable, String)}, {@link #runWithCPUMeter(RunnableWithException, String)}, or
     *            {@link #callWithCPUMeterWithException(Callable, String)}; may be {@code null}
     * @return the total CPU time measured by this meter spent in user mode, aggregated for the {@code key} specified
     */
    default Duration getTotalCPUTimeInUserMode(String key) {
        return getTotalCPUTimesInUserModeByKey().get(key);
    }

    /**
     * @param key
     *            the key used in any of {@link #runWithCPUMeter(Runnable, String)}, {@link #runWithCPUMeter(RunnableWithException, String)}, or
     *            {@link #callWithCPUMeterWithException(Callable, String)}; may be {@code null}
     * @return the total CPU time measured by this meter spent in system mode, aggregated for the {@code key} specified
     */
    default Duration getTotalCPUTimeInSystemMode(String key) {
        return getTotalCPUTime().minus(getTotalCPUTimeInUserMode());
    }
}
