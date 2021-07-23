package com.sap.sailing.domain.tractracadapter.impl;

import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Util.Triple;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.sensor.ISensorData;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.race.IRaceCompetitorListener;

/**
 * Subscribes for changes in an {@link IRaceCompetitor} object, such as the {@link IRaceCompetitor#getStatus() status}
 * or {@link IRaceCompetitor#getOfficialFinishTime() the official finishing time} and compares that with what we have in
 * the {@link RaceLog} {@link TrackedRace#getAttachedRaceLogs() attached} to the {@link TrackedRace} to which this
 * receiver belongs. If a valid {@link CompetitorResult} is found for the respective competitor already in the race log
 * that matches the new state of the {@link IRaceCompetitor}, a time stamp comparison is performed. The second component
 * of the triple enqueued by this receiver represents the time stamp in milliseconds since the epoch (Unix time) telling
 * when the event was originally created. If older than what we have in the race log already, we'll ignore the event.
 * Otherwise, a {@link CompetitorResult} object will be constructed and added in a
 * {@link RaceLogFinishPositioningConfirmedEvent} to the race log from where it will go into the leaderboard.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorChangeReceiver extends AbstractReceiverWithQueue<IRaceCompetitor, Void, Void>
implements OfficialCompetitorUpdateProvider {
    private static final Logger logger = Logger.getLogger(CompetitorChangeReceiver.class.getName());
    private final IRaceCompetitorListener listener;
    private final RaceAndCompetitorStatusWithRaceLogReconciler reconciler;

    public CompetitorChangeReceiver(DynamicTrackedRegatta trackedRegatta, IEvent tractracEvent,
            IRace tractracRace, Simulator simulator, DomainFactory domainFactory,
            IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber, long timeoutInMilliseconds,
            RaceAndCompetitorStatusWithRaceLogReconciler raceAndCompetitorStatusWithRaceLogReconciler) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator, eventSubscriber, raceSubscriber, timeoutInMilliseconds);
        reconciler = raceAndCompetitorStatusWithRaceLogReconciler;
        reconciler.setOfficialCompetitorUpdateProvider(this);
        listener = new IRaceCompetitorListener() {
            @Override
            public void addRaceCompetitor(long timestamp, IRaceCompetitor raceCompetitor) {
                // can't handle competitor list changes once the RaceDefinition exists
                logger.warning("The competitor "+raceCompetitor+" was added to the race "+getTrackedRace(raceCompetitor.getRace())+
                        " but we don't know how to handle this. Ignoring.");
            }

            @Override
            public void updateRaceCompetitor(long timestamp, IRaceCompetitor raceCompetitor) {
                enqueue(new Triple<>(raceCompetitor, null, null));
            }

            @Override
            public void deleteRaceCompetitor(long timestamp, UUID competitorId) {
                // can't handle competitor list changes once the RaceDefinition exists
                logger.warning("The competitor with ID "+competitorId+" was removed from a race "+
                        "but we don't know how to handle this. Ignoring.");
            }

            @Override
            public void removeOffsetPositions(long timestamp, UUID competitorId, int offset) {
                // not sure what this means
            }
        };
    }

    @Override
    public void subscribe() {
        getRaceSubscriber().subscribeRaceCompetitor(listener);
        startThread();
    }
    
    @Override
    protected void unsubscribe() {
        getRaceSubscriber().unsubscribeRaceCompetitor(listener);
    }
    
    /**
     * Looks for valid, non-{@code null} components for ride height port/starboard, heel and trim
     * in the {@link ISensorData} object. All those values are copied into a {@link BravoFix} and
     * inserted into the competitor's bravo fix track.
     */
    @Override
    protected void handleEvent(Triple<IRaceCompetitor, Void, Void> event) {
        final IRace race = event.getA().getRace();
        final DynamicTrackedRace trackedRace = getTrackedRace(race);
        if (trackedRace != null) {
            final IRaceCompetitor raceCompetitor = event.getA();
            if (reconciler != null && raceCompetitor != null) {
                reconciler.reconcileCompetitorStatus(raceCompetitor, trackedRace);
            }
        } else {
            logger.warning("Couldn't find tracked race for race " + race.getName()
                    + ". Dropping competitor change event " + event);
        }
    }

    @Override
    public void runWhenNoMoreOfficialCompetitorUpdatesPending(Runnable runnable) {
        callBackWhenLoadingQueueIsDone(receiver->runnable.run());
    }
}
