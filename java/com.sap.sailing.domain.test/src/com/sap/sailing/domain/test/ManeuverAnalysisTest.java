package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Maneuver.Type;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class ManeuverAnalysisTest extends KielWeek2011BasedTest {

    private SimpleDateFormat dateFormat;
    private static final int TACK_TOLERANCE = 7000;
    private static final int JIBE_TOLERANCE = 7000;
    private static final int PENALTYCIRCLE_TOLERANCE = 9000;

    private List<Maneuver> maneuversInvalid;

    public ManeuverAnalysisTest() throws URISyntaxException, IOException, InterruptedException {
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
    }

    /**
     * Test for 505 Race 2 for competitor "Findel"
     */
    @Test
    public void testDouglasPeuckerForFindel() throws ParseException, NoWindException {
        Competitor competitor = getCompetitorByName("Findel");
        assertNotNull(competitor);
        Date fromDate = dateFormat.parse("06/23/2011-15:28:20");
        Date toDate = dateFormat.parse("06/23/2011-16:38:01");
        assertNotNull(fromDate);
        assertNotNull(toDate);
        List<Maneuver> maneuvers = getTrackedRace().getManeuvers(competitor, new MillisecondsTimePoint(fromDate),
                new MillisecondsTimePoint(toDate));
        maneuversInvalid = new ArrayList<Maneuver>(maneuvers);

        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:30")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:38:01")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:40:28")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:40:52")), TACK_TOLERANCE);

        assertManeuver(maneuvers, Maneuver.Type.JIBE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:46:13")), JIBE_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.JIBE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:49:06")), JIBE_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.JIBE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:50:41")), JIBE_TOLERANCE);

        assertManeuver(maneuvers, Maneuver.Type.PENALTY_CIRCLE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:53:45")), PENALTYCIRCLE_TOLERANCE);

        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:54:01")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:58:27")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:03:19")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:04:41")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:05:25")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:05:43")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:06:16")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:07:33")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:11:27")), TACK_TOLERANCE);

        assertManeuver(maneuvers, Maneuver.Type.JIBE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:13:28")), JIBE_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.JIBE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:18:37")), JIBE_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.JIBE,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:21:28")), JIBE_TOLERANCE);

        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:26:14")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:28:21")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:31:29")), TACK_TOLERANCE);
        assertManeuver(maneuvers, Maneuver.Type.TACK,
                new MillisecondsTimePoint(dateFormat.parse("06/23/2011-16:38:00")), TACK_TOLERANCE);

        List<Maneuver.Type> maneuverTypesFound = new ArrayList<Maneuver.Type>();
        maneuverTypesFound.add(Maneuver.Type.TACK);
        maneuverTypesFound.add(Maneuver.Type.JIBE);
        maneuverTypesFound.add(Maneuver.Type.PENALTY_CIRCLE);
        assertAllManeuversOfTypesDetected(maneuverTypesFound, maneuversInvalid);
    }

    /**
     * Checks that a maneuver of the type <code>maneuverType</code> to the time of <code>maneuverTimePoint</code> does
     * exist.
     * 
     * @param maneuverList
     *            The whole list of maneuvers to search for that maneuver type.
     * @param maneuverType
     *            The type of maneuver that should have happened to the given time point.
     * @param maneuverTimePoint
     *            The time point the maneuver type should have happened.
     * @param tolerance
     *            The tolerance of time, the maneuver should have happened in milliseconds.
     */
    private void assertManeuver(List<Maneuver> maneuverList, Maneuver.Type maneuverType,
            MillisecondsTimePoint maneuverTimePoint, int tolerance) {
        for (Maneuver maneuver : maneuverList) {
            assertNotNull(maneuver.getTimePoint());
            if (maneuver.getType() == maneuverType
                    && Math.abs(maneuver.getTimePoint().asMillis() - maneuverTimePoint.asMillis()) <= tolerance) {
                maneuversInvalid.remove(maneuver);
                return;
            }
        }
        fail("Didn't find maneuver type " + maneuverType + " in " + tolerance + "ms around " + maneuverTimePoint);
    }

    /**
     * Checks if there where additional maneuvers of the given types listed in <code>maneuverTypesFound</code> found,
     * that where not found by {@link ManeuverAnalysisTest#assertManeuver(List, Type, MillisecondsTimePoint, int)}.
     * 
     * @param maneuverTypesFound
     *            The maneuver types that should be found.
     * @param maneuversNotDetected
     *            The maneuvers of the types listed in <code>maneuverTypesFound</code> that where not detected by
     *            {@link ManeuverAnalysisTest#assertManeuver(List, Type, MillisecondsTimePoint, int)}
     */
    private void assertAllManeuversOfTypesDetected(List<Maneuver.Type> maneuverTypesFound,
            List<Maneuver> maneuversNotDetected) {
        for (Maneuver maneuver : maneuversNotDetected) {
            for (Maneuver.Type type : maneuverTypesFound) {
                if (maneuver.getType().equals(type)) {
                    fail("The maneuver detectionTest did not detect the maneuver " + maneuver.getType() + " around "
                            + maneuver.getTimePoint());
                }
            }
        }
    }

    private void printManeuvers(List<Maneuver> list) {
        List<Maneuver> tackManeuvers = new ArrayList<Maneuver>();
        List<Maneuver> jibeManeuvers = new ArrayList<Maneuver>();
        List<Maneuver> penaltyManeuvers = new ArrayList<Maneuver>();
        for (Maneuver maneuver : list) {
            if (maneuver.getType().equals(Maneuver.Type.TACK)) {
                tackManeuvers.add(maneuver);
            } else if (maneuver.getType().equals(Maneuver.Type.JIBE)) {
                jibeManeuvers.add(maneuver);
            } else if (maneuver.getType().equals(Maneuver.Type.PENALTY_CIRCLE)) {
                penaltyManeuvers.add(maneuver);
            }
        }
        System.out.println("\nTACKS:");
        for (Maneuver maneuver : tackManeuvers) {
            System.out.println(dateFormat.format(maneuver.getTimePoint().asDate()));
        }
        System.out.println("\nJIBES:");
        for (Maneuver maneuver : jibeManeuvers) {
            System.out.println(dateFormat.format(maneuver.getTimePoint().asDate()));
        }
        System.out.println("\nPENALTY CIRCLES:");
        for (Maneuver maneuver : penaltyManeuvers) {
            System.out.println(dateFormat.format(maneuver.getTimePoint().asDate()));
        }
    }
}
