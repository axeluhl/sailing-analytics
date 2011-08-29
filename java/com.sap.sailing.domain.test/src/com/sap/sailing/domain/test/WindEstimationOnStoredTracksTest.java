package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.util.Util;

public class WindEstimationOnStoredTracksTest extends StoredTrackBasedTest {
    private DynamicTrackedRace trackedRace;
    
    @Before
    public void setUp() throws FileNotFoundException, IOException {
        Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks = loadTracks();
        trackedRace = createTestTrackedRace("Kieler Woche", "505 Race 2", "505", tracks.keySet());
        copyTracks(tracks, trackedRace);
    }
    
    @Test
    public void testSuccessfulTrackedRaceCreation() throws FileNotFoundException, IOException {
        assertNotNull(trackedRace);
        assertEquals(Util.size(trackedRace.getRace().getCompetitors()), loadTracks().size());
    }

    @Test
    public void testSimpleWindEstimation() throws NoWindException {
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        GPSFixTrack<Competitor, GPSFixMoving> hassosTrack = trackedRace.getTrack(hasso);
        TimePoint start = hassosTrack.getFirstRawFix().getTimePoint();
        TimePoint stop = hassosTrack.getLastRawFix().getTimePoint();
        TimePoint middle = new MillisecondsTimePoint(start.asMillis() + (stop.asMillis()-start.asMillis())*3/4);
        Wind estimatedWindDirection = trackedRace.getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
    }

    private Competitor getCompetitorByName(String name) {
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }
}
