package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class LongRangeRegattaGeocoderTest extends AbstractManeuverDetectionTestCase {

    public LongRangeRegattaGeocoderTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, InterruptedException {
        super.setUp();
        super.setUp("event_20110609_KielerWoch",
        /* raceId */"357c700a-9d9a-11e0-85be-406186cbf87c", new ReceiverType[] { ReceiverType.MARKPASSINGS,
                ReceiverType.RACESTARTFINISH, ReceiverType.RACECOURSE });
        fixApproximateMarkPositionsForGeocoder(getTrackedRace());
        dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
    }
    
    private void fixApproximateMarkPositionsForGeocoder(DynamicTrackedRace race) {
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> buoyPositions = new HashMap<String, Position>();
        buoyPositions.put("K Start (left)", new DegreePosition(5.90906782829855, -55.16166687011719));
        buoyPositions.put("K Start (right)", new DegreePosition(5.939801840526332, -55.17402648925781));
        buoyPositions.put("K Mark1", new DegreePosition(8.233237111274565, -31.640625));
        buoyPositions.put("K Mark4 (right)", new DegreePosition(13.63083009512624, -21.8243408203125));
        buoyPositions.put("K Mark4 (left)", new DegreePosition(13.870080100685891, -21.9122314453125));
        buoyPositions.put("K Finish (left)", new DegreePosition(14.694198629294522, -17.39307403564453));
        buoyPositions.put("K Finish (right)", new DegreePosition(14.693866535193942, -17.371788024902344));
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            for (Buoy buoy : w.getBuoys()) {
                race.getOrCreateTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), epoch));
                race.getOrCreateTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), now));
            }
        }
    }

    @Test
    public void testSetupOK() throws ParseException, NoWindException {
        assertNotNull(getTrackedRace());
        Pair<Placemark, Placemark> placemarks = getTrackedRace().getStartFinishPlacemarks();
        assertNotNull(placemarks.getA());
        assertNotNull(placemarks.getB());
    }
}
