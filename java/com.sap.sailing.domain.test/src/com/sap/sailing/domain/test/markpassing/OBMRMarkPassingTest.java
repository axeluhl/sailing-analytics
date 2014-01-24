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

public class OBMRMarkPassingTest extends AbstractMarkPassingTest {

    public  OBMRMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testRace1() throws IOException, InterruptedException, URISyntaxException {
        setUp("65099a64-245d-11e2-9635-10bf48d758ce");
        testRace();
        testStartOfRace();
    }
    
    protected void setUp(String raceID) throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        if (forceReload && !loadData(raceID)) {
            System.out.println("Downloading new data from the web.");
            setUp("event_20121031_OBMR",
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
        return "OBMR 2012";
    }
}
