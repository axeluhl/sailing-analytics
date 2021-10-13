package com.sap.sailing.domain.yellowbrickadapter.impl;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Duration;

public class YellowBrickTrackingAdapterImpl implements YellowBrickTrackingAdapter {
    private final static Duration TIMEOUT_FOR_RACE_LOADING = Duration.ONE_MINUTE;
    private final DomainFactory baseDomainFactory;
    
    public YellowBrickTrackingAdapterImpl(DomainFactory baseDomainFactory) {
        this.baseDomainFactory = baseDomainFactory;
    }

    @Override
    public void addYellowBrickRace(RacingEventService service, RegattaIdentifier regattaToAddTo,
            String yellowBrickRaceUrl, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            String yellowBrickUsername, String yellowBrickPassword, boolean trackWind, boolean correctWindByDeclination)
            throws Exception {
        service.addRace(regattaToAddTo,
                new YellowBrickRaceTrackingConnectivityParams(yellowBrickRaceUrl, yellowBrickUsername,
                        yellowBrickPassword, trackWind, correctWindByDeclination, raceLogStore, regattaLogStore,
                        baseDomainFactory),
                /* timeout */ TIMEOUT_FOR_RACE_LOADING.asMillis());
    }
}
