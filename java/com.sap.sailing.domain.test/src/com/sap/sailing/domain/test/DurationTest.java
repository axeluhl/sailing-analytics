package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsDurationImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;

public class DurationTest {
    @Test
    public void testDurationPlus() {
        TimePoint t1 = new MillisecondsTimePoint(123);
        Duration d = new MillisecondsDurationImpl(234);
        TimePoint t2 = t1.plus(d);
        assertEquals(123+234, t2.asMillis());
    }

    @Test
    public void testDurationMinus() {
        TimePoint t1 = new MillisecondsTimePoint(234);
        Duration d = new MillisecondsDurationImpl(123);
        TimePoint t2 = t1.minus(d);
        assertEquals(234-123, t2.asMillis());
    }

    @Test
    public void testDurationUntil() {
        TimePoint t1 = new MillisecondsTimePoint(123);
        TimePoint t2 = new MillisecondsTimePoint(234);
        Duration d = t1.until(t2);
        assertEquals(234-123, d.asMillis());
    }
}
