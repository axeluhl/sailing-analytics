package com.sap.sailing.simulator.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.sap.sailing.simulator.impl.SailingSimulatorImpl;

public class SimulatorTest {
	@Test
	public void myFirstTest() {
		System.out.println("Hello world");
		fail("Intentional failure");
		SailingSimulatorImpl s = new SailingSimulatorImpl();
		s.m();
	}
}
