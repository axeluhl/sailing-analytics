package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.domain.tractracadapter.impl.RaceTrackingConnectivityParametersImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ConnectivityParamsLoadAndStoreTest extends AbstractMongoDBTest {
    public ConnectivityParamsLoadAndStoreTest() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testStoreAndLoadSimpleTracTracParams() throws MalformedURLException, URISyntaxException {
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
        RaceTrackingConnectivityParameters tracTracParams = new RaceTrackingConnectivityParametersImpl(
                paramURL, /* live URI */ null, storedURI, courseDesignUpdateURI, startOfTracking, endOfTracking,
                delayToLiveInMillis, offsetToStartTimeOfSimulatedRace, useInternalMarkPassingAlgorithm,
                /* raceLogStore */ null, /* regattaLogStore */ null, DomainFactory.INSTANCE, tracTracUsername, tracTracPassword,
                raceStatus, raceVisibility);
        TypeBasedServiceFinderFactory serviceFinderFactory = new MockConnectivityParamsServiceFinderFactory();
        final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService(), serviceFinderFactory);
        final DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), com.sap.sailing.domain.base.DomainFactory.INSTANCE, serviceFinderFactory);
        mongoObjectFactory.addConnectivityParametersForRaceToRestore(tracTracParams);
        final Iterable<RaceTrackingConnectivityParameters> connectivityParametersForRacesToRestore = domainObjectFactory.loadConnectivityParametersForRacesToRestore();
        assertEquals(1, Util.size(connectivityParametersForRacesToRestore));
        final RaceTrackingConnectivityParameters paramsReadFromDB = connectivityParametersForRacesToRestore.iterator().next();
        assertEquals(delayToLiveInMillis, paramsReadFromDB.getDelayToLiveInMillis());
    }
}
