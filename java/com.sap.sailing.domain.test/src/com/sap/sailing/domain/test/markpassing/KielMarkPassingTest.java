package com.sap.sailing.domain.test.markpassing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class KielMarkPassingTest extends AbstractMarkPassingTest {

    public  KielMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }
   

    @Test
    public void testRace2() throws IOException, InterruptedException, URISyntaxException {
        setUp(2);
        testRace();
        testStartOfRace();
    }
    @Test
    public void testRace3() throws IOException, InterruptedException, URISyntaxException {
        setUp(3);
        testRace();
        testStartOfRace();
    }
    @Test
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        setUp(4);
        testRace();
        testStartOfRace();
    }
    
    public void setUp(int race) throws URISyntaxException, IOException, InterruptedException {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("resources/event_20110609_KielerWoch-505_Race_"+race+".mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:///"+new File("resources/event_20110609_KielerWoch-505_Race_"+race+".txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(), new MillisecondsTimePoint(
                new GregorianCalendar(2011, 05, 23).getTime()));
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                        new DegreeBearingImpl(65))), new WindSourceImpl(WindSourceType.WEB));
    }

}

