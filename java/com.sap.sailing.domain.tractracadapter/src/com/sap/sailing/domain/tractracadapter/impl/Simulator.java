package com.sap.sailing.domain.tractracadapter.impl;

import java.util.Map;

import com.sap.sailing.domain.base.Timed;
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
    private DynamicTrackedRace trackedRace;
    private WindStore windStore;
    private long advanceInMillis;
    /**
     * Creates a wind store which replays the wind store events against a tracked race, correcting the wind fixes with
     * the simulation delay. The wind store returned is an {@link EmptyWindStore}.
     */
    public WindStore simulatingWindStore(WindStore windStore) {
        this.windStore = windStore;
        return EmptyWindStore.INSTANCE;
    }

    public void setTrackedRace(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
        startWindPlayer();
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
        TimePoint delayedTimePoint = delay(wind);
        return new WindImpl(wind.getPosition(), delayedTimePoint, new KnotSpeedWithBearingImpl(wind.getKnots(), wind.getBearing()));
    }
    
    private long getAdvanceInMillis() {
        return advanceInMillis;
    }

    /**
     * Transforms <code>timed</code>'s time point according to this simulator's delay and waits roughly until this time
     * has passed. Then, returns the adjusted time point which therefore should roughly equal "now."
     */
    private TimePoint delay(Timed timed) {
        long now = System.currentTimeMillis();
        long then = timed.getTimePoint().asMillis();
        long transformedTimed = then + getAdvanceInMillis();
        long waitTimeInMillis = transformedTimed - now;
        do {
            if (waitTimeInMillis > 0) {
                try {
                    Thread.sleep(waitTimeInMillis);
                    waitTimeInMillis = 0;
                } catch (InterruptedException e) {
                    // try again:
                    waitTimeInMillis = transformedTimed - System.currentTimeMillis();
                }
            }
        } while (waitTimeInMillis > 0);
        return new MillisecondsTimePoint(transformedTimed);
    }
}
