package com.sap.sailing.domain.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class StoredTrackedBasedTestWithTrackedRace extends StoredTrackBasedTest {
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

    protected DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

    protected void setTrackedRace(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    protected Competitor getCompetitorByName(String name) {
        for (Competitor c : getTrackedRace().getRace().getCompetitors()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

}
