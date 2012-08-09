package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.impl.PolarDiagramCSV;

public class PolarDiagramCSVTest {

	@Test
	public void test() throws IOException {
		PolarDiagram pd = new PolarDiagramCSV("C:\\Users\\i059829\\Desktop\\test.txt");
		
		assertEquals("No test",1,1);
		
	}

}
