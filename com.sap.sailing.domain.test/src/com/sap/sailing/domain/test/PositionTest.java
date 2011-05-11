package com.sap.sailing.domain.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.impl.DegreePosition;

public class PositionTest {
	@Test
	public void distanceTest() {
		Position p1 = new DegreePosition(49.2, 008.3);
		Position p2 = new DegreePosition(49.3, 008.2);
		assertTrue(p1.distanceInSeaMiles(p2) < 10);
		Position northPole = new DegreePosition(90, 0);
		Position southPole = new DegreePosition(-90, 0);
		assertTrue(Math.abs(southPole.distanceInSeaMiles(northPole) * 1.852 - 20000) < 2);
	}
}
