package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveRankingMetric;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

/**
 * See bug 5122; Test cases for {@link ORCPerformanceCurveRankingMetric}
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ORCPerformanceCurveRankingTest extends OnlineTracTracBasedTest {
    final static MillisecondsTimePoint TIME_14_30_00 = new MillisecondsTimePoint(1555849800000l);
    final static MillisecondsTimePoint TIME_14_30_12 = new MillisecondsTimePoint(1555849812000l);

    public ORCPerformanceCurveRankingTest() throws MalformedURLException, URISyntaxException {
    }
    
    @Override
    protected String getExpectedEventName() {
        return "D-Marin ORC World Championship";
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("resources/orc/worlds2019/c5a516a0-69f7-0137-dd9c-60a44ce903c3.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:///"+new File("resources/orc/worlds2019/c5a516a0-69f7-0137-dd9c-60a44ce903c3.txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE,
                        ReceiverType.RACESTARTFINISH });
        getTrackedRace().recordWind(new WindImpl(/* position */ new DegreePosition(44.37670797575265, 8.925960855558515), TIME_14_30_00,
                new KnotSpeedWithBearingImpl(7.5, new DegreeBearingImpl(246).reverse())), new WindSourceImpl(WindSourceType.WEB));
    }
    
    @Test
    public void testVadnaiLead() {
        Competitor vadnai = getCompetitorByName("Benjamin Vadnai");
        Competitor lewis = getCompetitorByName("Andrew Lewis");
        Competitor lynch = getCompetitorByName("Finn Lynch");
        Competitor blanco = getCompetitorByName("JoaquÃ­n Blanco");
        assertEquals(1, getTrackedRace().getRank(vadnai, TIME_14_30_00));
        assertEquals(2, getTrackedRace().getRank(lewis, TIME_14_30_00));
        assertEquals(3, getTrackedRace().getRank(lynch, TIME_14_30_00));
        assertEquals(4, getTrackedRace().getRank(blanco, TIME_14_30_00));
        assertEquals(1, getTrackedRace().getRank(vadnai, TIME_14_30_12));
        assertEquals(2, getTrackedRace().getRank(lewis, TIME_14_30_12));
        assertEquals(3, getTrackedRace().getRank(lynch, TIME_14_30_12));
        assertEquals(4, getTrackedRace().getRank(blanco, TIME_14_30_12));
    }

}
