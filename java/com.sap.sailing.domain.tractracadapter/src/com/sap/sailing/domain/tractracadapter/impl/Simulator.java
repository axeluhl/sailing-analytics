package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class Simulator {
    private static final Logger logger = Logger.getLogger(Simulator.class.getName());
    
    private DynamicTrackedRace trackedRace;
    private final WindStore windStore;
    private long advanceInMillis = -1;
    private Timer timer = new Timer("Timer for TracTrac Simulator");
    private boolean stopped;
    
    public Simulator(WindStore windStore) {
        super();
        assert windStore != null;
        this.windStore = windStore;
    }

    /**
     * The wind store returned is an {@link EmptyWindStore}.
     */
    public WindStore simulatingWindStore(WindStore windStore) {
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
        for (final Map.Entry<? extends WindSource, ? extends WindTrack> windSourceAndTrack : windStore.loadWindTracks(
                trackedRace.getTrackedRegatta().getRegatta().getName(), trackedRace,
                /* millisecondsOverWhichToAverageWind doesn't matter because we only use raw fixes */ 10000).entrySet()) {
            new Thread("Wind simulator for wind source "+windSourceAndTrack.getKey()+" for tracked race "+trackedRace.getRace().getName()) {
                @Override
                public void run() {
                    final WindTrack windTrack = windSourceAndTrack.getValue();
                    windTrack.lockForRead();
                    try {
                        for (Wind wind : windTrack.getRawFixes()) {
                            if (stopped) {
                                break;
                            }
                            trackedRace.recordWind(delayWind(wind), windSourceAndTrack.getKey());
                        }
                    } finally {
                        windTrack.unlockAfterRead();
                    }
                }
            }.start();
        }
    }
    
    public void stop() {
        stopped = true;
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
        while (!isAdvanceInMillisSet()) {
            if (trackedRace.getStartOfRace() != null) {
                setAdvanceInMillis(System.currentTimeMillis() - trackedRace.getStartOfRace().asMillis());
            } else {
                try {
                    wait(2000); // wait for two seconds, then re-evaluate whether there is a start time
                } catch (InterruptedException e) {
                    logger.throwing(Simulator.class.getName(), "getAdvanceInMillis", e);
                    // ignore; try again
                }
            }
        }
        return advanceInMillis;
    }

    /**
     * Transforms <code>timed</code>'s time point according to this simulator's delay and waits roughly until this time
     * has passed. Then, returns the adjusted time point which therefore should roughly equal "now."
     */
    public TimePoint delay(TimePoint timePoint) {
        TimePoint transformedTimed = advance(timePoint);
        long waitTimeInMillis = getWaitTimeInMillisUntil(transformedTimed);
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

    private long getWaitTimeInMillisUntil(TimePoint transformedTimepoint) {
        long now = System.currentTimeMillis();
        long waitTimeInMillis = transformedTimepoint.asMillis() - now;
        return waitTimeInMillis;
    }

    /**
     * Like {@link #delay}, only that it doesn't wait until <code>timePoint</code> is reached in wall time.
     */
    public TimePoint advance(TimePoint timePoint) {
        return new MillisecondsTimePoint(timePoint.asMillis()+getAdvanceInMillis());
    }

    /**
     * If {@link #advanceInMillis} is already set to a non-negative value, it is left alone, and
     * {@link #delay(TimePoint)} is called. Otherwise, <code>time</code> is taken to be the original start time of the
     * race which is then used to compute {@link #advanceInMillis} such that
     * <code>time.asMillis() + advanceInMillis == System.currentTimeMillis()</code>.
     */
    public TimePoint advanceMarkPassingTimePoint(TimePoint time) {
        return advanceTimePointAndUseAsStartTimeIfNeeded(time);
    }

    /**
     * If {@link #advanceInMillis} is already set to a non-negative value, it is left alone, and
     * {@link #delay(TimePoint)} is called. Otherwise, the given start time <code>time</code> is used to compute
     * {@link #advanceInMillis} such that <code>time.asMillis() + advanceInMillis == System.currentTimeMillis()</code>.
     */
    public TimePoint advanceStartTime(TimePoint raceStartTime) {
        return advanceTimePointAndUseAsStartTimeIfNeeded(raceStartTime);
    }

    private TimePoint advanceTimePointAndUseAsStartTimeIfNeeded(TimePoint time){
        if (isAdvanceInMillisSet()) {
            return advance(time);
        } else {
            setAdvanceInMillis(System.currentTimeMillis() - time.asMillis());
            return advance(time);
        }
    }

    private boolean isAdvanceInMillisSet() {
        return advanceInMillis != -1;
    }

    /**
     * Delivers those mark passings for which we don't have to wait as a single list to the {@link #trackedRace} now.
     * The remaining mark passings are delayed as long as it takes for the first mark passing with a positive wait time
     * to match "now." The whole list is then re-submitted to this same method.
     */
    public void delayMarkPassings(final Competitor competitor, List<MarkPassing> markPassings) {
        List<MarkPassing> deliverTransformedNow = new ArrayList<MarkPassing>();
        final List<MarkPassing> deliverLater = new ArrayList<MarkPassing>();
        Iterator<MarkPassing> i = markPassings.iterator();
        if (i.hasNext()) {
            MarkPassing markPassing = i.next();
            deliverLater.add(markPassing);
            TimePoint transformedTimepoint = advanceMarkPassingTimePoint(markPassing.getTimePoint());
            while (getWaitTimeInMillisUntil(transformedTimepoint) <= 0 && markPassing != null) {
                MarkPassing transformedMarkPassing = new MarkPassingImpl(transformedTimepoint,
                        markPassing.getWaypoint(), markPassing.getCompetitor());
                deliverTransformedNow.add(transformedMarkPassing);
                if (i.hasNext()) {
                    markPassing = i.next();
                    transformedTimepoint = advanceMarkPassingTimePoint(markPassing.getTimePoint());
                    deliverLater.add(markPassing);
                } else {
                    markPassing = null;
                }
            }
            trackedRace.updateMarkPassings(competitor, deliverTransformedNow);
            if (markPassing != null) {
                // not consumed and delivered all mark passings now
                while (i.hasNext()) {
                    deliverLater.add(i.next());
                }
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            delayMarkPassings(competitor, deliverLater);
                        } catch (Exception e) {
                            logger.throwing(Simulator.class.getName(), "scheduleMarkPosition", e);
                        }
                    }
                }, transformedTimepoint.asDate());
            }
        } else {
            // deliver an empty list now
            trackedRace.updateMarkPassings(competitor, markPassings);
        }
        
    }

    public void scheduleMarkPosition(final Mark mark, GPSFixMoving markFix) {
        final TimePoint transformedTimepoint = advance(markFix.getTimePoint());
        final GPSFixMoving transformedMarkFix = new GPSFixMovingImpl(markFix.getPosition(), transformedTimepoint, markFix.getSpeed());
        long waitTime = getWaitTimeInMillisUntil(transformedMarkFix.getTimePoint());
        if (waitTime <= 0) {
            trackedRace.recordFix(mark, transformedMarkFix);
        } else {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        trackedRace.recordFix(mark, transformedMarkFix);
                    } catch (Exception e) {
                        logger.throwing(Simulator.class.getName(), "scheduleMarkPosition", e);
                    }
                }
            }, transformedTimepoint.asDate());
        }
    }
}
