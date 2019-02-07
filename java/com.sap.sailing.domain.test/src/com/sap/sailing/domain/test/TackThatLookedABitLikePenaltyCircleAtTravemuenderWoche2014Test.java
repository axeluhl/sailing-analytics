package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
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

/**
 * This is a test for bug 1904, comments #3 and #4 (see http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1904).
 * It checks that an obvious penalty circle that wasn't detected is now detected.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TackThatLookedABitLikePenaltyCircleAtTravemuenderWoche2014Test extends AbstractManeuverDetectionTestCase {
    public TackThatLookedABitLikePenaltyCircleAtTravemuenderWoche2014Test() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    protected String getExpectedEventName() {
        // don't worry about the missing "r" at the end of "Kiele"; this is what we're getting from TracTrac
        return "Travemunder Woche 2014";
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, InterruptedException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("resources/event_20140714_Travemuende-Int14-R5.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(
                new URL("file:///" + new File("resources/event_20140714_Travemuende-Int14-R5.txt").getCanonicalPath()),
                /* liveUri */null, /* storedUri */storedUri, new ReceiverType[] { ReceiverType.MARKPASSINGS,
                        ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, new MillisecondsTimePoint(dateFormat.parse("07/25/2014-13:08:17")),
                        new KnotSpeedWithBearingImpl(12.8, new DegreeBearingImpl(46))),
                new WindSourceImpl(WindSourceType.WEB));
    }
    
    /**
     * Asserts that Truswell/Pascoe are having at least a tack detected around 13:08:10+0200. The problem with
     * this track at this point is that the COG doesn't really match up with the actual lat/lon changes. That's
     * why maneuver recognition during what really was a penalty circle is a challenge.
     */
    @Test
    public void testTackForTruswellAndPascoe() throws ParseException, NoWindException {
        assertTack("Truswell/Pascoe", "07/25/2014-13:08:00", "07/25/2014-13:09:00", "07/25/2014-13:08:10");
    }

    private void assertTack(String competitorName, final String from, final String to,
            final String penaltyTimePoint) throws ParseException, NoWindException {
        Competitor competitor = getCompetitorByName(competitorName);
        Date fromDate = dateFormat.parse(from);
        Date toDate = dateFormat.parse(to);
        assertNotNull(fromDate);
        assertNotNull(toDate);
        assertNotNull(competitor);
        Iterable<Maneuver> maneuvers = getTrackedRace().getManeuvers(competitor, new MillisecondsTimePoint(fromDate),
                new MillisecondsTimePoint(toDate), /* waitForLatest */ true);
        maneuversInvalid = new ArrayList<Maneuver>();
        Util.addAll(maneuvers, maneuversInvalid);
        for (Maneuver maneuver : maneuvers) {
            if (maneuver.getType() == ManeuverType.TACK) {
                assertTrue(Math.abs(maneuver.getDirectionChangeInDegrees()) < 700); // the second penalty has to count for its own
            }
        }
        assertManeuver(maneuvers, ManeuverType.TACK, new MillisecondsTimePoint(dateFormat.parse(penaltyTimePoint)), 5000);
    }
}
