package com.sap.sse.metering;

import java.util.concurrent.Callable;

import com.sap.sse.concurrent.RunnableWithException;

/**
 * A lean interface, extended by {@link CPUMeter}, offering methods to <em>run</em> code in "metering" mode.
 * It allows types to expose only the aspect of running code in metered mode in some metering scope, without
 * having to expose the reading parts of the API.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ExecutorWithCPUMeter {
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

    default <T> T callWithCPUMeter(Callable<T> callable) throws Exception {
        return callWithCPUMeter(callable, /* key */ null);
    }
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter. The consumption will be assigned to the {@code null} key.
     */
    default <T> Callable<T> cpuMeter(Callable<T> callable) {
        return ()->callWithCPUMeter(callable);
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

    void runWithCPUMeter(Runnable runnable, String key);
    
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

    <T> T callWithCPUMeter(Callable<T> callable, String key) throws Exception;
    
    /**
     * Wraps the {@code callable} passed such that when callable returned is executed, it will meter the CPU consumption
     * with this CPU meter.
     * 
     * @param key
     *            the key to assign the callable's CPU consumption to
     */
    default <T> Callable<T> cpuMeter(Callable<T> callable, String key) {
        return ()->callWithCPUMeter(callable, key);
    }

    <E extends Exception> void runWithCPUMeter(RunnableWithException<E> runnableWithException, String key) throws E;
    
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