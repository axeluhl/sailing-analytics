package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Wind;

public class WindEstimationOnStoredTracksTest extends StoredTrackBasedTestWithTrackedRace {
    // ---------------- BearingWithConfidenceCluster -----------------
    
    @Test
    public void testSuccessfulTrackedRaceCreation() throws FileNotFoundException, IOException {
        assertNotNull(getTrackedRace());
        assertEquals(Util.size(getTrackedRace().getRace().getCompetitors()), loadTracks().size());
    }

    @Test
    public void testSimpleWindEstimation() throws NoWindException, FileNotFoundException, IOException {
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        GPSFixTrack<Competitor, GPSFixMoving> hassosTrack = getTrackedRace().getTrack(hasso);
        TimePoint start = hassosTrack.getFirstRawFix().getTimePoint();
        TimePoint stop = hassosTrack.getLastRawFix().getTimePoint();
        TimePoint middle = new MillisecondsTimePoint(start.asMillis() + (stop.asMillis()-start.asMillis())*3/4);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
    }
    
}
