package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class FlorianopolisMarkPassingTest extends AbstractMarkPassingTest {

    public  FlorianopolisMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }
   
    @Test
    public void testRace1() throws IOException, InterruptedException, URISyntaxException {
        setUp("bca3b490-2dce-0131-27f0-60a44ce903c3");
        testRace();
        testStartOfRace();
    }
    @Test
    public void testRace2() throws IOException, InterruptedException, URISyntaxException {
        setUp("52697ec0-2dd0-0131-2802-60a44ce903c3");
        testRace();
        testStartOfRace();
    }
    @Test
    public void testRace3() throws IOException, InterruptedException, URISyntaxException {
        setUp("528a0f30-2dd0-0131-2819-60a44ce903c3");
        testRace();
        testStartOfRace();
    }
    @Test
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        setUp("529a4150-2dd0-0131-2830-60a44ce903c3");
        testRace();
        testStartOfRace();
        testFirstTwoWaypoints();
    }
    
    protected void setUp(String raceID) throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        if (forceReload && !loadData(raceID)) {
            System.out.println("Downloading new data from the web.");
            setUp("event_20131112_ESSFlorian",
            /* raceId */raceID, new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS,
                    ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
            getTrackedRace().recordWind(
                    new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                            new DegreeBearingImpl(65))), new WindSourceImpl(WindSourceType.WEB));
            saveData();
        }
    }

    @Override
    protected String getExpectedEventName() {
        return "ESS Florianopolis 2013";
    }
}
