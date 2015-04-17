package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LineAnalysisTest extends TrackBasedTest {
    private MillisecondsTimePoint now;
    private DynamicTrackedRace trackedRace;

    @Before
    public void setUp() {
        final List<Competitor> emptyCompetitorList = Collections.emptyList();
        now = MillisecondsTimePoint.now();
        trackedRace = createTestTrackedRace("Test Regatta", "Test Race", "505", emptyCompetitorList, now);
    }
    
    @Test
    public void testLinesAnalysisWithNoAdvantage() {
        final LineDetails startLine = trackedRace.getStartLine(now);
        assertEquals(0, startLine.getAdvantage().getMeters(), 0.001);
        final LineDetails finishLine = trackedRace.getFinishLine(now);
        assertEquals(0, finishLine.getAdvantage().getMeters(), 0.001);
    }

    @Test
    public void testStartLineAnalysisWithAdvantageOnStarboard() {
        Mark right = getMark("Right lee gate buoy");
        final DynamicGPSFixTrack<Mark, GPSFix> rightMarkTrack = trackedRace.getOrCreateTrack(right);
        Position rightMarkAtNow = rightMarkTrack.getFirstRawFix().getPosition();
        TimePoint aBitAfterNow = now.plus(1000);
        final MeterDistance advantage = new MeterDistance(10);
        rightMarkTrack.addGPSFix(new GPSFixImpl(rightMarkAtNow.translateGreatCircle(new DegreeBearingImpl(0), advantage), aBitAfterNow));
        final LineDetails startLine = trackedRace.getStartLine(aBitAfterNow);
        assertEquals(advantage.getMeters(), startLine.getAdvantage().getMeters(), 0.1);
        assertEquals(NauticalSide.STARBOARD, startLine.getAdvantageousSideWhileApproachingLine());
        final LineDetails finishLine = trackedRace.getFinishLine(aBitAfterNow);
        assertEquals(advantage.getMeters(), finishLine.getAdvantage().getMeters(), 0.1);
        assertEquals(NauticalSide.PORT, finishLine.getAdvantageousSideWhileApproachingLine());
    }

    private Mark getMark(String name) {
        for (Mark mark : trackedRace.getMarks()) {
            if (mark.getName().equals(name)) {
                return mark;
            }
        }
        return null;
    }
}
