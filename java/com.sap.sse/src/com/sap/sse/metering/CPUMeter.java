package com.sap.sse.metering;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Callable;

import com.sap.sse.concurrent.RunnableWithException;
import com.sap.sse.metering.impl.CPUMeterImpl;

/**
 * Helps with metering CPU resource consumption. The interface offers different methods that can be used to execute
 * code, such as a {@link Runnable} or a {@link Callable} or a {@link RunnableWithException}, each in the current
 * thread, while adding the CPU time consumed to this meter.
 * <p>
 * 
 * CPU load can be grouped by providing different keys for different "types" of CPU load. For example, a client may
 * distinguish between "background" (low priority) and "foreground" (high priority) load; or between load used to
 * refresh cashes and load used to respond to client requests. These keys are simply {@link String}s, and it's up to the
 * users of this interface to define their semantics. The key may also be left out, corresponding to using the
 * {@code null} key. Simplified methods exist for this case.
 * <p>
 * 
 * The aggregated results of metering CPU can be obtained at any time, either by key, or as the gross sum, either
 * distinguishing between user and system mode, or total CPU time.
 * <p>
 * 
 * CPU load is measured using the {@link ThreadMXBean} offered through the {@link ManagementFactory}. It may distinguish
 * "user" and "system" load. See also {@link ThreadMXBean#getCurrentThreadUserTime()} and
 * {@link ThreadMXBean#getCurrentThreadCpuTime()}. Implementations shall ensure that
 * {@link ThreadMXBean#isThreadCpuTimeEnabled() measuring CPU time is supported}, e.g., by invoking
 * {@link ThreadMXBean#setThreadCpuTimeEnabled(boolean) ThreadMXBean.setThreadCpuTimeEnabled(true)}.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CPUMeter extends ExecutorWithCPUMeter, CPUMetrics {
    static CPUMeter create() {
        return new CPUMeterImpl();
    }
}