package com.sap.sailing.server.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class RaceTrackerTest {
    protected static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    protected static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private final URL paramUrl;
    private final URI liveUri;
    private final URI storedUri;
    private RacingEventServiceImpl service;
    private RacesHandle raceHandle;
    
    public RaceTrackerTest() throws MalformedURLException, URISyntaxException {
        // for live simulation:
        //   paramUrl  = new URL("http://sapsimulation.tracdev.dk/simulateconf/j80race12.txt");
        //   liveUri   = new URI("tcp://sapsimulation.tracdev.dk:4420"); // or with tunneling: tcp://localhost:4420
        //   storedUri = new URI("tcp://sapsimulation.tracdev.dk:4421"); // or with tunneling: tcp://localhost:4421
        // for stored race, non-real-time simulation:
        paramUrl  = new URL("http://" + TracTracConnectionConstants.HOST_NAME + "/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=bd8c778e-7c65-11e0-8236-406186cbf87c");
        
        if (tractracTunnel) {
            liveUri   = new URI("tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_LIVE);
            storedUri = new URI("tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_STORED);
        } else {
            // no tunnel:
            liveUri = new URI("tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_LIVE);
            storedUri = new URI("tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_STORED);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        service = new RacingEventServiceImpl();
        raceHandle = service.addTracTracRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        raceHandle.getRaces(); // wait for RaceDefinition to be completely wired in Event
    }
    
    @After
    public void tearDown() throws MalformedURLException, IOException, InterruptedException {
        service.stopTracking(raceHandle.getRegatta());
    }

    private TrackedRace getTrackedRace(TrackedRegatta trackedRegatta) throws InterruptedException {
        final TrackedRace[] trackedRaces = new TrackedRace[1];
        trackedRegatta.addRaceListener(new RaceListener() {
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                synchronized (trackedRaces) {
                    trackedRaces[0] = trackedRace;
                    trackedRaces.notifyAll();
                }
            }
            @Override
            public void raceRemoved(TrackedRace trackedRace) {
            }
        });
        synchronized (trackedRaces) {
            if (trackedRaces[0] == null) {
                trackedRaces.wait();
            }
        }
        return trackedRaces[0];
    }

    @Test
    public void testInitialization() throws InterruptedException {
        Set<RaceDefinition> races = raceHandle.getRaces();
        assertNotNull(races);
        assertFalse(races.isEmpty());
        assertNotNull(getTrackedRace(raceHandle.getTrackedRegatta()));
    }
    
    @Test
    public void testStopTracking() throws Exception {
        TrackedRegatta oldTrackedEvent = raceHandle.getTrackedRegatta();
        TrackedRace oldTrackedRace = getTrackedRace(oldTrackedEvent);
        RaceDefinition oldRaceDefinition = oldTrackedRace.getRace();
        service.removeEvent(raceHandle.getRegatta());
        RacesHandle myRaceHandle = service.addTracTracRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        TrackedRegatta newTrackedEvent = myRaceHandle.getTrackedRegatta();
        assertNotSame(oldTrackedEvent, newTrackedEvent);
        TrackedRace newTrackedRace = getTrackedRace(newTrackedEvent);
        // expecting a new tracked race to be created when starting over with tracking
        try {
            assertNotSame(oldTrackedRace, newTrackedRace);
            assertNotSame(oldRaceDefinition, newTrackedRace.getRace());
        } finally {
            service.stopTracking(myRaceHandle.getRegatta());
        }
    }

    /**
     * This test asserts that tracking the same race twice doesn't create another tracker and in particular no
     * new tracked regatta / tracked race.
     * @throws Exception 
     */
    @Test
    public void testTrackingSameRaceWithoutStopping() throws Exception {
        TrackedRegatta oldTrackedRegatta = raceHandle.getTrackedRegatta();
        TrackedRace oldTrackedRace = getTrackedRace(oldTrackedRegatta);
        RacesHandle myRaceHandle = service.addTracTracRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        TrackedRegatta newTrackedEvent = myRaceHandle.getTrackedRegatta();
        TrackedRace newTrackedRace = getTrackedRace(newTrackedEvent);
        // expecting a new tracked race to be created when starting over with tracking
        try {
            assertSame(oldTrackedRace, newTrackedRace);
            assertSame(raceHandle.getRaceTracker(), myRaceHandle.getRaceTracker());
        } finally {
            service.stopTracking(myRaceHandle.getRegatta());
        }
    }
}
