package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackerManager;

public class SwissTimingAdapterImpl implements SwissTimingAdapter {
    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.swisstimingadapter.DomainFactory swissTimingDomainFactory;

    private final RaceSpecificMessageLoader raceSpecificMessageLoader;

    public SwissTimingAdapterImpl(DomainFactory baseDomainFactory, RaceSpecificMessageLoader raceSpecificMessageLoader) {
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        swissTimingDomainFactory = new DomainFactoryImpl(baseDomainFactory);
        this.raceSpecificMessageLoader = raceSpecificMessageLoader;
    }

    @Override
    public com.sap.sailing.domain.swisstimingadapter.DomainFactory getSwissTimingDomainFactory() {
        return swissTimingDomainFactory;
    }

    @Override
    public SwissTimingFactory getSwissTimingFactory() {
        return swissTimingFactory;
    }

    @Override
    public List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(String hostname,
            int port, boolean canSendRequests) throws InterruptedException, UnknownHostException, IOException,
            ParseException {
        List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> result = new ArrayList<com.sap.sailing.domain.swisstimingadapter.RaceRecord>();
        SailMasterConnector swissTimingConnector = swissTimingFactory.getOrCreateSailMasterConnector(hostname, port,
                raceSpecificMessageLoader, canSendRequests);
        for (Race race : swissTimingConnector.getRaces()) {
            String raceID = race.getRaceID();
            TimePoint startTime = swissTimingConnector.getStartTime(raceID);
            boolean hasCourse = swissTimingConnector.hasCourse(raceID);
            boolean hasStartlist = swissTimingConnector.hasStartlist(raceID);
            result.add(new com.sap.sailing.domain.swisstimingadapter.RaceRecord(raceID,
                    race.getDescription(), startTime == null ? null : startTime.asDate(),
                            hasCourse, hasStartlist));
        }
        return result;
    }

    @Override
    public RacesHandle addSwissTimingRace(TrackerManager trackerManager, RegattaIdentifier regattaToAddTo, String raceID, String hostname,
            int port, boolean canSendRequests, RaceLogStore logStore, long timeoutInMilliseconds)
            throws Exception {
        return trackerManager.addRace(regattaToAddTo, swissTimingDomainFactory.createTrackingConnectivityParameters(hostname, port,
                raceID, canSendRequests, TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS, swissTimingFactory,
                swissTimingDomainFactory, logStore, raceSpecificMessageLoader),
                timeoutInMilliseconds);
    }

    @Override
    public void storeSwissTimingDummyRace(String racMessage, String stlMessage, String ccgMessage) {
        SailMasterMessage racSMMessage = swissTimingFactory.createMessage(racMessage, null);
        SailMasterMessage stlSMMessage = swissTimingFactory.createMessage(stlMessage, null);
        SailMasterMessage ccgSMMessage = swissTimingFactory.createMessage(ccgMessage, null);
        if (raceSpecificMessageLoader.getRace(stlSMMessage.getRaceID()) != null) {
            throw new IllegalArgumentException("Race with raceID \"" + stlSMMessage.getRaceID() + "\" already exists.");
        } else {
            raceSpecificMessageLoader.storeSailMasterMessage(racSMMessage);
            raceSpecificMessageLoader.storeSailMasterMessage(stlSMMessage);
            raceSpecificMessageLoader.storeSailMasterMessage(ccgSMMessage);
        }
    }

}
