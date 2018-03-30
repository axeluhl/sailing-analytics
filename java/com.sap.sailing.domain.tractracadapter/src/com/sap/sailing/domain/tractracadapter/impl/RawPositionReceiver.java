package com.sap.sailing.domain.tractracadapter.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Util.Triple;
import com.tractrac.model.lib.api.data.IPosition;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.competitor.IPositionListener;

public class RawPositionReceiver extends AbstractReceiverWithQueue<IRaceCompetitor, IPosition, Void> {
    private static final Logger logger = Logger.getLogger(RawPositionReceiver.class.getName());

    private int received;

    private final IPositionListener listener;

    public RawPositionReceiver(DynamicTrackedRegatta trackedRegatta, IEvent tractracEvent,
            DomainFactory domainFactory, Simulator simulator, IEventSubscriber eventSubscriber,
            IRaceSubscriber raceSubscriber, long timeoutInMilliseconds) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator, eventSubscriber, raceSubscriber, timeoutInMilliseconds);
        listener = new IPositionListener() {
            @Override
            public void gotPosition(IRaceCompetitor controlPoint, IPosition position) {
                enqueue(new Triple<IRaceCompetitor, IPosition, Void>(controlPoint, position, null));
            }
        };
    }
    
    @Override
    public void subscribe() {
        getRaceSubscriber().subscribePositions(listener);
        startThread();
    }
    
    @Override
    protected void unsubscribe() {
        getRaceSubscriber().unsubscribePositions(listener);
    }

    @Override
    protected void handleEvent(Triple<IRaceCompetitor, IPosition, Void> event) {
        if (!event.getA().getCompetitor().isNonCompeting()) {
            if (received++ % 1000 == 0) {
                System.out.print("P");
                if ((received / 1000 + 1) % 80 == 0) {
                    System.out.println();
                }
            }
            IRace race = event.getA().getRace();
            DynamicTrackedRace trackedRace = getTrackedRace(race);
            if (trackedRace != null) {
                final GPSFixMoving fix = getDomainFactory().createGPSFixMoving(event.getB());
                Competitor competitor = getDomainFactory().resolveCompetitor(event.getA().getCompetitor());
                if (getSimulator() != null) {
                    getSimulator().scheduleCompetitorPosition(competitor, fix);
                } else {
                    trackedRace.recordFix(competitor, fix);
                }
            } else {
                logger.warning("Couldn't find tracked race for race " + race.getName()
                        + ". Dropping raw position event " + event);
            }
        }
    }
}
