package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Maneuver.Type;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class ManouverAnalysisTest extends KielWeek2011BasedTest {

    private SimpleDateFormat dateFormat;
    private long tackDelta;
    private long jibeDelta;
    private long penaltyCircleDelta;

    public ManouverAnalysisTest() throws URISyntaxException, IOException, InterruptedException {
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
        dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");

        // TODO correct milliseconds of the type of Manouvers
        tackDelta = 20 * 1000;
        jibeDelta = 30 * 1000;
        penaltyCircleDelta = 20 * 1000;
    }

    @Test
    public void testDouglasPeuckerForFindel() throws ParseException {
        Competitor competitor = getCompetitorByName("Findel");
        assertNotNull(competitor);
        Date fromDate = dateFormat.parse("06/23/1022-16:28:25");
        Date toDate = dateFormat.parse("06/23/2011-15:51:05");
        assertNotNull(fromDate);
        assertNotNull(toDate);
        List<Maneuver> maneuvers = getTrackedRace().getManeuvers(competitor, new MillisecondsTimePoint(fromDate),
                new MillisecondsTimePoint(toDate));

        assertManeuversCounts(maneuvers);
        assertTimeOfManeuvers(maneuvers);
    }

    private void assertTimeOfManeuvers(List<Maneuver> tackManeuvers) throws ParseException {
        List<TimePoint> timePointOfTackManeuversList = new ArrayList<TimePoint>();
        // TODO add TimePoints of manouvers - only tacks and jibes
        // Tacks
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:28:30")));
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:38:01")));
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:40:30")));
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:40:57")));
        // Jibes
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:46:13")));
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:49:21")));
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:51:00")));
        // PENALTY CIRCLE
        timePointOfTackManeuversList.add(new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:43:45")));
        
        
        // assert that there are enought estimated TimePoints
        assertEquals(timePointOfTackManeuversList.size(), tackManeuvers.size());
        
        for (int i = 0; i < tackManeuvers.size(); i++) {
            Maneuver maneuver = tackManeuvers.get(i);
            TimePoint timepoint = timePointOfTackManeuversList.get(i);
            Type type = maneuver.getType();
            switch (type) {
            case HEAD_UP:
                break;
            case BEAR_AWAY:
                break;
            case TACK:
                assertEquals(maneuver.getTimePoint().asMillis(), timepoint.asMillis(), tackDelta);
                break;
            case JIBE:
                assertEquals(maneuver.getTimePoint().asMillis(), timepoint.asMillis(), jibeDelta);
                break;
            case PENALTY_CIRCLE:
                assertEquals(maneuver.getTimePoint().asMillis(), timepoint.asMillis(), penaltyCircleDelta);
                break;
            }
            
        }
    }

    private void assertManeuversCounts(List<Maneuver> maneuverList) {
        // TODO count headup and bear away
        //int maneuverHeadUpCount = 0;
        //int maneuverBearAwayCount = 0;
        int maneuverTackCount = 0;
        int maneuverJibecount = 0;
        int maneuverPenaltyCircle = 0;
        for (Maneuver maneuver : maneuverList) {
            Type type = maneuver.getType();
            switch (type) {
            case HEAD_UP:
                //maneuverHeadUpCount++;
                break;
            case BEAR_AWAY:
                //maneuverBearAwayCount++;
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
