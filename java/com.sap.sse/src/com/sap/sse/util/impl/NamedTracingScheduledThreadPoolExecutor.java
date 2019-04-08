package com.sap.sse.util.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.concurrent.ConcurrentWeakHashMap;

/**
 * A scheduled thread pool executor with a name that produces {@link Future} tasks that write a trace message
 * to the log after their {@link Future#get()} method hasn't produced a result for some time, then keep trying.
 * The tasks are linked to this executor, also when a {@link KnowsExecutor} task is submitted to the {@link #execute(Runnable)}
 * method, so that when they need to trace they
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class NamedTracingScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private static final Logger logger = Logger.getLogger(NamedTracingScheduledThreadPoolExecutor.class.getName());
    private final String name;
    
    /**
     * If logging level is at least {@link Level#FINE}, each runnable's start time point is recorded in the
     * {@link #beforeExecute(Thread, Runnable)} method. If at this logging level, the
     * {@link #afterExecute(Runnable, Throwable)} method calculates the runnable's duration, and if above the
     * {@link #DURATION_LOGGING_THRESHOLD}, writes a log entry.
     */
    private final ConcurrentHashMap<Runnable, TimePoint> startTimePoints = new ConcurrentHashMap<>();
    
    /**
     * When logging {@link Level#FINE}, the {@link #schedule(Runnable, long, TimeUnit)} variants produce wrappers
     * around the runnables whose content is nulled when {@link #afterExecute(Runnable, Throwable)} is called. In order
     * to be able to log long-running runnables correctly, this map stores the connection between the wrapping
     * future and the runnable wrapped by it.
     */
    private final ConcurrentWeakHashMap<ScheduledFuture<?>, Runnable> wrappedRunnables = new ConcurrentWeakHashMap<>();
    
    private final Duration DURATION_LOGGING_THRESHOLD = Duration.ONE_SECOND.times(5);
    
    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
        this.name = name;
    }

    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize, ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
        this.name = name;
    }

    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        this.name = name;
    }

    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize) {
        super(corePoolSize);
        this.name = name;
    }
    
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return new ThreadPoolAwareRunnableScheduledFutureDelegate<>(this, task);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return new ThreadPoolAwareRunnableScheduledFutureDelegate<>(this, task);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ThreadPoolAwareFutureTask<T>(this, runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ThreadPoolAwareFutureTask<T>(this, callable);
    }

    @Override
    public void execute(Runnable command) {
        if (command instanceof KnowsExecutor) {
            ((KnowsExecutor) command).setExecutorThisTaskIsScheduledFor(this);
        }
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if (logger.isLoggable(Level.FINE)) {
            startTimePoints.put(r, MillisecondsTimePoint.now());
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (logger.isLoggable(Level.FINE)) {
            final TimePoint end = MillisecondsTimePoint.now();
            final TimePoint startTime = startTimePoints.remove(r);
            Runnable theRunnableToLog = null;
            if (r instanceof ScheduledFuture<?>) {
                theRunnableToLog = wrappedRunnables.get((ScheduledFuture<?>) r);
            }
            if (theRunnableToLog == null) {
                theRunnableToLog = r;
            }
            if (startTime != null) {
                final Duration duration = startTime.until(end);
                if (duration.compareTo(DURATION_LOGGING_THRESHOLD) > 0) {
                    logger.fine("Runnable "+theRunnableToLog+" took "+duration+" to complete in executor "+this+(t==null?"":(". It failed with exception "+t)));
                }
            } else {
                logger.fine("Strange internal problem: had expected to find start time for "+r);
            }
        }
        super.afterExecute(r, t);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        final ScheduledFuture<?> result = super.schedule(command, delay, unit);
        if (logger.isLoggable(Level.FINE)) {
            // remember which runnable was wrapped by the future because it will have been nulled out when afterExecute is called
            wrappedRunnables.put(result, command);
        }
        return result;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        final ScheduledFuture<?> result = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        if (logger.isLoggable(Level.FINE)) {
            // remember which runnable was wrapped by the future because it will have been nulled out when afterExecute is called
            wrappedRunnables.put(result, command);
        }
        return result;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        final ScheduledFuture<?> result = super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        if (logger.isLoggable(Level.FINE)) {
            // remember which runnable was wrapped by the future because it will have been nulled out when afterExecute is called
            wrappedRunnables.put(result, command);
        }
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "[name=" + name + "]";
    }
}
