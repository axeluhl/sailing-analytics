package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingAdapterFactoryImpl;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayConnectivityParameters;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayServiceFactoryImpl;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sse.common.Util;

public class SwissTimingReplayConnectivityParamsLoadAndStoreTest extends AbstractConnectivityParamsLoadAndStoreTest {
    public SwissTimingReplayConnectivityParamsLoadAndStoreTest() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testStoreAndLoadSwissTimingReplayParams() throws MalformedURLException, URISyntaxException {
        final String link = "a.b.com?race=1234&_start=0";
        final String raceID = "Race ID 123";
        final String raceName = "The Race";
        final String boatClassName = "49er";
        final boolean useInternalMarkPassingAlgorithm = false;
        final SwissTimingAdapter swissTimingAdapter = new SwissTimingAdapterFactoryImpl().getOrCreateSwissTimingAdapter(domainObjectFactory.getBaseDomainFactory());
        final SwissTimingReplayConnectivityParameters stParams = new SwissTimingReplayConnectivityParameters(
                link, raceName, raceID, boatClassName, useInternalMarkPassingAlgorithm, swissTimingAdapter.getSwissTimingDomainFactory(),
                new SwissTimingReplayServiceFactoryImpl().createSwissTimingReplayService(swissTimingAdapter.getSwissTimingDomainFactory(), racingEventService),
                /* raceLogStore */ null, /* regattaLogStore */ null);
        // store
        mongoObjectFactory.addConnectivityParametersForRaceToRestore(stParams);
        // load
        final Iterable<RaceTrackingConnectivityParameters> connectivityParametersForRacesToRestore = domainObjectFactory.loadConnectivityParametersForRacesToRestore();
        // compare
        assertEquals(1, Util.size(connectivityParametersForRacesToRestore));
        final RaceTrackingConnectivityParameters paramsReadFromDB = connectivityParametersForRacesToRestore.iterator().next();
        assertTrue(paramsReadFromDB instanceof SwissTimingReplayConnectivityParameters);
        SwissTimingReplayConnectivityParameters stParamsReadFromDB = (SwissTimingReplayConnectivityParameters) paramsReadFromDB;
        assertEquals(link, stParamsReadFromDB.getLink());
        assertEquals(boatClassName, stParamsReadFromDB.getBoatClassName());
        assertEquals(raceID, stParamsReadFromDB.getRaceID());
        assertEquals(raceName, stParamsReadFromDB.getRaceName());
        assertEquals(stParams.getTrackerID(), stParamsReadFromDB.getTrackerID());
        assertEquals(useInternalMarkPassingAlgorithm, stParams.isUseInternalMarkPassingAlgorithm());
        // remove again
        mongoObjectFactory.removeConnectivityParametersForRaceToRestore(stParams);
        assertTrue(Util.isEmpty(domainObjectFactory.loadConnectivityParametersForRacesToRestore()));
    }
}
