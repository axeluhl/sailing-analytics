package com.sap.sse.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Java8-compliant {@link Timer} specialization that can deal with {@link Runnable}s instead of
 * {@link TimerTask} which makes it combinable with lambdas as {@link Runnable} implementations.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TimerWithRunnable extends Timer {
    private static class TimerTaskWithRunnable extends TimerTask {
        private final Runnable runnable;

        protected TimerTaskWithRunnable(Runnable runnable) {
            super();
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }

    public TimerWithRunnable() {
        super();
    }

    public TimerWithRunnable(boolean isDaemon) {
        super(isDaemon);
    }

    public TimerWithRunnable(String name, boolean isDaemon) {
        super(name, isDaemon);
    }

    public TimerWithRunnable(String name) {
        super(name);
    }

    public void schedule(Runnable task, long delay) {
        super.schedule(new TimerTaskWithRunnable(task), delay);
    }

    public void schedule(Runnable task, Date time) {
        super.schedule(new TimerTaskWithRunnable(task), time);
    }

    public void schedule(Runnable task, long delay, long period) {
        super.schedule(new TimerTaskWithRunnable(task), delay, period);
    }

    public void schedule(Runnable task, Date firstTime, long period) {
        super.schedule(new TimerTaskWithRunnable(task), firstTime, period);
    }

    public void scheduleAtFixedRate(Runnable task, long delay, long period) {
        super.scheduleAtFixedRate(new TimerTaskWithRunnable(task), delay, period);
    }

    public void scheduleAtFixedRate(Runnable task, Date firstTime, long period) {
        super.scheduleAtFixedRate(new TimerTaskWithRunnable(task), firstTime, period);
    }
    
    
}
