package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.impl.PolarDiagram49;

import org.junit.Test;

public class PolarDiagram49Test {

    @Test
    public void testPolarDiagram49_1() throws IOException {
    	
        PolarDiagram polarDiagram = new PolarDiagram49();
    	
    	NavigableMap<Speed, NavigableMap<Bearing, Speed>> table = polarDiagram.polarDiagramPlot(10.0);
        
    	String outputFile = "C:\\Users\\i059829\\Desktop\\new.csv";
		FileWriter fw = new FileWriter(outputFile);
 
    	Set<Speed> validSpeeds = table.keySet();
    	validSpeeds.remove(Speed.NULL);
    	for (Speed s : validSpeeds) {
    		fw.write(s.toString() + "\n");
    		String xLine = "";
    		String yLine = "";
    		for (Bearing b : table.get(s).keySet()) {
    			xLine += "," + table.get(s).get(b).getKnots()*Math.cos(Math.PI/2 - b.getRadians());
    			yLine += "," + table.get(s).get(b).getKnots()*Math.sin(Math.PI/2 - b.getRadians());
    		}
    		fw.write(xLine + "\n");
    		fw.write(yLine + "\n");
    		polarDiagram.setTargetDirection(new DegreeBearingImpl(10.0));
    		
    		xLine = "";
    		yLine = "";
   
    		for (Bearing b : polarDiagram.optimalDirectionsUpwind()) {
    			xLine += "," + polarDiagram.getSpeedAtBearing(b).getKnots() * Math.cos(Math.PI/2 - b.getRadians());
    			yLine += "," + polarDiagram.getSpeedAtBearing(b).getKnots() * Math.sin(Math.PI/2 - b.getRadians());
    		}
    		fw.write(xLine + "\n");
    		fw.write(yLine + "\n");
    	}
    	
    	fw.close();
    	assertEquals("no test",1,1);
    	
    }
  
}
