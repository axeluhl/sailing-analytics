package com.sap.sailing.domain.yellowbrickadapter;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.server.interfaces.RacingEventService;

public interface YellowBrickTrackingAdapter {
    void addYellowBrickRace(RacingEventService service, RegattaIdentifier regattaToAddTo, String yellowBrickRaceUrl,
            RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, String yellowBrickUsername,
            String yellowBrickPassword, boolean trackWind, boolean correctWindByDeclination) throws Exception;
}
