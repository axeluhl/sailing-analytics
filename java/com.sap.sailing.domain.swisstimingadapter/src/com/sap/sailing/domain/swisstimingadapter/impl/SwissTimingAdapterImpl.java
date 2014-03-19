package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackerManager;

public class SwissTimingAdapterImpl implements SwissTimingAdapter {
    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.swisstimingadapter.DomainFactory swissTimingDomainFactory;

    public SwissTimingAdapterImpl(DomainFactory baseDomainFactory) {
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        swissTimingDomainFactory = new DomainFactoryImpl(baseDomainFactory);
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
            int port) throws InterruptedException, UnknownHostException, IOException,
            ParseException {
        List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> result = new ArrayList<com.sap.sailing.domain.swisstimingadapter.RaceRecord>();
        // TODO --> Frank: this needs to come from Manage2Sail and its JSON document somehow...
        return result;
    }

    @Override
    public RacesHandle addSwissTimingRace(TrackerManager trackerManager, RegattaIdentifier regattaToAddTo, String raceID, String raceDescription,
            String hostname, int port, RaceLogStore logStore, long timeoutInMilliseconds)
            throws Exception {
        return trackerManager.addRace(regattaToAddTo, swissTimingDomainFactory.createTrackingConnectivityParameters(hostname, port,
                raceID, raceDescription, TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS, swissTimingFactory,
                swissTimingDomainFactory, logStore),
                timeoutInMilliseconds);
    }

}
