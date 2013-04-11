package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class RaceLogEventTest {

    @Test
    public void testEqualityOfTwoNotSameEvents() {
        TimePoint t1 = MillisecondsTimePoint.now();
        int passId = 0;
        UUID randomUUID = UUID.randomUUID();
        RaceLogFlagEvent rcEvent1 = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, randomUUID, new ArrayList<Competitor>(), passId, Flags.CLASS, Flags.NONE, false);
        RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, randomUUID, new ArrayList<Competitor>(), passId, Flags.CLASS, Flags.NONE, false);
        assertNotSame(rcEvent1, rcEvent2);
        assertEquals(rcEvent1, rcEvent2);
    }

}
