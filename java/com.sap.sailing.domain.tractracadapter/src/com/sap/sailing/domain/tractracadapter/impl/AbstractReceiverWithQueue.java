package com.sap.sailing.domain.tractracadapter.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sse.common.UtilNew;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;

/**
 * Some event receiver that can be executed in a thread because it's a runnable, and
 * manages a queue of events received. The events are expected to be triplets.<p>
 * 
 * The receiver can be stopped in different ways. 
 * 
 * @author Axel Uhl (d043530)
 */
public abstract class AbstractReceiverWithQueue<A, B, C> implements Runnable, Receiver {
    private static Logger logger = Logger.getLogger(AbstractReceiverWithQueue.class.getName());
    
    private final LinkedBlockingQueue<UtilNew.Triple<A, B, C>> queue;
    private final DomainFactory domainFactory;
    private final com.tractrac.clientmodule.Event tractracEvent;
    private final DynamicTrackedRegatta trackedRegatta;
    private final Simulator simulator;
    private Thread thread;

    /**
     * used by {@link #stopAfterNotReceivingEventsForSomeTime(long)} and {@link #run()} to check if an event was received
     * during the timeout period.
     */
    private boolean receivedEventDuringTimeout;
    
    public AbstractReceiverWithQueue(DomainFactory domainFactory, Event tractracEvent,
            DynamicTrackedRegatta trackedRegatta, Simulator simulator) {
        super();
        this.tractracEvent = tractracEvent;
        this.trackedRegatta = trackedRegatta;
        this.domainFactory = domainFactory;
        this.simulator = simulator;
        this.queue = new LinkedBlockingQueue<UtilNew.Triple<A, B, C>>();
    }
    
    protected synchronized void setAndStartThread(Thread thread) {
        this.thread = thread;
        thread.start();
    }
    
    protected DomainFactory getDomainFactory() {
        return domainFactory;
    }
    
    protected com.tractrac.clientmodule.Event getTracTracEvent() {
        return tractracEvent;
    }
    
    protected DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }
    
    public void stopPreemptively() {
        // mark the end and hence terminate the thread by adding a null/null/null event to the queue
        queue.clear();
        stopAfterProcessingQueuedEvents();
    }
    
    @Override
    public void stopAfterProcessingQueuedEvents() {
        queue.add(new UtilNew.Triple<A, B, C>(null, null, null));
    }
    
    protected Simulator getSimulator() {
        return simulator;
    }
    
    @Override
    public void stopAfterNotReceivingEventsForSomeTime(final long timeoutInMilliseconds) {
        receivedEventDuringTimeout = false;
        TracTracRaceTrackerImpl.scheduler.schedule(new Runnable() {
            public void run() {
                if (!receivedEventDuringTimeout) {
                    logger.info("Stopping receiver "+AbstractReceiverWithQueue.this+
                                " of class "+AbstractReceiverWithQueue.this.getClass().getName()+
                                " after not having received an event during "+timeoutInMilliseconds+"ms");
                    stopAfterProcessingQueuedEvents();
                } else {
                    logger.info("Rescheduling the stopping of receiver "+AbstractReceiverWithQueue.this+
                            " for another "+timeoutInMilliseconds+"ms");
                    receivedEventDuringTimeout = false;
                    TracTracRaceTrackerImpl.scheduler.schedule(this, timeoutInMilliseconds, TimeUnit.MILLISECONDS);
                }
            }
        }, timeoutInMilliseconds, TimeUnit.MILLISECONDS);
    }

    protected void enqueue(UtilNew.Triple<A, B, C> event) {
        queue.add(event);
        receivedEventDuringTimeout = true;
    }
    
    private boolean isStopEvent(UtilNew.Triple<A, B, C> event) {
        return event.getA() == null && event.getB() == null && event.getC() == null;
    }

    @Override
    public void run() {
        UtilNew.Triple<A, B, C> event = null;
        while (event == null || !isStopEvent(event)) {
            try {
                event = queue.take();
                if (!isStopEvent(event)) {
                    handleEvent(event);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (simulator != null) {
            simulator.stop();
        }
    }

    @Override
    public synchronized void join() throws InterruptedException {
        if (thread != null) {
            thread.join();
        }
    }

    @Override
    public synchronized void join(long timeoutInMilliseconds) throws InterruptedException {
        if (thread != null) {
            thread.join(timeoutInMilliseconds);
        }
    }

    protected abstract void handleEvent(UtilNew.Triple<A, B, C> event);

    /**
     * Tries to find a {@link TrackedRace} for <code>race</code> in the {@link com.sap.sailing.domain.base.Regatta}
     * corresponding to {@link #tractracEvent}, as keyed by the {@link #domainFactory}. Waits for
     * {@link RaceTracker#TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS} milliseconds for the
     * {@link RaceDefinition} to show up. If it doesn't, <code>null</code> is returned. If the {@link RaceDefinition}
     * for <code>race</code> is not found in the {@link com.sap.sailing.domain.base.Regatta}, <code>null</code> is
     * returned. If the {@link TrackedRace} for <code>race</code> isn't found in the {@link TrackedRegatta},
     * <code>null</code> is returned, too.
     */
    protected DynamicTrackedRace getTrackedRace(Race race) {
        DynamicTrackedRace result = null;
        RaceDefinition raceDefinition = getDomainFactory().getAndWaitForRaceDefinition(race.getId(),
                RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
        if (raceDefinition != null) {
            com.sap.sailing.domain.base.Regatta domainRegatta = trackedRegatta.getRegatta();
            if (domainRegatta.getRaceByName(raceDefinition.getName()) != null) {
                result = trackedRegatta.getTrackedRace(raceDefinition);
            }
        }
        return result;
    }
}
