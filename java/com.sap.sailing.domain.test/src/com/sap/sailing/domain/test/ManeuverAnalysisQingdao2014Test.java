package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class ManeuverAnalysisQingdao2014Test extends AbstractManeuverDetectionTestCase {
    public ManeuverAnalysisQingdao2014Test() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    protected String getExpectedEventName() {
        return "ESS Qingdao 2014";
    }


    @Before
    public void setUp() throws URISyntaxException, IOException, InterruptedException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("resources/event_20140429_ESSQingdao-Race_4.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(
                new URL("file:///" + new File("resources/event_20140429_ESSQingdao-Race_4.txt").getCanonicalPath()),
                /* liveUri */null, /* storedUri */storedUri, new ReceiverType[] { ReceiverType.MARKPASSINGS,
                        ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, new MillisecondsTimePoint(dateFormat.parse("05/01/2014-09:02:00")),
                        new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(269))),
                new WindSourceImpl(WindSourceType.WEB));
    }
    
    /**
     * Tests the Race 4 for competitor "Alinghi" at a time where the maneuver detection test is likely to compute
     * a penalty although between the jibe and the tack there is a mark passing
     */
    @Test
    public void testManeuversForAlinghiCriticalDetection() throws ParseException, NoWindException {
        Competitor competitor = getCompetitorByName("Alinghi");
        assertNotNull(competitor);
        Date fromDate = dateFormat.parse("05/01/2014-09:07:00");
        Date toDate = dateFormat.parse("05/01/2014-09:10:00");
        assertNotNull(fromDate);
        assertNotNull(toDate);
        Iterable<Maneuver> maneuvers = getTrackedRace().getManeuvers(competitor, new MillisecondsTimePoint(fromDate),
                new MillisecondsTimePoint(toDate), /* waitForLatest */ true);
        maneuversInvalid = new ArrayList<Maneuver>();
        Util.addAll(maneuvers, maneuversInvalid);
        assertManeuver(maneuvers, ManeuverType.JIBE, new MillisecondsTimePoint(dateFormat.parse("05/01/2014-09:08:15")), 5000);
        assertManeuver(maneuvers, ManeuverType.TACK, new MillisecondsTimePoint(dateFormat.parse("05/01/2014-09:08:56")), 5000);
        for (Maneuver maneuver : maneuvers) {
            // make sure there is no penalty detected in the time frame considered
            assertNotSame("Found an unexpected penalty "+maneuver, ManeuverType.PENALTY_CIRCLE, maneuver.getType());
        }
    }
}
