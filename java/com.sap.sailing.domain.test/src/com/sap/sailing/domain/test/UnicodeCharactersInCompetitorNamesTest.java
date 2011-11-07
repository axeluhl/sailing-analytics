package com.sap.sailing.domain.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;

@Ignore
public class UnicodeCharactersInCompetitorNamesTest {
    private DomainFactory domainFactory;
    
    @Before
    public void setUp() {
        domainFactory = new DomainFactoryImpl();
    }
    
    public static void main(String[] args) throws URISyntaxException, IOException {
        UnicodeCharactersInCompetitorNamesTest t = new UnicodeCharactersInCompetitorNamesTest();
        t.setUp();
        t.readJSONURLAndCheckCompetitorNames();
        t.testFindUnicodeCharactersInCompetitorNames();
    }
    
    @Ignore
    @Test
    public void testFindUnicodeCharactersInCompetitorNames() throws MalformedURLException, FileNotFoundException, URISyntaxException {
        TracTracRaceTracker fourtyninerYellow_2 = domainFactory
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
    
    @Test
    public void readJSONURLAndCheckCompetitorNames() throws MalformedURLException, IOException {
        System.out.println("Default charset: " + Charset.defaultCharset().name() + ". Supported: "
                + Charset.isSupported(Charset.defaultCharset().name()));
        String charsetname = System.getProperty("test.charset", "UTF-8");
        System.out.println("Using "+charsetname+" for input stream reader");
        BufferedReader localBufferedReader = new BufferedReader(
                new InputStreamReader(
                        new URL(
                                "http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=5b08a9ee-9933-11e0-85be-406186cbf87c")
                                .openStream(), charsetname));
        String line;
        while ((line=localBufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        localBufferedReader.close();
    }
}
