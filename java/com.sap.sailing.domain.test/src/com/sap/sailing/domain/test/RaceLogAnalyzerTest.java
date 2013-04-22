package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagFinder;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;

public class RaceLogAnalyzerTest {

    @Test
    public void testLastFlagFinder() {
        RaceLog raceLog = new RaceLogImpl("RaceLogTest");
        TimePoint t1 = MillisecondsTimePoint.now();
        int passId = 0;
        boolean isDisplayed = true;
        RaceLogFlagEvent rcEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.XRAY, Flags.NONE,
                isDisplayed);
        raceLog.add(rcEvent);
        rcEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(t1.plus(1), passId, Flags.PAPA, Flags.NONE,
                isDisplayed);
        raceLog.add(rcEvent);

        raceLog.lockForRead();
        try {
            LastFlagFinder lff = new LastFlagFinder(raceLog);
            assertEquals(rcEvent, lff.getLastFlagEvent());

        } finally {
            raceLog.unlockAfterRead();
        }
    }

}
