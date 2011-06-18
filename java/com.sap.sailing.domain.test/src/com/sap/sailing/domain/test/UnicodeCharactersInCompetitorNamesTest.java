package com.sap.sailing.domain.test;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;

public class UnicodeCharactersInCompetitorNamesTest {
    private DomainFactory domainFactory;
    
    @Before
    public void setUp() {
        domainFactory = new DomainFactoryImpl();
    }
    
    @Test
    public void testFindUNicodeCharactersInCompetitorNames() throws MalformedURLException, FileNotFoundException, URISyntaxException {
        RaceTracker fourtyninerYellow_2 = domainFactory
                .createRaceTracker(
                        new URL(
                                "http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=5b08a9ee-9933-11e0-85be-406186cbf87c"),
                        new URI("tcp://germanmaster.traclive.dk:1520"), new URI("tcp://germanmaster.traclive.dk:1521"),
                        EmptyWindStore.INSTANCE);
        
        Iterable<Competitor> competitors = fourtyninerYellow_2.getRaceHandle().getRace().getCompetitors();
        for (Competitor competitor : competitors) {
            System.out.println(competitor.getName());
        }
    }
}
