package com.sap.sailing.server.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.server.RacingEventServiceImpl;

public class RaceTrackerTest {
    protected static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    protected static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private final URL paramUrl;
    private final URI liveUri;
    private final URI storedUri;
    private RacingEventServiceImpl service;
    private RaceHandle raceHandle;
    
    public RaceTrackerTest() throws MalformedURLException, URISyntaxException {
        // for live simulation:
        //   paramUrl  = new URL("http://sapsimulation.tracdev.dk/simulateconf/j80race12.txt");
        //   liveUri   = new URI("tcp://sapsimulation.tracdev.dk:4420"); // or with tunneling: tcp://localhost:4420
        //   storedUri = new URI("tcp://sapsimulation.tracdev.dk:4421"); // or with tunneling: tcp://localhost:4421
        // for stored race, non-real-time simulation:
        paramUrl  = new URL("http://germanmaster.traclive.dk/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=bd8c778e-7c65-11e0-8236-406186cbf87c");
        
        if (tractracTunnel) {
            liveUri   = new URI("tcp://"+tractracTunnelHost+":4412");
            storedUri = new URI("tcp://"+tractracTunnelHost+":4413");
        } else {
            // no tunnel:
            liveUri = new URI("tcp://germanmaster.traclive.dk:4400");
            storedUri = new URI("tcp://germanmaster.traclive.dk:4401");
        }
    }
    
    @Before
    public void setUp() throws MalformedURLException, FileNotFoundException, URISyntaxException, InterruptedException {
        service = new RacingEventServiceImpl();
        raceHandle = service.addRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
    }
    
    @After
    public void tearDown() throws MalformedURLException, IOException, InterruptedException {
        service.stopTracking(raceHandle.getEvent());
    }

    private TrackedRace getTrackedRace(TrackedEvent trackedEvent) throws InterruptedException {
        final TrackedRace[] trackedRaces = new TrackedRace[1];
        trackedEvent.addRaceListener(new RaceListener() {
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
        RaceDefinition race = raceHandle.getRace();
        assertNotNull(race);
        assertNotNull(getTrackedRace(raceHandle.getTrackedEvent()));
    }
    
    @Test
    public void testStopTracking() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        TrackedEvent oldTrackedEvent = raceHandle.getTrackedEvent();
        TrackedRace oldTrackedRace = getTrackedRace(oldTrackedEvent);
        RaceDefinition oldRaceDefinition = oldTrackedRace.getRace();
        service.stopTracking(raceHandle.getEvent());
        RaceHandle myRaceHandle = service.addRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        TrackedEvent newTrackedEvent = myRaceHandle.getTrackedEvent();
        TrackedRace newTrackedRace = getTrackedRace(newTrackedEvent);
        // expecting a new tracked race to be created when starting over with tracking
        try {
            assertNotSame(oldTrackedRace, newTrackedRace);
            assertNotSame(oldRaceDefinition, newTrackedRace.getRace());
        } finally {
            service.stopTracking(myRaceHandle.getEvent());
        }
    }

    /**
     * This test asserts that tracking the same race twice doesn't create another tracker and in particular no
     * new tracked event / tracked race.
     */
    @Test
    public void testTrackingSameRaceWithoutStopping() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        TrackedEvent oldTrackedEvent = raceHandle.getTrackedEvent();
        TrackedRace oldTrackedRace = getTrackedRace(oldTrackedEvent);
        RaceHandle myRaceHandle = service.addRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        TrackedEvent newTrackedEvent = myRaceHandle.getTrackedEvent();
        TrackedRace newTrackedRace = getTrackedRace(newTrackedEvent);
        // expecting a new tracked race to be created when starting over with tracking
        try {
            assertSame(oldTrackedRace, newTrackedRace);
            assertSame(raceHandle.getRaceTracker(), myRaceHandle.getRaceTracker());
        } finally {
            service.stopTracking(myRaceHandle.getEvent());
        }
    }
}
