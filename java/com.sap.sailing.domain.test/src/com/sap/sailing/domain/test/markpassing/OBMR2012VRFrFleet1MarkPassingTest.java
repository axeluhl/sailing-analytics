package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

public class OBMR2012VRFrFleet1MarkPassingTest extends AbstractMarkPassingTest {

    public  OBMR2012VRFrFleet1MarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testRace1() throws IOException, InterruptedException, URISyntaxException {
        testRace(1);
    }
    
    @Override
    protected String getExpectedEventName() {
        return "OBMR 2012";
    }

    @Override
    protected String getFileName() {
        return "event_20121031_OBMR-OBMR_2012_VR_Fr_Fleet_1_";
    }
}
