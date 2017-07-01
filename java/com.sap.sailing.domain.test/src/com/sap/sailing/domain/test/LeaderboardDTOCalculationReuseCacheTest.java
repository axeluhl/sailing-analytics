package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardDTOCalculationReuseCacheTest {
    @Test
    public void testNullWindCaching() {
        final TimePoint now = MillisecondsTimePoint.now();
        final LeaderboardDTOCalculationReuseCache cache = new LeaderboardDTOCalculationReuseCache(now);
        final Competitor competitor = mock(Competitor.class);
        @SuppressWarnings("unchecked")
        final GPSFixTrack<Competitor, GPSFixMoving> track = mock(GPSFixTrack.class);
        final Position p = new DegreePosition(123, 12);
        when(track.getEstimatedPosition(now, false)).thenReturn(p);
        final TrackedRace trackedRace = mock(TrackedRace.class);
        when(trackedRace.getTrack(competitor)).thenReturn(track);
        when(trackedRace.getWind(p, now)).thenReturn(null);
        assertNull(cache.getWind(trackedRace, competitor, now));
    }
}
