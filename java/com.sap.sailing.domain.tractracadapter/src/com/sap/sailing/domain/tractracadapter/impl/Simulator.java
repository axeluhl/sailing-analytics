package com.sap.sailing.domain.tractracadapter.impl;

import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.WindImpl;

public class Simulator {
    private static final Logger logger = Logger.getLogger(Simulator.class.getName());
    
    private DynamicTrackedRace trackedRace;
    private WindStore windStore;
    private long advanceInMillis = -1;
    /**
     * Creates a wind store which replays the wind store events against a tracked race, correcting the wind fixes with
     * the simulation delay. The wind store returned is an {@link EmptyWindStore}.
     */
    public WindStore simulatingWindStore(WindStore windStore) {
        this.windStore = windStore;
        return EmptyWindStore.INSTANCE;
    }

    public synchronized void setTrackedRace(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
        startWindPlayer();
    }
    
    /**
     * This is what everybody is waiting for :-). Notifies all waiters.
     */
    public synchronized void setAdvanceInMillis(long advanceInMillis) {
        this.advanceInMillis = advanceInMillis;
        notifyAll();
    }

    /**
     * Launches a thread which re-plays all wind fixes from {@link #windStore}'s
     * {@link WindStore#loadWindTracks(com.sap.sailing.domain.tracking.TrackedRegatta, com.sap.sailing.domain.tracking.TrackedRace, long)
     * loaded tracks} on {@link #trackedRace}.
     */
    private void startWindPlayer() {
        assert this.trackedRace != null;
        assert this.windStore != null;
        for (final Map.Entry<? extends WindSource, ? extends WindTrack> windSourceAndTrack : windStore.loadWindTracks(
                trackedRace.getTrackedRegatta(), trackedRace,
                /* millisecondsOverWhichToAverageWind doesn't matter because we only use raw fixes */ 10000).entrySet()) {
            new Thread("Wind simulator for wind source "+windSourceAndTrack.getKey()+" for tracked race "+trackedRace.getRace().getName()) {
                @Override
                public void run() {
                    for (Wind wind : windSourceAndTrack.getValue().getRawFixes()) {
                        trackedRace.recordWind(delayWind(wind), windSourceAndTrack.getKey());
                    }
                }
            }.start();
        }
    }
    
    private Wind delayWind(Wind wind) {
        TimePoint delayedTimePoint = delay(wind.getTimePoint());
        return new WindImpl(wind.getPosition(), delayedTimePoint, new KnotSpeedWithBearingImpl(wind.getKnots(), wind.getBearing()));
    }
    
    /**
     * Waits until {@link #advanceInMillis} is set to something not equal to -1 which is its initial value. Unblocked by
     * {@link #setAdvanceInMillis(long)}.
     */
    private synchronized long getAdvanceInMillis() {
        while (advanceInMillis == -1) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.throwing(Simulator.class.getName(), "getAdvanceInMillis", e);
                // ignore; try again
            }
        }
        return advanceInMillis;
    }

    /**
     * Transforms <code>timed</code>'s time point according to this simulator's delay and waits roughly until this time
     * has passed. Then, returns the adjusted time point which therefore should roughly equal "now."
     */
    public TimePoint delay(TimePoint timePoint) {
        long now = System.currentTimeMillis();
        TimePoint transformedTimed = advance(timePoint);
        long waitTimeInMillis = transformedTimed.asMillis() - now;
        do {
            if (waitTimeInMillis > 0) {
                try {
                    Thread.sleep(waitTimeInMillis);
                    waitTimeInMillis = 0;
                } catch (InterruptedException e) {
                    // try again:
                    waitTimeInMillis = transformedTimed.asMillis() - System.currentTimeMillis();
                }
            }
        } while (waitTimeInMillis > 0);
        return transformedTimed;
    }

    /**
     * Like {@link #delay}, only that it doesn't wait until <code>timePoint</code> is reached in wall time.
     */
    public TimePoint advance(TimePoint timePoint) {
        return new MillisecondsTimePoint(timePoint.asMillis()+getAdvanceInMillis());
    }

    /**
     * If {@link #advanceInMillis} is already set to a non-negative value, it is left alone, and {@link #delay(TimePoint)} is called.
     * Otherwise, <code>time</code> is taken to be the original start time of the race which is then used to compute
     * {@link #advanceInMillis} such that <code>time.asMillis() + advanceInMillis == System.currentTimeMillis()</code>.
     */
    public TimePoint delayMarkPassingTimePoint(TimePoint time) {
        if (isAdvanceInMilliseSet()) {
            return delay(time);
        } else {
            setAdvanceInMillis(System.currentTimeMillis() - time.asMillis());
            return delay(time);
        }
    }

    private boolean isAdvanceInMilliseSet() {
        return advanceInMillis != -1;
    }
}
