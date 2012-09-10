package com.sap.sailing.simulator.test;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.impl.PolarDiagram49STG;

public class PolarDiagramPlotDataPolar {

    @Test
    public void testPolarDiagram_PlotDataPolar() throws IOException {

        PolarDiagram polarDiagram = new PolarDiagram49STG();

        Set<Speed> extraSpeeds = new TreeSet<Speed>();
        extraSpeeds.add(new KnotSpeedImpl(2.0));
        extraSpeeds.add(new KnotSpeedImpl(4.0));

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> table = polarDiagram.polarDiagramPlot(1.0, extraSpeeds);

        String outputFile = "C:\\temp\\pd-test-49orc.csv";
        FileWriter fw = new FileWriter(outputFile);

        Set<Speed> validSpeeds = table.keySet();
        validSpeeds.remove(Speed.NULL);

        String xLine = "X";
        for (Speed s : validSpeeds) {
            xLine += ";" + s.getKnots();
        }
        fw.write(xLine + "\n");

        for (Bearing b : table.get(validSpeeds.iterator().next()).keySet()) {
            xLine = "";
            xLine += "" + b.getDegrees();
            for (Speed s : validSpeeds) {
                xLine += ";" + table.get(s).get(b).getKnots();
            }
            fw.write(xLine + "\n");
        }

        fw.close();
        assertEquals("no test", 1, 1);

    }

}
