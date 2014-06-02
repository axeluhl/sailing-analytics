package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.UtilNew;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.CompetitorPositionRawData;
import com.tractrac.clientmodule.data.ICallbackData;

public class RawPositionReceiver extends AbstractReceiverWithQueue<RaceCompetitor, CompetitorPositionRawData, Boolean> {
    private static final Logger logger = Logger.getLogger(RawPositionReceiver.class.getName());

    private int received;

    public RawPositionReceiver(DynamicTrackedRegatta trackedRegatta, com.tractrac.clientmodule.Event tractracEvent,
            DomainFactory domainFactory, Simulator simulator) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator);
    }
    
    /**
     * Obtains the listener and starts a thread of this object which will block for events received.
     */
    @Override
    public Iterable<TypeController> getTypeControllersAndStart() {
        ICallbackData<RaceCompetitor, CompetitorPositionRawData> positionListener = new ICallbackData<RaceCompetitor, CompetitorPositionRawData>() {
            public void gotData(RaceCompetitor tracked,
                    CompetitorPositionRawData record, boolean isLiveData) {
                enqueue(new UtilNew.Triple<RaceCompetitor, CompetitorPositionRawData, Boolean>(tracked, record, isLiveData));
            }
        };
        List<TypeController> listeners = new ArrayList<TypeController>();
        for (Race race : getTracTracEvent().getRaceList()) {
            TypeController listener = CompetitorPositionRawData.subscribe(race,
                positionListener, /* fromTime */0 /* means ALL */, /* toTime */Long.MAX_VALUE);
            listeners.add(listener);
        }
        setAndStartThread(new Thread(this, getClass().getName()));
        return listeners;
    }

    @Override
    protected void handleEvent(UtilNew.Triple<RaceCompetitor, CompetitorPositionRawData, Boolean> event) {
        if (received++ % 1000 == 0) {
            System.out.print("P");
            if ((received / 1000 + 1) % 80 == 0) {
                System.out.println();
            }
        }
        Race race = event.getA().getRace();
        DynamicTrackedRace trackedRace = getTrackedRace(race);
        if (trackedRace != null) {
            GPSFixMoving fix = getDomainFactory().createGPSFixMoving(event.getB());
            if (getSimulator() != null) {
                fix = new GPSFixMovingImpl(fix.getPosition(), getSimulator().delay(
                        fix.getTimePoint()), fix.getSpeed());
            }
            Competitor competitor = getDomainFactory().getOrCreateCompetitor(event.getA().getCompetitor());
            trackedRace.recordFix(competitor, fix);
        } else {
            logger.warning("Couldn't find tracked race for race " + race.getName()
                    + ". Dropping raw position event " + event);
        }
    }
}
