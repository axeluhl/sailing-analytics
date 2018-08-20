package com.sap.sailing.domain.tracking.impl;

import java.util.Map;

import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;

public abstract class AbstractRaceTrackingConnectivityParametersHandler
        implements RaceTrackingConnectivityParametersHandler {
    private static final String CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION = "correctWindDirectionByMagneticDeclination";
    private static final String TRACK_WIND = "trackWind";
    
    protected void addWindTrackingParameters(RaceTrackingConnectivityParameters params, Map<String, Object> map) {
        map.put(TRACK_WIND, params.isTrackWind());
        map.put(CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION, params.isCorrectWindDirectionByMagneticDeclination());
    }
    
    protected boolean isTrackWind(Map<String, Object> from) {
        return from.containsKey(TRACK_WIND) ? (Boolean) from.get(TRACK_WIND) : false;
    }
    
    protected boolean isCorrectWindDirectionByMagneticDeclination(Map<String, Object> from) {
        return from.containsKey(CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION) ? (Boolean) from.get(CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION) : true;
    }
}
