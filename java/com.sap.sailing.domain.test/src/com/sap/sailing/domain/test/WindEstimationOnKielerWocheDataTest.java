package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;
import com.tractrac.clientmodule.Race;

public class WindEstimationOnKielerWocheDataTest extends KielerWoche2011BasedTest {
    private RaceDefinition race;
    private DynamicTrackedRace trackedRace;

    public WindEstimationOnKielerWocheDataTest() throws MalformedURLException, URISyntaxException {
        super();
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException {
        super.setUp(new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        Race tractracRace = getEvent().getRaceList().iterator().next();
        race = getDomainFactory().getRaceDefinition(tractracRace);
        assertNotNull(race);
        synchronized (getSemaphor()) {
            while (!isStoredDataLoaded()) {
                getSemaphor().wait();
            }
        }
        trackedRace = getTrackedEvent().getTrackedRace(race);
    }
    
    private Competitor getCompetitorByName(String name) {
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    @Test
    public void testSetUp() {
        assertNotNull(trackedRace);
        assertTrue(Util.size(trackedRace.getTrack(getCompetitorByName("Dr.Plattner")).getFixes()) > 1000);
    }
}
