package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class WindTest {
    @Test
    public void testSingleElementWindTrack() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(0));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }
}
