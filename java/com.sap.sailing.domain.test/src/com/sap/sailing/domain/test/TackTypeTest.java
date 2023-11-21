package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.GregorianCalendar;
import org.junit.Before;
import org.junit.Test;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TackType;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TackTypeTest extends OnlineTracTracBasedTest {

    public TackTypeTest() throws MalformedURLException, URISyntaxException {
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("resources/event_20110609_KielerWoch-505_Race_2.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:///"+new File("resources/event_20110609_KielerWoch-505_Race_2.txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(), new MillisecondsTimePoint(
                new GregorianCalendar(2011, 05, 23).getTime()));
            getTrackedRace().recordWind(
                new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(8,
                        new DegreeBearingImpl(220))), new WindSourceImpl(WindSourceType.WEB));
    }
    @Test
    public void TestShortTack() throws NoWindException {
        final Competitor findel = getCompetitorByName("Findel");
        TrackedLegOfCompetitor findelsFirstLeg = getTrackedRace().getTrackedLeg(getTrackedRace().getRace().getCourse().getLegs().get(0)).getTrackedLeg(findel);
        TimePoint findelStartedHisSecondLegAt = findelsFirstLeg.getStartTime();
      //00:00:30 später als Legstart um 15:27:35
        TackType testcase = findelsFirstLeg.getTackType(findelStartedHisSecondLegAt.plus(30000));
        assertEquals(testcase, TackType.SHORTTACK);
    }
    @Test
    public void TestLongTack() throws NoWindException {
        final Competitor findel = getCompetitorByName("Findel");
        TrackedLegOfCompetitor findelsFirstLeg = getTrackedRace().getTrackedLeg(getTrackedRace().getRace().getCourse().getLegs().get(0)).getTrackedLeg(findel);
        TimePoint findelStartedHisSecondLegAt = findelsFirstLeg.getStartTime();
        //00:01:30 später als Legstart um 15:27:35
        TackType testcase = findelsFirstLeg.getTackType(findelStartedHisSecondLegAt.plus(90000));
        assertEquals(testcase, TackType.LONGTACK);
    } 
}
