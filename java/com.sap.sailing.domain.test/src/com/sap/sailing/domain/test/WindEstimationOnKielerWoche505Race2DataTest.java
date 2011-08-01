package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;

public class WindEstimationOnKielerWoche505Race2DataTest extends KielerWoche2011BasedTest {

    public WindEstimationOnKielerWoche505Race2DataTest() throws MalformedURLException, URISyntaxException {
        super("357c700a-9d9a-11e0-85be-406186cbf87c");  // 505 Race 2: ID = 357c700a-9d9a-11e0-85be-406186cbf87c
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException {
        super.setUp(new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
    }
    
    @Test
    public void testSetUp() {
        assertNotNull(getTrackedRace());
        assertTrue(Util.size(getTrackedRace().getTrack(getCompetitorByName("Dr.Plattner")).getFixes()) > 1000);
    }
}
