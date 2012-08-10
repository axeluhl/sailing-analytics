package com.sap.sailing.simulator.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.impl.PolarDiagramCSV;

public class PolarDiagramCSVTest {

    @SuppressWarnings("unused")
    @Test
    public void test() throws IOException {
        PolarDiagram pd = new PolarDiagramCSV("resources/PolarDiagram49.csv");

        assertEquals("No test", 1, 1);
    }

}
