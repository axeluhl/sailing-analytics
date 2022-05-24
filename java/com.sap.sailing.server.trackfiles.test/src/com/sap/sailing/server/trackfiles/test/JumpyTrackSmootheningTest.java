package com.sap.sailing.server.trackfiles.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;
import com.sap.sse.common.Util;

/**
 * See also bug 5728. We have seen tracks coming from phones where there seem to be two
 * "interleaved" sub-sequences of fixes: one where the time stamps are rounded to a full
 * second; and one where time stamps have a fractional seconds value. Each of these
 * sub-sequences seems to be consistent in itself, but at their boundaries the track
 * appears jittery and jumpy.<p>
 * 
 * We surmise that a constant offset can be computed by which the full-second time stamps
 * would need to be adjusted in order to result in a consistent track that is smooth also
 * at the boundaries between the two sub-sequences.<p>
 * 
 * This test starts with looking at two tracks that are known to show this issue. Jumpiness
 * can be measured by looking at the number and badness of inconsistencies between computed
 * and reported COG/SOG values between fixes, then "learning" the offset, adjusting all
 * integer-second fixes by the offset and computing number and badness of inconsistencies
 * again.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class JumpyTrackSmootheningTest {
    private DynamicGPSFixTrack<Competitor, GPSFixMoving> track;
    
    protected void readTrack(String filename) throws Exception {
        track = new DynamicGPSFixMovingTrackImpl<Competitor>(AbstractLeaderboardTest.createCompetitor(filename),
                /* millisecondsOverWhichToAverage */ 5000, /* losslessCompaction */ true);
        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(filename);
        final InputStream inputStream;
        if (filename.endsWith(".gz")) {
            inputStream = new GZIPInputStream(fileInputStream);
        } else {
            inputStream = fileInputStream;
        }
        RouteConverterGPSFixImporterFactory.INSTANCE.createRouteConverterGPSFixImporter().importFixes(inputStream,
                (fix, device)->track.add((GPSFixMoving) fix), /* inferSpeedAndBearing */ false, filename);
    }
    
    /**
     * On {@link #track} looks at adjacent fixes and compares the COG/SOG values reported by those fixes
     * with the COG/SOG value inferred from their position and time delta.
     */
    private int getInconsistenciesOnRawFixes() {
        int numberOfInconsistencies = 0;
        GPSFixMoving previous = null;
        track.lockForRead();
        try {
            for (final GPSFixMoving fix : track.getRawFixes()) {
                if (previous == null) {
                    previous = fix;
                } else {
                    final SpeedWithBearing inferred = previous.getSpeedAndBearingRequiredToReach(fix);
                    final SpeedWithBearing reportedByPrevious = previous.getSpeed();
                    final SpeedWithBearing reportedByFix = fix.getSpeed();
                    if (tooDifferent(inferred, reportedByPrevious) || tooDifferent(inferred, reportedByFix)) {
                        numberOfInconsistencies++;
                    }
                }
            }
        } finally {
            track.unlockAfterRead();
        }
        return numberOfInconsistencies;
    }
    
    private boolean tooDifferent(SpeedWithBearing inferred, SpeedWithBearing reported) {
        return
                // speed difference must be less than 20% of 
                (Math.abs((inferred.getKnots()-reported.getKnots()) / inferred.getKnots()) > 0.2 ||
                // course difference must be less than 10deg
                Math.abs(inferred.getBearing().getDifferenceTo(reported.getBearing()).getDegrees()) > 10);
    }

    @Test
    public void testCZE2471() throws Exception {
        readTrack("CZE2471.gpx.gz");
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final int numberOfInconsistencies = getInconsistenciesOnRawFixes();
        assertTrue(numberOfInconsistencies > 0);
    }
    
    @Test
    public void testCZE2956() throws Exception {
        readTrack("CZE2956.gpx.gz");
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final int numberOfInconsistencies = getInconsistenciesOnRawFixes();
        assertTrue(numberOfInconsistencies > 0);
    }
    
    @Test
    public void testGallagherZelenka() throws Exception {
        readTrack("GallagherZelenka.gpx.gz");
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final int numberOfInconsistencies = getInconsistenciesOnRawFixes();
        assertTrue(numberOfInconsistencies > 0);
    }
}
