package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParametersHandler;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;

/**
 * Handles mapping TracTrac connectivity parameters from and to a map with {@link String} keys. The
 * "param URL" is considered the {@link #getKey(RaceTrackingConnectivityParameters) key} for these objects.<p>
 * 
 * Lives in the same package as {@link RaceTrackingConnectivityParameters} for package-private access to
 * its members.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SwissTimingReplayConnectivityParamsHandler extends AbstractRaceTrackingConnectivityParametersHandler {
    private static final String BOAT_CLASS_NAME = "boatClassName";
    private static final String RACE_ID = "raceId";
    private static final String RACE_NAME = "raceName";
    private static final String LINK = "link";
    private static final String SWISS_TIMING_URL = "swissTimingUrl";
    private static final String USE_INTERNAL_MARK_PASSING_ALGORITHM = "useInternalMarkPassingAlgorithm";
    private static final String DELAY_TO_LIVE_IN_MILLIS = "delayToLiveInMillis";
    private final RaceLogStore raceLogStore;
    private final RegattaLogStore regattaLogStore;
    private final DomainFactory domainFactory;
    private final SwissTimingReplayService replayService;
    private final SecurityService securityService;

    public SwissTimingReplayConnectivityParamsHandler(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            DomainFactory domainFactory, SwissTimingReplayService replayService, SecurityService securityService) {
        super();
        this.raceLogStore = raceLogStore;
        this.regattaLogStore = regattaLogStore;
        this.domainFactory = domainFactory;
        this.replayService = replayService;
        this.securityService = securityService;
    }

    @Override
    public Map<String, Object> mapFrom(RaceTrackingConnectivityParameters params) {
        assert params instanceof SwissTimingReplayConnectivityParameters;
        final SwissTimingReplayConnectivityParameters stParams = (SwissTimingReplayConnectivityParameters) params;
        final Map<String, Object> result = getKey(params);
        result.put(SWISS_TIMING_URL, stParams.getSwissTimingUrl());
        result.put(DELAY_TO_LIVE_IN_MILLIS, stParams.getDelayToLiveInMillis());
        result.put(USE_INTERNAL_MARK_PASSING_ALGORITHM, stParams.isUseInternalMarkPassingAlgorithm());
        result.put(RACE_NAME, stParams.getRaceName());
        result.put(RACE_ID, stParams.getRaceID());
        result.put(BOAT_CLASS_NAME, stParams.getBoatClassName());
        addWindTrackingParameters(stParams, result);
        return result;
    }

    @Override
    public RaceTrackingConnectivityParameters mapTo(Map<String, Object> map) throws MalformedURLException, URISyntaxException {
        return new SwissTimingReplayConnectivityParameters((String) map.get(LINK), (String) map.get(SWISS_TIMING_URL),
                (String) map.get(RACE_NAME), (String) map.get(RACE_ID),
                (String) map.get(BOAT_CLASS_NAME), (boolean) map.get(USE_INTERNAL_MARK_PASSING_ALGORITHM), domainFactory, replayService,
                raceLogStore, regattaLogStore);
    }

    @Override
    public Map<String, Object> getKey(RaceTrackingConnectivityParameters params) {
        assert params instanceof SwissTimingReplayConnectivityParameters;
        final SwissTimingReplayConnectivityParameters stParams = (SwissTimingReplayConnectivityParameters) params;
        final Map<String, Object> result = new HashMap<>();
        result.put(TypeBasedServiceFinder.TYPE, params.getTypeIdentifier());
        result.put(LINK, stParams.getLink());
        return result;
    }

    @Override
    public RaceTrackingConnectivityParameters resolve(RaceTrackingConnectivityParameters params) throws Exception {
        assert params instanceof SwissTimingReplayConnectivityParameters;
        final SwissTimingReplayConnectivityParameters stParams = (SwissTimingReplayConnectivityParameters) params;
        SwissTimingReplayConnectivityParameters result = new SwissTimingReplayConnectivityParameters(stParams.getLink(),
                stParams.getSwissTimingUrl(), stParams.getRaceName(), stParams.getRaceID(),
                stParams.getBoatClassName(), stParams.isUseInternalMarkPassingAlgorithm(), domainFactory, replayService,
                raceLogStore, regattaLogStore);
        final String creatorName = SessionUtils.getPrincipal().toString();
        if (result.getSwissTimingUrl() != null) { // legacy records won't have this URL stored in their connectivity params
            final SwissTimingArchiveConfiguration swissTimingArchiveConfiguration = SwissTimingFactory.INSTANCE
                    .createSwissTimingArchiveConfiguration(result.getSwissTimingUrl(), creatorName);
            SwissTimingAdapterPersistence.INSTANCE.updateSwissTimingArchiveConfiguration(swissTimingArchiveConfiguration);
            securityService.setDefaultOwnershipIfNotSet(swissTimingArchiveConfiguration.getIdentifier());
        }
        return result;
    }
}
