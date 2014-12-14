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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class UnicodeCharactersInCompetitorNamesTest {
    protected static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    protected static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private DomainFactory domainFactory;
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(2 * 60 * 1000);

    @Before
    public void setUp() {
        domainFactory = new DomainFactoryImpl(new com.sap.sailing.domain.base.impl.DomainFactoryImpl());
    }
    
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, CreateModelException, SubscriberInitializationException {
        UnicodeCharactersInCompetitorNamesTest t = new UnicodeCharactersInCompetitorNamesTest();
        t.setUp();
        t.readJSONURLAndCheckCompetitorNames();
        t.testFindUnicodeCharactersInCompetitorNames();
    }
    
    @Test
    public void testFindUnicodeCharactersInCompetitorNames() throws MalformedURLException, FileNotFoundException, URISyntaxException, IOException, InterruptedException, CreateModelException, SubscriberInitializationException {
        TracTracRaceTracker fourtyninerYellow_2 = domainFactory
                .createRaceTracker(
                        new URL(
                                "http://"
                                        + TracTracConnectionConstants.HOST_NAME
                                        + "/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=5b08a9ee-9933-11e0-85be-406186cbf87c"),
                        tractracTunnel ? new URI("tcp://" + tractracTunnelHost + ":"
                                + TracTracConnectionConstants.PORT_TUNNEL_LIVE) : new URI("tcp://"
                                + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_LIVE),
                        tractracTunnel ? new URI("tcp://" + tractracTunnelHost + ":"
                                + TracTracConnectionConstants.PORT_TUNNEL_STORED)
                                : new URI("tcp://" + TracTracConnectionConstants.HOST_NAME + ":"
                                        + TracTracConnectionConstants.PORT_STORED),
                        new URI("http://tracms.traclive.dk/update_course"),
                        /* startOfTracking */null, /* endOfTracking */null, /* delayToLiveInMillis */0l,
                        /* simulateWithStartTimeNow */ false, /*ignoreTracTracMarkPassings*/false, EmptyRaceLogStore.INSTANCE, EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, "tracTest", "tracTest", "", "",
                        new DummyTrackedRegattaRegistry());

        Iterable<Competitor> competitors = fourtyninerYellow_2.getRacesHandle().getRace().getCompetitors();
        for (Competitor competitor : competitors) {
            System.out.println(competitor.getName());
        }
        fourtyninerYellow_2.stop(/* preemptive */ false);
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
                                "http://" + TracTracConnectionConstants.HOST_NAME + "/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=5b08a9ee-9933-11e0-85be-406186cbf87c")
                                .openStream(), charsetname));
        String line;
        while ((line=localBufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        localBufferedReader.close();
    }
}
