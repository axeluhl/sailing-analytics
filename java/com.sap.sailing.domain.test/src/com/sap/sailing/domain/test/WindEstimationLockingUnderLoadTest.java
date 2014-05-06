package com.sap.sailing.domain.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.TrackBasedEstimationWindTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;

/**
 * See the issues documented in bug 1923 (http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1923).
 * This test mocks {@link TrackedRace} to the degree necessary and fires more or less random "measured"
 * wind fixes against the {@link TrackBasedEstimationWindTrackImpl} while concurrently reading massively
 * from it. With this, the test is supposed to reproduce conditions as observed during events, leading
 * up to bug 1923. This can then be the basis for a fix.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class WindEstimationLockingUnderLoadTest {
    private static final int MIN_WIND_SPEED_IN_KNOTS = 5;

    private static final int MAX_WIND_SPEED_IN_KNOTS = 20;

    private TrackedRace mockedTrackedRace;
    
    private TrackBasedEstimationWindTrackImpl estimationTrack;
    
    private WindTrackImpl measuredTrack;
    
    @Before
    public void setUp() {
        mockedTrackedRace = mockTrackedRace();
        estimationTrack = new TrackBasedEstimationWindTrackImpl(mockedTrackedRace, WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, 0.5);
        measuredTrack = new WindTrackImpl(WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, /* useSpeed */ true, /* nameForReadWriteLock */ "Test wind track in "+getClass().getName());
    }

    private TrackedRace mockTrackedRace() {
        TrackedRace result = mock(TrackedRace.class);
        RaceDefinition mockedRaceDefinition = mock(RaceDefinition.class);
        when(result.getRace()).thenReturn(mockedRaceDefinition);
        when(mockedRaceDefinition.getName()).thenReturn("Test Race");
        when(result.getEstimatedWindDirectionWithConfidence((Position) any(), (TimePoint) any())).thenAnswer(new Answer() {
                 public Object answer(InvocationOnMock invocation) {
                     return randomWindOrNull();
//                         Object[] args = invocation.getArguments();
//                         Object mock = invocation.getMock();
//                         return "called with arguments: " + args;
                     }
        });
        return result;
    }
    
    private WindWithConfidence<TimePoint> randomWindOrNull() {
        final WindWithConfidence<TimePoint> result;
        double speedInKnots = MAX_WIND_SPEED_IN_KNOTS*Math.random();
        if (speedInKnots < MIN_WIND_SPEED_IN_KNOTS) {
            result = null;
        } else {
            double directionInDegrees = 360*Math.random();
            double confidence = Math.random();
            final MillisecondsTimePoint now = MillisecondsTimePoint.now();
            result = new WindWithConfidenceImpl<TimePoint>(new WindImpl(new DegreePosition(49, 8), now, new KnotSpeedWithBearingImpl(speedInKnots, new DegreeBearingImpl(directionInDegrees))),
                    confidence, now, /* useSpeed */ true);
        }
        return result;
    }

    @Test
    public void testSimpleWindEstimation() {
        // We can expect MIN_WIND_SPEED_IN_KNOTS/MAX_WIND_SPEED_IN_KNOTS results to be null, but as there is a significant
        // difference between the two, with a sufficient amount of random samples there has to be at least one non-null
        // result
        int notNullCount = 0;
        for (int i=0; i<1000; i++) {
            Wind estimatedWind = estimationTrack.getAveragedWind(/* position */ null, MillisecondsTimePoint.now().minus(10000*i));
            if (estimatedWind != null) {
                notNullCount++;
            }
        }
        assertTrue(notNullCount > 0);
    }
}
