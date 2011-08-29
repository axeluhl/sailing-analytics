package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.util.Util;

public class WindEstimationOnStoredTracksTest extends StoredTrackBasedTest {
    private DynamicTrackedRace trackedRace;
    
    @Before
    public void setUp() throws FileNotFoundException, IOException {
        Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks = loadTracks();
        trackedRace = createTestTrackedRace("Kieler Woche", "505 Race 2", "505", tracks.keySet());
        copyTracks(tracks);
    }
    
    private void copyTracks(Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks) {
        for (Map.Entry<Competitor, DynamicTrack<Competitor, GPSFixMoving>> e : tracks.entrySet()) {
            DynamicTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(e.getKey());
            for (GPSFixMoving fix : e.getValue().getRawFixes()) {
                track.addGPSFix(fix);
            }
            List<MarkPassing> markPassings = new ArrayList<MarkPassing>();
            // add a mark passing for the start gate at the very beginning to make sure everyone is on a valid leg
            markPassings.add(new MarkPassingImpl(track.getFirstRawFix().getTimePoint(), trackedRace.getRace()
                    .getCourse().getWaypoints().iterator().next(), e.getKey()));
            trackedRace.updateMarkPassings(e.getKey(), markPassings);
        }
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
