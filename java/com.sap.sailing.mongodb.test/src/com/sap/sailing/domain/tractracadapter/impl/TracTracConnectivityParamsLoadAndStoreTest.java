package com.sap.sailing.domain.tractracadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.mongodb.test.AbstractConnectivityParamsLoadAndStoreTest;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TracTracConnectivityParamsLoadAndStoreTest extends AbstractConnectivityParamsLoadAndStoreTest {
    public TracTracConnectivityParamsLoadAndStoreTest() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testStoreAndLoadSimpleTracTracParams() throws MalformedURLException, URISyntaxException {
        // set up
        final URL paramURL = new URL("http://tractrac.com/some/url");
        final URI storedURI = new URI("live://tractrac.com/storedURI");
        final URI courseDesignUpdateURI = new URI("https://skitrac.dk/reverse/update");
        final TimePoint startOfTracking = MillisecondsTimePoint.now();
        final TimePoint endOfTracking = startOfTracking.plus(1000);
        final long delayToLiveInMillis = 3000;
        final Duration offsetToStartTimeOfSimulatedRace = Duration.ONE_MINUTE;
        final boolean useInternalMarkPassingAlgorithm = false;
        final String tracTracUsername = "user";
        final String tracTracPassword = "pass";
        final String raceStatus = (String) TracTracConnectionConstants.REPLAY_STATUS;
        final String raceVisibility = (String) TracTracConnectionConstants.REPLAY_VISIBILITY;
        final RaceTrackingConnectivityParameters tracTracParams = new RaceTrackingConnectivityParametersImpl(
                paramURL, /* live URI */ null, storedURI, courseDesignUpdateURI, startOfTracking, endOfTracking,
                delayToLiveInMillis, offsetToStartTimeOfSimulatedRace, useInternalMarkPassingAlgorithm,
                /* raceLogStore */ null, /* regattaLogStore */ null, DomainFactory.INSTANCE, tracTracUsername, tracTracPassword,
                raceStatus, raceVisibility);
        // store
        mongoObjectFactory.addConnectivityParametersForRaceToRestore(tracTracParams);
        // load
        final Iterable<RaceTrackingConnectivityParameters> connectivityParametersForRacesToRestore = domainObjectFactory.loadConnectivityParametersForRacesToRestore();
        // compare
        assertEquals(1, Util.size(connectivityParametersForRacesToRestore));
        final RaceTrackingConnectivityParameters paramsReadFromDB = connectivityParametersForRacesToRestore.iterator().next();
        assertTrue(paramsReadFromDB instanceof RaceTrackingConnectivityParametersImpl);
        RaceTrackingConnectivityParametersImpl tracTracParamsReadFromDB = (RaceTrackingConnectivityParametersImpl) paramsReadFromDB;
        assertEquals(delayToLiveInMillis, tracTracParamsReadFromDB.getDelayToLiveInMillis());
        assertEquals(paramURL, tracTracParamsReadFromDB.getParamURL());
        assertEquals(storedURI, tracTracParamsReadFromDB.getStoredURI());
        assertEquals(courseDesignUpdateURI, tracTracParamsReadFromDB.getCourseDesignUpdateURI());
        assertEquals(startOfTracking, tracTracParamsReadFromDB.getStartOfTracking());
        assertEquals(endOfTracking, tracTracParamsReadFromDB.getEndOfTracking());
        assertEquals(offsetToStartTimeOfSimulatedRace, tracTracParamsReadFromDB.getOffsetToStartTimeOfSimulatedRace());
        assertEquals(useInternalMarkPassingAlgorithm, tracTracParamsReadFromDB.isUseInternalMarkPassingAlgorithm());
        assertEquals(tracTracUsername, tracTracParamsReadFromDB.getTracTracUsername());
        assertEquals(tracTracPassword, tracTracParamsReadFromDB.getTracTracPassword());
        assertEquals(raceStatus, tracTracParamsReadFromDB.getRaceStatus());
        assertEquals(raceVisibility, tracTracParamsReadFromDB.getRaceVisibility());
        assertEquals(tracTracParams.getTrackerID(), tracTracParamsReadFromDB.getTrackerID());
    }
}
