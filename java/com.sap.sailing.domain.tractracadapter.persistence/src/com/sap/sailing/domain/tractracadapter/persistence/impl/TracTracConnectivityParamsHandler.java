package com.sap.sailing.domain.tractracadapter.persistence.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.impl.RaceTrackingConnectivityParametersImpl;
import com.sap.sailing.domain.tractracadapter.impl.TracTracRaceTrackerImpl;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
public class TracTracConnectivityParamsHandler extends AbstractRaceTrackingConnectivityParametersHandler {
    private static final String USE_INTERNAL_MARK_PASSING_ALGORITHM = "useInternalMarkPassingAlgorithm";
    private static final String TRAC_TRAC_USERNAME = "tracTracUsername";
    private static final String TRAC_TRAC_PASSWORD = "tracTracPassword";
    private static final String STORED_URI = "storedURI";
    private static final String START_OF_TRACKING_MILLIS = "startOfTrackingMillis";
    private static final String RACE_VISIBILITY = "raceVisibility";
    private static final String RACE_STATUS = "raceStatus";
    private static final String PARAM_URL = "paramURL";
    private static final String OFFSET_TO_START_TIME_OF_SIMULATED_RACE_MILLIS = "offsetToStartTimeOfSimulatedRaceMillis";
    private static final String LIVE_URI = "liveURI";
    private static final String END_OF_TRACKING_MILLIS = "endOfTrackingMillis";
    private static final String DELAY_TO_LIVE_IN_MILLIS = "delayToLiveInMillis";
    private static final String COURSE_DESIGN_UPDATE_URI = "courseDesignUpdateURI";
    private final RaceLogStore raceLogStore;
    private final RegattaLogStore regattaLogStore;
    private final DomainFactory domainFactory;

    public TracTracConnectivityParamsHandler(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, DomainFactory domainFactory) {
        super();
        this.raceLogStore = raceLogStore;
        this.regattaLogStore = regattaLogStore;
        this.domainFactory = domainFactory;
    }

    @Override
    public Map<String, Object> mapFrom(RaceTrackingConnectivityParameters params) throws MalformedURLException {
        assert params instanceof RaceTrackingConnectivityParametersImpl;
        final RaceTrackingConnectivityParametersImpl ttParams = (RaceTrackingConnectivityParametersImpl) params;
        final Map<String, Object> result = getKey(params);
        result.put(COURSE_DESIGN_UPDATE_URI, ttParams.getCourseDesignUpdateURI()==null?null:ttParams.getCourseDesignUpdateURI().toString());
        result.put(DELAY_TO_LIVE_IN_MILLIS, ttParams.getDelayToLiveInMillis());
        result.put(END_OF_TRACKING_MILLIS, ttParams.getEndOfTracking()==null?null:ttParams.getEndOfTracking().asMillis());
        result.put(LIVE_URI, ttParams.getLiveURI()==null?null:ttParams.getLiveURI().toString());
        result.put(OFFSET_TO_START_TIME_OF_SIMULATED_RACE_MILLIS, ttParams.getOffsetToStartTimeOfSimulatedRace()==null?null:ttParams.getOffsetToStartTimeOfSimulatedRace().asMillis());
        result.put(RACE_STATUS, ttParams.getRaceStatus());
        result.put(RACE_VISIBILITY, ttParams.getRaceVisibility());
        result.put(START_OF_TRACKING_MILLIS, ttParams.getStartOfTracking()==null?null:ttParams.getStartOfTracking().asMillis());
        result.put(STORED_URI, ttParams.getStoredURI()==null?null:ttParams.getStoredURI().toString());
        result.put(TRAC_TRAC_PASSWORD, ttParams.getTracTracPassword());
        result.put(TRAC_TRAC_USERNAME, ttParams.getTracTracUsername().toString());
        result.put(USE_INTERNAL_MARK_PASSING_ALGORITHM, ttParams.isUseInternalMarkPassingAlgorithm());
        addWindTrackingParameters(ttParams, result);
        return result;
    }

    @Override
    public RaceTrackingConnectivityParameters mapTo(Map<String, Object> map) throws Exception {
        return new RaceTrackingConnectivityParametersImpl(
                new URL(map.get(PARAM_URL).toString()),
                map.get(LIVE_URI) == null ? null : new URI(map.get(LIVE_URI).toString()),
                map.get(STORED_URI) == null ? null : new URI(map.get(STORED_URI).toString()),
                map.get(COURSE_DESIGN_UPDATE_URI) == null ? null : new URI(map.get(COURSE_DESIGN_UPDATE_URI).toString()),
                map.get(START_OF_TRACKING_MILLIS) == null ? null : new MillisecondsTimePoint(((Number) map.get(START_OF_TRACKING_MILLIS)).longValue()),
                map.get(END_OF_TRACKING_MILLIS) == null ? null : new MillisecondsTimePoint(((Number) map.get(END_OF_TRACKING_MILLIS)).longValue()),
                ((Number) map.get(DELAY_TO_LIVE_IN_MILLIS)).longValue(),
                map.get(OFFSET_TO_START_TIME_OF_SIMULATED_RACE_MILLIS) == null ? null : new MillisecondsDurationImpl(((Number) map.get(OFFSET_TO_START_TIME_OF_SIMULATED_RACE_MILLIS)).longValue()),
                (Boolean) map.get(USE_INTERNAL_MARK_PASSING_ALGORITHM),
                raceLogStore, regattaLogStore, domainFactory,
                map.get(TRAC_TRAC_USERNAME)==null?null:map.get(TRAC_TRAC_USERNAME).toString(),
                map.get(TRAC_TRAC_PASSWORD)==null?null:map.get(TRAC_TRAC_PASSWORD).toString(),
                map.get(RACE_STATUS)==null?null:map.get(RACE_STATUS).toString(),
                map.get(RACE_VISIBILITY)==null?null:map.get(RACE_VISIBILITY).toString(), isTrackWind(map),
                isCorrectWindDirectionByMagneticDeclination(map), /* preferReplayIfAvailable */ true,
                /* default timeout for obtaining IRace object from params URL */ (int) RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
    }

    @Override
    public Map<String, Object> getKey(RaceTrackingConnectivityParameters params) throws MalformedURLException {
        assert params instanceof RaceTrackingConnectivityParametersImpl;
        final RaceTrackingConnectivityParametersImpl ttParams = (RaceTrackingConnectivityParametersImpl) params;
        final Map<String, Object> result = new HashMap<>();
        result.put(TypeBasedServiceFinder.TYPE, params.getTypeIdentifier());
        result.put(PARAM_URL, TracTracRaceTrackerImpl.getParamURLStrippedOfRandomParam(new URL(ttParams.getParamURL().toString())).toString());
        return result;
    }
}
