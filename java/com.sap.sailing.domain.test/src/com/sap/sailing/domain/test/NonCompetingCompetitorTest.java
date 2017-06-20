package com.sap.sailing.domain.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class NonCompetingCompetitorTest extends AbstractManeuverDetectionTestCase {
    public NonCompetingCompetitorTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    protected String getExpectedEventName() {
        return "ESS ST. Petersburg 2016";
    }


    @Before
    public void setUp() throws URISyntaxException, IOException, InterruptedException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        super.setUp(
                new URL("http://dev.tractrac.com/events/event_20160222_ESSSTPeter_1473057208/clientparams.php?event=event_20160222_ESSSTPeter_1473057208&race=5a265e70-519a-0134-dd23-10b11c4ed8fd&random=296858907"),
                /* liveUri */null, /* storedUri */null, new ReceiverType[] { ReceiverType.MARKPASSINGS,
                        ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
    }
    
    /**
     * Asserts that the "Ump..." competitors are considered non-competing and therefore don't show up
     */
    @Test
    public void testNoNonCompetingCompetitors() throws ParseException, NoWindException {
    	for (final Entry<Competitor, Boat> competitorAndBoatEntry : getTrackedRace().getRace().getCompetitorsAndTheirBoats().entrySet()) {
    		assertFalse(competitorAndBoatEntry.getKey().getName().startsWith("Ump"));
    		assertSame(getDomainFactory().getOrCreateBoatClass("GC32"), competitorAndBoatEntry.getValue().getBoatClass());
    	}
    }
}
