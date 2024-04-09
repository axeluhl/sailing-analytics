package com.sap.sailing.domain.tractracadapter.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.competitor.ICompetitorsListener;
import com.tractrac.subscription.lib.api.control.IControlsListener;
import com.tractrac.subscription.lib.api.event.IConnectionStatusListener;
import com.tractrac.subscription.lib.api.event.IEventMessageListener;
import com.tractrac.subscription.lib.api.event.IServerTimeListener;
import com.tractrac.subscription.lib.api.race.IRacesListener;
import com.tractrac.subscription.lib.api.race.IStartStopTimesChangeListener;

/**
 * A wrapper around a {@link IEventSubscriber} that can be shared across many {@link TracTracRaceTrackerImpl} instances
 * that each invoke {@link #start} and {@link #stop()} symmetrically. This wrapper manages a counter (as an
 * {@link AtomicInteger}) such that {@link #start} will only delegate to the instance wrapped if the counter is 0;
 * likewise, {@link #stop} will delegate only if the counter goes to 0.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class EventSubscriberWrapper implements IEventSubscriber {
    private final IEventSubscriber delegate;
    private final AtomicInteger startCounter;
    
    public EventSubscriberWrapper(IEventSubscriber delegate) {
        this.delegate = delegate;
        startCounter = new AtomicInteger(0);
    }

    @Override
    public void subscribeConnectionStatus(IConnectionStatusListener listener) {
        delegate.subscribeConnectionStatus(listener);
    }

    @Override
    public void unsubscribeConnectionStatus(IConnectionStatusListener listener) {
        delegate.unsubscribeConnectionStatus(listener);
    }

    @Override
    public void start() {
        if (startCounter.getAndIncrement() == 0) {
            delegate.start();
        }
    }

    @Override
    public void stop() {
        if (startCounter.decrementAndGet() == 0) {
            delegate.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public void subscribeControls(IControlsListener listener) {
        delegate.subscribeControls(listener);
    }

    @Override
    public void unsubscribeControls(IControlsListener listener) {
        delegate.unsubscribeControls(listener);
    }

    @Override
    public void subscribeEventTimesChanges(IStartStopTimesChangeListener listener) {
        delegate.subscribeEventTimesChanges(listener);
    }

    @Override
    public void unsubscribeEventTimesChanges(IStartStopTimesChangeListener listener) {
        delegate.unsubscribeEventTimesChanges(listener);
    }

    @Override
    public void subscribeEventMessages(IEventMessageListener listener) {
        delegate.subscribeEventMessages(listener);
    }

    @Override
    public void unsubscribeEventMessages(IEventMessageListener listener) {
        delegate.unsubscribeEventMessages(listener);
    }

    @Override
    public void subscribeServerTime(IServerTimeListener serverTimeListener) {
        delegate.subscribeServerTime(serverTimeListener);
    }

    @Override
    public void unsubscribeServerTime(IServerTimeListener serverTimeListener) {
        delegate.unsubscribeServerTime(serverTimeListener);
    }

    @Override
    public void subscribeRaces(IRacesListener listener) {
        delegate.subscribeRaces(listener);
    }

    @Override
    public void unsubscribeRaces(IRacesListener listener) {
        delegate.unsubscribeRaces(listener);
    }

    @Override
    public void subscribeCompetitors(ICompetitorsListener listener) {
        delegate.subscribeCompetitors(listener);
    }

    @Override
    public void unsubscribeCompetitors(ICompetitorsListener listener) {
        delegate.unsubscribeCompetitors(listener);
    }
}
