package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public class DouglasPeuckerManeuverTest extends StoredTrackBasedTest {

    private final Map<Competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving>> tracks;

    public DouglasPeuckerManeuverTest() {
        tracks = new HashMap<Competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving>>();
    }

    @Before
    public void setUpTrackData() throws FileNotFoundException, IOException {
        tracks.putAll(loadTracks());
    }

    private DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrackByCompetitorName(String name) {
        for (Map.Entry<Competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving>> e : tracks.entrySet()) {
            if (e.getKey().getName().equals(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Test
    public void checkLoadedTracksCount() throws InterruptedException, FileNotFoundException, IOException {
        assertEquals(36, tracks.size());
    }

    @Test
    public void testFindelTrack(){
        // get the track of Findel
        DynamicGPSFixTrack<Competitor,GPSFixMoving> trackByCompetitorName = getTrackByCompetitorName("Findel");
        assertNotNull(trackByCompetitorName);
        TrackedRace trackedRace = new MockedTrackedRace();
    }
}
