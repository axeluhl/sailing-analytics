package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

/**
 * Manages a timer and can auto-advance it at a given acceleration/deceleration rate, with a given delay compared to
 * real time. It can be {@link #pause() paused} and {@link #resume() resumed}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Timer {
    /**
     * The time point represented by this timer
     */
    private Date time;

    /**
     * Listeners interested in changes of {@link #time}
     */
    private final Set<TimeListener> timeListeners;
    
    /**
     * Listeners interested in changes of {@link #playing}
     */
    private final Set<PlayStateListener> playStateListeners;
    
    /**
     * If <code>true</code>, an auto-refreshing command is running with {@link #delayBetweenAutoAdvancesInMilliseconds}
     * as its refresh interval.
     */
    private boolean playing;
    
    /**
     * The refresh interval for auto-updating this timer
     */
    private long delayBetweenAutoAdvancesInMilliseconds;
    
    /**
     * Set to <code>true</code> if the delay changed while {@link #playing}. This forces the auto-refreshing command
     * to re-schedule itself with the new delay, then terminate.
     */
    private boolean delayBetweenAutoAdvancesChanged;
    
    /**
     * Factor by which the timer runs faster than real time. 1.0 means real-time, 2.0 means twice as fast as real time, and so on.
     */
    private double accelerationFactor;
    
    /**
     * The timer is created in resumed mode, using "now" as its current time, 1.0 as its {@link #accelerationFactor acceleration factor} and
     * 1 second (1000ms) as the {@link #delayBetweenAutoAdvancesInMilliseconds delay between automatic updates} should the timer be
     * {@link #resume() started}.
     */
    public Timer() {
        this(1000);
    }
    
    /**
     * The timer is created in resumed mode, using "now" as its current time, 1.0 as its {@link #accelerationFactor
     * acceleration factor} and <code>delayBetweenAutoAdvancesInMilliseconds</code> as the
     * {@link #delayBetweenAutoAdvancesInMilliseconds delay between automatic updates} should the timer be
     * {@link #resume() started}.
     */
    public Timer(long delayBetweenAutoAdvancesInMilliseconds) {
        time = new Date();
        timeListeners = new HashSet<TimeListener>();
        playStateListeners = new HashSet<PlayStateListener>();
        playing = false;
        accelerationFactor = 1.0;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
    }
    
    public void addTimeListener(TimeListener listener) {
        timeListeners.add(listener);
    }
    
    public void removeTimeListener(TimeListener listener) {
        timeListeners.remove(listener);
    }
    
    public void addPlayStateListener(PlayStateListener listener) {
        playStateListeners.add(listener);
    }
    
    public void removePlayStateListener(PlayStateListener listener) {
        playStateListeners.remove(listener);
    }
    
    public void setTime(long timePointAsMillis) {
        Date oldTime = time;
        time = new Date(timePointAsMillis);
        if ((oldTime == null) != (time == null) || (oldTime != null && !oldTime.equals(time))) {
            for (TimeListener listener : timeListeners) {
                listener.timeChanged(time);
            }
        }
    }
    
    public void setAccelerationFactor(double accelerationFactor) {
        this.accelerationFactor = accelerationFactor;
    }
    
    public void setDelayBetweenAutoAdvancesInMilliseconds(long delayBetweenAutoAdvancesInMilliseconds) {
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        if (playing) {
            delayBetweenAutoAdvancesChanged = true;
        }
    }
    
    public long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds;
    }

    public Date getTime() {
        return time;
    }

    /**
     * Pauses this timer after the next time advance. {@link #playing} is set to <code>false</code> if not already
     * paused, and registered {@link PlayStateListener}s will be notified.
     */
    public void pause() {
        if (playing) {
            playing = !playing; // this will cause the repeating command to stop executing
            for (PlayStateListener playStateListener : playStateListeners) {
                playStateListener.playStateChanged(playing);
            }
        }
    }

    /**
     * Resumes this timer if not already {@link #playing}. {@link #playing} is set to <code>true</code>
     * and registered {@link PlayStateListener}s will be notified.
     */
    public void resume() {
        if (!playing) {
            playing = !playing;
            startAutoAdvance();
            for (PlayStateListener playStateListener : playStateListeners) {
                playStateListener.playStateChanged(playing);
            }
        }
    }

    private void startAutoAdvance() {
        RepeatingCommand command = new RepeatingCommand( ) {
            @Override
            public boolean execute() {
                if (time != null) {
                    setTime(time.getTime() + (long) (accelerationFactor * delayBetweenAutoAdvancesInMilliseconds));
                }
                if (delayBetweenAutoAdvancesChanged) {
                    delayBetweenAutoAdvancesChanged = false;
                    scheduleAdvancerCommand(this);
                    return false; // stop this command; use the newly-scheduled one instead that uses the new frequency
                } else {
                    return playing;
                }
            }
        };
        scheduleAdvancerCommand(command);
    }

    private void scheduleAdvancerCommand(RepeatingCommand command) {
        Scheduler.get().scheduleFixedPeriod(command, (int) delayBetweenAutoAdvancesInMilliseconds);
    }
    
    /**
     * Indirect way of setting this timer to a specific point in time, namely to "now" - <code>delayInMilliseconds</code>.
     */
    public void setDelay(long delayInMilliseconds) {
        setTime(System.currentTimeMillis()-delayInMilliseconds);
    }
    
    public long getDelay() {
        return System.currentTimeMillis() - getTime().getTime();
    }

    public boolean isPlaying() {
        return playing;
    }
    
}
