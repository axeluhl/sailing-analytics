package com.sap.sailing.domain.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Tack;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Maneuver.Type;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class ManeuverDetectionOnMdM2011SemifinalTest extends OnlineTracTracBasedTest {

    public ManeuverDetectionOnMdM2011SemifinalTest() throws MalformedURLException, URISyntaxException {
    }
    
    @Override
    protected String getExpectedEventName() {
        return "Sailing Team Germany";
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        super.setUp();
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c", new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> buoyPositions = new HashMap<String, Position>();
        buoyPositions.put("CR Start (left)", new DegreePosition(53.562944999999985, 10.010104000000046));
        buoyPositions.put("CR Start (right)", new DegreePosition(53.562944999999985, 10.010104000000046));
        buoyPositions.put("Leeward mark", new DegreePosition(53.562145000000015, 10.009252));
        buoyPositions.put("Luvtonne", new DegreePosition(53.560581899999995, 10.005657));
        for (Waypoint w : getTrackedRace().getRace().getCourse().getWaypoints()) {
            for (Buoy buoy : w.getBuoys()) {
                getTrackedRace().getOrCreateTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), epoch));
                getTrackedRace().getOrCreateTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), now));
            }
        }
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(51))), WindSource.WEB);
    }
    
    @Test
    public void testManeuversForDennis() throws NoWindException {
        Competitor dennis = getCompetitorByName("Gehrlein");
        NavigableSet<MarkPassing> dennisMarkPassings = getTrackedRace().getMarkPassings(dennis);
        List<Maneuver> maneuvers = getTrackedRace().getManeuvers(dennis, dennisMarkPassings.first().getTimePoint(),
                dennisMarkPassings.last().getTimePoint());
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"));
        c.set(2011, 10-1, 30, 13, 32, 42);
        assertManeuver(maneuvers, Maneuver.Type.TACK, Tack.PORT, new MillisecondsTimePoint(c.getTime()), /* tolerance in milliseconds */ 3000);
        c.set(2011, 10-1, 30, 13, 34, 00);
        assertManeuver(maneuvers, Maneuver.Type.TACK, Tack.STARBOARD, new MillisecondsTimePoint(c.getTime()), /* tolerance in milliseconds */ 3000);
        c.set(2011, 10-1, 30, 13, 35, 46);
        assertManeuver(maneuvers, Maneuver.Type.JIBE, Tack.STARBOARD, new MillisecondsTimePoint(c.getTime()), /* tolerance in milliseconds */ 3000);
        c.set(2011, 10-1, 30, 13, 36, 49);
        assertManeuver(maneuvers, Maneuver.Type.JIBE, Tack.PORT, new MillisecondsTimePoint(c.getTime()), /* tolerance in milliseconds */ 3000);
    }

    private void assertManeuver(List<Maneuver> maneuvers, Type type, Tack newTack,
            MillisecondsTimePoint timePoint, int toleranceInMilliseconds) {
        for (Maneuver maneuver : maneuvers) {
            if (maneuver.getType() == type && (newTack == null || newTack == maneuver.getNewTack()) &&
                    Math.abs(maneuver.getTimePoint().asMillis() - timePoint.asMillis()) <= toleranceInMilliseconds) {
                return;
            }
        }
        fail("Didn't find maneuver type " + type + (newTack == null ? "" : " to new tack " + newTack) + " in "
                + toleranceInMilliseconds + "ms around " + timePoint);
    }

}
