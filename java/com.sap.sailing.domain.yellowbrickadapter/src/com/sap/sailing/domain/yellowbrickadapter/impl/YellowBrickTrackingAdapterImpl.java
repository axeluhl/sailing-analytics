package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRace;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.util.HttpUrlConnectionHelper;

public class YellowBrickTrackingAdapterImpl implements YellowBrickTrackingAdapter {
    private final static Duration TIMEOUT_FOR_RACE_LOADING = Duration.ONE_MINUTE;
    
    /**
     * The base URL template with a string parameter for the race URL
     */
    private final static String BASE_URL_TEMPLATE = "https://yb.tl/API3/Race/%s/GetPositions";
    
    /**
     * Template to construct the URL from the race URL parameter and the number of latest fixes per team to obtain
     */
    private final static String NUMBER_OF_POSITIONS_URL_TEMPLATE = BASE_URL_TEMPLATE + "?n=%d";
    
    /**
     * Template to construct the URL from the race URL parameter and the date (as Unix time stamp, milliseconds
     * since the epoch, 1970-01-01T00:00:00Z) after which to fetch positions.
     */
    private final static String POSITIONS_SINCE_DATE_URL_TEMPLATE = BASE_URL_TEMPLATE + "?t=%d";
    
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
    
    String getUrlForLatestFix(String raceUrl) {
        return String.format(NUMBER_OF_POSITIONS_URL_TEMPLATE, raceUrl, 1);
    }

    @Override
    public YellowBrickRace getYellowBrickRace(String raceUrl) throws IOException, ParseException {
        final String url = getUrlForLatestFix(raceUrl);
        final URLConnection result = HttpUrlConnectionHelper.redirectConnectionWithBearerToken(new URL(url), TIMEOUT_FOR_RACE_LOADING, /* bearer token */ null);
        final InputStream inputStream = (InputStream) result.getContent();
        final PositionsDocument doc = new GetPositionsParser().parse(new InputStreamReader(inputStream));
        return new YellowBrickRaceImpl(raceUrl, doc.getTimePointOfLastFix(), Util.size(doc.getTeams()));
    }
}
