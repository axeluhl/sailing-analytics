package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;

public class TimePointTest {
	@Test
	public void compare() {
		TimePoint one = new MillisecondsTimePoint(Long.MIN_VALUE);
		TimePoint two = new MillisecondsTimePoint(Long.MAX_VALUE);
		
		assertTrue(one.before(two));
	}
}
