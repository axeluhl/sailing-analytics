package com.sap.sailing.simulator.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.impl.RectangularBoundary;

public class RectangularBoundaryTest {

	@Test 
    public void testRectangularBoundary1() {
    	Position p1 = new DegreePosition(25.661333, -90.752563);
        Position p2 = new DegreePosition(24.522137, -90.774536);

        Boundary b = new RectangularBoundary(p1, p2, 0.1);
        Position[][] grid = b.extractGrid(20,20,0,0);
        assertEquals("Number of lattice points",400,grid.length*grid[0].length);
    	
    }

}
