package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class MultipleClassesInRegattaTest {
    private static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    private static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private DomainFactory domainFactory;
    private TracTracRaceTracker kiwotest1;
    private TracTracRaceTracker kiwotest2;
    private TracTracRaceTracker kiwotest3;
    private TracTracRaceTracker weym470may112014_2;
    
    @Before
    public void setUp() {
        domainFactory = new DomainFactoryImpl(new com.sap.sailing.domain.base.impl.DomainFactoryImpl());
    }
    
    @Test
    public void testLoadTwoRacesWithEqualEventNameButDifferentClasses() throws MalformedURLException, FileNotFoundException, URISyntaxException, CreateModelException, SubscriberInitializationException {
        String httpAndHost = "http://" + TracTracConnectionConstants.HOST_NAME;
        String liveURI = "tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_LIVE;
        String storedURI = "tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_STORED;
        String courseDesignUpdateURI = "http://tracms.traclive.dk/update_course";
        String tracTracUsername = "tracTest";
        String tracTracPassword = "tracTest";
        if (tractracTunnel) {
            liveURI   = "tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_LIVE;
            storedURI = "tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_STORED;
        }
        kiwotest1 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=cce678c8-97e6-11e0-9aed-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI), new URI(courseDesignUpdateURI),
                        /* startOfTracking */ null, /* endOfTracking */ null, /* delayToLiveInMillis */ 0l, /* simulateWithStartTimeNow */ false,
                        EmptyRaceLogStore.INSTANCE, EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, tracTracUsername, tracTracPassword, TracTracConnectionConstants.ONLINE_STATUS, TracTracConnectionConstants.ONLINE_VISIBILITY, new DummyTrackedRegattaRegistry());
        kiwotest2 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=11290bd6-97e7-11e0-9aed-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI), new URI(courseDesignUpdateURI),
                        /* startOfTracking */ null, /* endOfTracking */ null, /* delayToLiveInMillis */ 0l, /* simulateWithStartTimeNow */ false, EmptyRaceLogStore.INSTANCE, EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, tracTracUsername, tracTracPassword, TracTracConnectionConstants.ONLINE_STATUS, TracTracConnectionConstants.ONLINE_VISIBILITY, new DummyTrackedRegattaRegistry());
        kiwotest3 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=39635b24-97e7-11e0-9aed-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI), new URI(courseDesignUpdateURI),
                        /* startOfTracking */ null, /* endOfTracking */ null, /* delayToLiveInMillis */ 0l, /* simulateWithStartTimeNow */ false, EmptyRaceLogStore.INSTANCE, EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, tracTracUsername, tracTracPassword, TracTracConnectionConstants.ONLINE_STATUS, TracTracConnectionConstants.ONLINE_VISIBILITY, new DummyTrackedRegattaRegistry());
        weym470may112014_2 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=04498426-7dfd-11e0-8236-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI), new URI(courseDesignUpdateURI),
                        /* startOfTracking */ null, /* endOfTracking */ null, /* delayToLiveInMillis */ 0l, /* simulateWithStartTimeNow */ false, EmptyRaceLogStore.INSTANCE, EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, tracTracUsername, tracTracPassword, TracTracConnectionConstants.ONLINE_STATUS, TracTracConnectionConstants.ONLINE_VISIBILITY, new DummyTrackedRegattaRegistry());
        
        assertEquals("STG", kiwotest1.getRegatta().getBoatClass().getName());
        assertEquals("505", kiwotest2.getRegatta().getBoatClass().getName());
        assertEquals("49er", kiwotest3.getRegatta().getBoatClass().getName());
        assertEquals("STG", weym470may112014_2.getRegatta().getBoatClass().getName());
        assertNotSame(kiwotest1.getRegatta(), kiwotest2.getRegatta());
        assertNotSame(kiwotest1.getRegatta(), kiwotest3.getRegatta());
        assertNotSame(kiwotest2.getRegatta(), kiwotest3.getRegatta());
    }
    
    @After
    public void tearDown() throws MalformedURLException, IOException, InterruptedException {
        kiwotest1.stop(/* preemptive */ false);
        kiwotest2.stop(/* preemptive */ false);
        kiwotest3.stop(/* preemptive */ false);
        weym470may112014_2.stop(/* preemptive */ false);
    }
}
