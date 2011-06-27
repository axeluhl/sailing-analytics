package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;

public class MultipleClassesInEventTest {
    private DomainFactory domainFactory;
    
    @Before
    public void setUp() {
        domainFactory = new DomainFactoryImpl();
    }
    
    @Test
    public void testLoadTwoRacesWithEqualEventNameButDifferentClasses() throws MalformedURLException, FileNotFoundException, URISyntaxException {
        String httpAndHost = "http://germanmaster.traclive.dk";
        String liveURI = "tcp://germanmaster.traclive.dk:4400";
        String storedURI = "tcp://germanmaster.traclive.dk:4401";
        if (Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"))) {
            httpAndHost = "http://localhost:12348";
            liveURI   = "tcp://localhost:4412";
            storedURI = "tcp://localhost:4413";
        }
        RaceTracker kiwotest1 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=cce678c8-97e6-11e0-9aed-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI),
                        EmptyWindStore.INSTANCE);
        RaceTracker kiwotest2 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=11290bd6-97e7-11e0-9aed-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI),
                        EmptyWindStore.INSTANCE);
        RaceTracker kiwotest3 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=39635b24-97e7-11e0-9aed-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI),
                        EmptyWindStore.INSTANCE);
        RaceTracker weym470may112014_2 = domainFactory
                .createRaceTracker(
                        new URL(
                                httpAndHost+"/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=04498426-7dfd-11e0-8236-406186cbf87c"),
                        new URI(liveURI), new URI(storedURI),
                        EmptyWindStore.INSTANCE);
        
        assertEquals("STG", kiwotest1.getEvent().getBoatClass().getName());
        assertEquals("505", kiwotest2.getEvent().getBoatClass().getName());
        assertEquals("49er", kiwotest3.getEvent().getBoatClass().getName());
        assertEquals("STG", weym470may112014_2.getEvent().getBoatClass().getName());
        assertSame(weym470may112014_2.getEvent(), kiwotest1.getEvent());
        assertNotSame(kiwotest1.getEvent(), kiwotest2.getEvent());
        assertNotSame(kiwotest1.getEvent(), kiwotest3.getEvent());
        assertNotSame(kiwotest2.getEvent(), kiwotest3.getEvent());
    }
}
