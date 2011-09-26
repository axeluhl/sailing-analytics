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
    private final Set<TimeListener> timeListeners;
    private final Set<PlayStateListener> playStateListeners;
    private Date time;
    private boolean playing;
    private long delayBetweenAutoAdvancesInMilliseconds;
    private double accelerationFactor;
    
    public Timer() {
        timeListeners = new HashSet<TimeListener>();
        playStateListeners = new HashSet<PlayStateListener>();
        playing = false;
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

    public Date getTime() {
        return time;
    }

    public void pause() {
        if (playing) {
            playing = !playing; // this will cause the repeating command to stop executing
            for (PlayStateListener playStateListener : playStateListeners) {
                playStateListener.playStateChanged(playing);
            }
        }
    }

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
                return playing;
            }
        };
        Scheduler.get().scheduleFixedPeriod(command, (int) delayBetweenAutoAdvancesInMilliseconds);
    }
    
    public void setDelay(long delayInMilliseconds) {
        setTime(System.currentTimeMillis()-delayInMilliseconds);
    }
}
