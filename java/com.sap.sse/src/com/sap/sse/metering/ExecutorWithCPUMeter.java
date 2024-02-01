package com.sap.sse.metering;

import java.util.concurrent.Callable;

import com.sap.sse.concurrent.RunnableWithException;
import com.sap.sse.concurrent.RunnableWithResult;
import com.sap.sse.concurrent.RunnableWithResultAndException;

/**
 * A lean interface, extended by {@link CPUMeter}, offering methods to <em>run</em> code in "metering" mode.
 * It allows types to expose only the aspect of running code in metered mode in some metering scope, without
 * having to expose the reading parts of the API.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ExecutorWithCPUMeter {
    void runWithCPUMeter(Runnable runnable, String key);
    
    <T, E extends Throwable> T callWithCPUMeterWithException(RunnableWithResultAndException<T, E> callable, String key) throws E;
    
    <T> T callWithCPUMeter(RunnableWithResult<T> callable, String key);
    
    <E extends Exception> void runWithCPUMeter(RunnableWithException<E> runnableWithException, String key) throws E;
    
    default void runWithCPUMeter(Runnable runnable) {
        runWithCPUMeter(runnable, /* key */ null);
    }
    
    /**
     * Wraps the {@code runnableWithException} passed such that when runnable returned is executed, it will meter the
     * CPU consumption with this CPU meter. The consumption will be assigned to the {@code null} key.
     */
    default Runnable cpuMeter(Runnable runnable) {
        return ()->runWithCPUMeter(runnable);
    }

    default <T, E extends Throwable> T callWithCPUMeterWithException(RunnableWithResultAndException<T, E> callable) throws E {
        return callWithCPUMeterWithException(callable, /* key */ null);
    }
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter. The consumption will be assigned to the {@code null} key.
     */
    default <T, E extends Throwable> RunnableWithResultAndException<T, E> cpuMeter(RunnableWithResultAndException<T, E> callable) {
        return ()->callWithCPUMeterWithException(callable);
    }
    
    default <T> T callWithCPUMeter(RunnableWithResult<T> callable) {
        return callWithCPUMeter(callable, /* key */ null);
    }
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter. The consumption will be assigned to the {@code null} key.
     */
    default <T> RunnableWithResult<T> cpuMeter(RunnableWithResult<T> callable) {
        return ()->callWithCPUMeter(callable);
    }
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter.
     * 
     * @param key
     *            the key to assign the runnable's CPU consumption to
     */
    default <T> RunnableWithResult<T> cpuMeter(RunnableWithResult<T> callable, String key) {
        return ()->callWithCPUMeter(callable, key);
    }
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter. The consumption will be assigned to the {@code null} key.
     */
    default <T> Callable<T> cpuMeterCallable(Callable<T> callable) {
        return ()->cpuMeter((RunnableWithResultAndException<T, Exception>) callable::call, /* key */ null).run();
    }
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter.
     * 
     * @param key
     *            the key to assign the runnable's CPU consumption to
     */
    default <T> Callable<T> cpuMeterCallable(Callable<T> callable, String key) {
        return ()->cpuMeter((RunnableWithResultAndException<T, Exception>) callable::call, key).run();
    }
    
    default <E extends Exception> void runWithCPUMeter(RunnableWithException<E> runnableWithException) throws E {
        runWithCPUMeter(runnableWithException, /* key */ null);
    }
    
    /**
     * Wraps the {@code runnableWithException} passed such that when runnable returned is executed, it will meter the
     * CPU consumption with this CPU meter. The consumption will be assigned to the {@code null} key.
     */
    default <E extends Exception> RunnableWithException<E> cpuMeter(RunnableWithException<E> runnableWithException) {
        return ()->runWithCPUMeter(runnableWithException);
    }

    /**
     * Wraps the {@code runnable} passed such that when runnable returned is executed, it will meter the CPU consumption
     * with this CPU meter.
     * 
     * @param key
     *            the key to assign the runnable's CPU consumption to
     */
    default Runnable cpuMeter(Runnable runnable, String key) {
        return ()->runWithCPUMeter(runnable, key);
    }

    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter.
     * 
     * @param key
     *            the key to assign the callable's CPU consumption to
     */
    default <T, E extends Throwable> RunnableWithResultAndException<T, E> cpuMeter(RunnableWithResultAndException<T, E> callable, String key) {
        return ()->callWithCPUMeterWithException(callable, key);
    }

    /**
     * Wraps the {@code runnableWithException} passed such that when runnable returned is executed, it will meter the
     * CPU consumption with this CPU meter.
     * 
     * @param key
     *            the key to assign the runnable's CPU consumption to
     */
    default <E extends Exception> RunnableWithException<E> cpuMeter(RunnableWithException<E> runnableWithException, String key) {
        return ()->runWithCPUMeter(runnableWithException, key);
    }
}