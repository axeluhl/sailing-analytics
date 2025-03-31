package com.sap.sailing.domain.yellowbrickadapter.persistence.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sailing.domain.yellowbrickadapter.impl.YellowBrickConfigurationImpl;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;

/**
 * Handles mapping YellowBrick connectivity parameters from and to a map with {@link String} keys. The "race URL" is
 * considered the {@link #getKey(RaceTrackingConnectivityParameters) key} for these objects.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class YellowBrickConnectivityParamsHandler extends AbstractRaceTrackingConnectivityParametersHandler {
    private static final String YELLOW_BRICK_USERNAME = "yellowBrickUsername";
    private static final String YELLOW_BRICK_PASSWORD = "yellowBrickPassword";
    private static final String RACE_URL = "raceURL";
    private final RaceLogStore raceLogStore;
    private final RegattaLogStore regattaLogStore;
    private final DomainFactory baseDomainFactory;
    private final SecurityService securityService;
    private final YellowBrickTrackingAdapter yellowBrickTrackingAdapter;

    public YellowBrickConnectivityParamsHandler(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            DomainFactory baseDomainFactory, SecurityService securityService,
            YellowBrickTrackingAdapter yellowBrickTrackingAdapter) {
        super();
        this.raceLogStore = raceLogStore;
        this.regattaLogStore = regattaLogStore;
        this.baseDomainFactory = baseDomainFactory;
        this.securityService = securityService;
        this.yellowBrickTrackingAdapter = yellowBrickTrackingAdapter;
    }

    @Override
    public Map<String, Object> mapFrom(RaceTrackingConnectivityParameters params) throws MalformedURLException {
        assert params instanceof YellowBrickRaceTrackingConnectivityParams;
        final YellowBrickRaceTrackingConnectivityParams ybParams = (YellowBrickRaceTrackingConnectivityParams) params;
        final Map<String, Object> result = getKey(params);
        result.put(RACE_URL, ybParams.getRaceUrl()==null?null:ybParams.getRaceUrl().toString());
        result.put(YELLOW_BRICK_USERNAME, ybParams.getUsername());
        result.put(YELLOW_BRICK_PASSWORD, ybParams.getPassword());
        addWindTrackingParameters(ybParams, result);
        return result;
    }

    @Override
    public RaceTrackingConnectivityParameters mapTo(Map<String, Object> map) throws Exception {
        return new YellowBrickRaceTrackingConnectivityParams(map.get(RACE_URL).toString(),
                map.get(YELLOW_BRICK_USERNAME) == null ? null : map.get(YELLOW_BRICK_USERNAME).toString(),
                map.get(YELLOW_BRICK_PASSWORD) == null ? null : map.get(YELLOW_BRICK_PASSWORD).toString(),
                isTrackWind(map), isCorrectWindDirectionByMagneticDeclination(map), raceLogStore, regattaLogStore,
                baseDomainFactory, yellowBrickTrackingAdapter);
    }

    @Override
    public Map<String, Object> getKey(RaceTrackingConnectivityParameters params) throws MalformedURLException {
        assert params instanceof YellowBrickRaceTrackingConnectivityParams;
        final YellowBrickRaceTrackingConnectivityParams ybParams = (YellowBrickRaceTrackingConnectivityParams) params;
        final Map<String, Object> result = new HashMap<>();
        result.put(TypeBasedServiceFinder.TYPE, params.getTypeIdentifier());
        result.put(RACE_URL, ybParams.getRaceUrl());
        return result;
    }

    @Override
    public RaceTrackingConnectivityParameters resolve(RaceTrackingConnectivityParameters params) throws Exception {
        assert params instanceof YellowBrickRaceTrackingConnectivityParams;
        final YellowBrickRaceTrackingConnectivityParams ybParams = (YellowBrickRaceTrackingConnectivityParams) params;
        YellowBrickRaceTrackingConnectivityParams result = new YellowBrickRaceTrackingConnectivityParams(
                ybParams.getRaceUrl(), ybParams.getUsername(), ybParams.getPassword(), ybParams.isTrackWind(),
                ybParams.isCorrectWindDirectionByMagneticDeclination(), raceLogStore, regattaLogStore,
                baseDomainFactory, yellowBrickTrackingAdapter);
        updatePersistentYellowBrickConfiguration(result);
        return result;
    }

    private void updatePersistentYellowBrickConfiguration(YellowBrickRaceTrackingConnectivityParams params)
            throws MalformedURLException, IOException, ParseException, URISyntaxException {
        final String creatorName = SessionUtils.getPrincipal().toString();
        final YellowBrickConfigurationImpl yellowBrickConfiguration = new YellowBrickConfigurationImpl(params.getRaceUrl(),
                params.getRaceUrl(), params.getUsername(), params.getPassword(), creatorName);
        yellowBrickTrackingAdapter.addYellowBrickConfiguration(yellowBrickConfiguration);
        securityService.setDefaultOwnershipIfNotSet(yellowBrickConfiguration.getIdentifier());
    }
}
