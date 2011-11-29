package com.sap.sailing.domain.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Maneuver.Type;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class DouglasPeuckerManeuverTest extends KielWeek2011BasedTest {

    public DouglasPeuckerManeuverTest() throws URISyntaxException, IOException, InterruptedException {
        super();
        super.setUp();
        super.setUp("event_20110609_KielerWoch",
        /* raceId */"357c700a-9d9a-11e0-85be-406186cbf87c", new ReceiverType[] { ReceiverType.MARKPASSINGS,
                ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        KielWeek2011BasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace());
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                        new DegreeBearingImpl(70))), WindSource.WEB);
    }

    @Test
    public void testDouglasPeuckerForFindel() throws ParseException {
        Competitor competitor = getCompetitorByName("Findel");
        assertNotNull(competitor);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
        Date fromDate = format.parse("06/23/2011-15:28:28");
        Date toDate = format.parse("06/23/2011-16:38:03");
        assertNotNull(fromDate);
        assertNotNull(toDate);
        List<Maneuver> maneuvers = getTrackedRace().getManeuvers(competitor, new MillisecondsTimePoint(fromDate),
                new MillisecondsTimePoint(toDate));
        assertManeuversCounts(maneuvers);
    }

    private void assertManeuversCounts(List<Maneuver> maneuverList) {
        int maneuverHeadUpCount = 0;
        int maneuverBearAwayCount = 0;
        int maneuverTackCount = 0;
        int maneuverJibecount = 0;
        int maneuverPenaltyCircle = 0;
        for (Maneuver maneuver : maneuverList) {
            Type type = maneuver.getType();
            switch (type) {
            case HEAD_UP:
                maneuverHeadUpCount++;
                break;
            case BEAR_AWAY:
                maneuverBearAwayCount++;
                break;
            case TACK:
                maneuverTackCount++;
                break;
            case JIBE:
                maneuverJibecount++;
                break;
            case PENALTY_CIRCLE:
                maneuverPenaltyCircle++;
                break;
            }
        }
        
        assertEquals(maneuverTackCount, 19);
        assertEquals(maneuverJibecount, 8);
        assertEquals(maneuverPenaltyCircle, 1);
    }

}
