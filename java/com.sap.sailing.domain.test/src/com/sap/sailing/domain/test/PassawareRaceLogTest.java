package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.impl.PassAwareRaceLogImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;

public class PassawareRaceLogTest {

    @Test
    public void testAddingTwoEventsOfDifferentPassesButSameTimestamps() {
        TimePoint t1 = MillisecondsTimePoint.now();
        int passId = 0;
        RaceLog raceLog = new RaceLogImpl("RaceLogTest");
        
        RaceLogFlagEvent rcEvent1 = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, UUID.randomUUID(), new ArrayList<Competitor>(), passId, Flags.CLASS, Flags.NONE, false);
        RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, UUID.randomUUID(), new ArrayList<Competitor>(), ++passId, Flags.CLASS, Flags.NONE, false);
        
        RaceLog passAwareRaceLog = new PassAwareRaceLogImpl(raceLog);
        
        passAwareRaceLog.add(rcEvent1);
        passAwareRaceLog.add(rcEvent2);
        
        passAwareRaceLog.lockForRead();
        try {
            assertEquals(2, Util.size(passAwareRaceLog.getRawFixes()));
            assertEquals(1, Util.size(passAwareRaceLog.getFixes()));
        } finally {
            passAwareRaceLog.unlockAfterRead();
        }
    }

}
