package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.server.RacingEventServiceImpl;
import com.sap.sailing.util.Util;

public class RaceTrackerTest {
    private final URL paramUrl;
    private final URI liveUri;
    private final URI storedUri;
    private RacingEventServiceImpl service;
    private DomainFactory domainFactory;
    private RaceHandle raceHandle;
    
    public RaceTrackerTest() throws MalformedURLException, URISyntaxException {
        // for live simulation:
        //   paramUrl  = new URL("http://sapsimulation.tracdev.dk/simulateconf/j80race12.txt");
        //   liveUri   = new URI("tcp://sapsimulation.tracdev.dk:4420"); // or with tunneling: tcp://localhost:4420
        //   storedUri = new URI("tcp://sapsimulation.tracdev.dk:4421"); // or with tunneling: tcp://localhost:4421
        // for stored race, non-real-time simulation:
        paramUrl  = new URL("http://germanmaster.traclive.dk/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=bd8c778e-7c65-11e0-8236-406186cbf87c");
        
        // tunneled:
        //liveUri   = new URI("tcp://localhost:4412");
        //storedUri = new URI("tcp://localhost:4413");
        
        //no tunnel:
        liveUri   = new URI("tcp://germanmaster.traclive.dk:4400");
        storedUri = new URI("tcp://germanmaster.traclive.dk:4401");
    }
    
    @Before
    public void setUp() throws MalformedURLException, FileNotFoundException, URISyntaxException {
        service = new RacingEventServiceImpl();
        raceHandle = service.addRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE);
        domainFactory = service.getDomainFactory();
    }
    
    @Test
    public void testInitialization() throws InterruptedException {
        RaceDefinition race = raceHandle.getRace();
        assertNotNull(race);
        TrackedEvent trackedEvent = domainFactory.trackEvent(raceHandle.getEvent());
        final boolean[] gotTrackedRace = new boolean[1];
        trackedEvent.addRaceListener(new RaceListener() {
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                synchronized (gotTrackedRace) {
                    gotTrackedRace[0] = true;
                    gotTrackedRace.notifyAll();
                }
            }
        });
        synchronized (gotTrackedRace) {
            if (!gotTrackedRace[0]) {
                gotTrackedRace.wait();
            }
        }
        assertEquals(1, Util.size(trackedEvent.getTrackedRaces()));
    }
    
    @Test
    public void testStopTracking() {
        
    }
}
