package com.sap.sailing.simulator.test;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.impl.PolarDiagram49;

public class PolarDiagram49Test {

    @Test
    public void testPolarDiagram49_1() throws IOException {
    	
    	// read PolarDiagram from csv file
    	/*
    	String inputFile = "C:\\Users\\i059829\\Desktop\\new.csv";
    	FileReader fr = new FileReader(inputFile);
    	BufferedReader bfr = new BufferedReader(fr);
    	
    	List<Speed> velocity = new ArrayList<Speed>();
    	List<Bearing> beatAngles = new ArrayList<Bearing>();
    	List<Speed> beatVMG = new ArrayList<Speed>();
    	Map<Bearing, List<Speed>> speeds = new HashMap<Bearing, List<Speed>>();
    	List<Speed> runVMG = new ArrayList<Speed>();
    	List<Bearing> gybeAngles = new ArrayList<Bearing>();
    	
    	String line = "";
    	while (line != null) {
    		line = bfr.readLine();
    		String[] elements = line.split(",");
    		elements[0] = elements[0].replace(" ", "");
    		elements[0] = elements[0].toLowerCase();
    		switch (elements[0]) {
    		case "windvelocity" : 
    			for (int i = 1; i < elements.length; i++)
    				velocity.add(new KnotSpeedImpl(new Double(elements[i])));
    			break;
    		case "beatangles" :
    			for (int i = 1; i < elements.length; i++)
    				beatAngles.add(new DegreeBearingImpl(new Double(elements[i])));
    		break;
    		case "beatvmg" :
    			for (int i = 1; i < elements.length; i++)
    				beatVMG.add(new KnotSpeedImpl(new Double(elements[i])));
    			break;
    		case "runvmg" :
    			for (int i = 1; i < elements.length; i++)
    				runVMG.add(new KnotSpeedImpl(new Double(elements[i])));
    			break;
    		case "gybeangles" :
    			for (int i = 1; i < elements.length; i++)
    				gybeAngles.add(new DegreeBearingImpl(new Double(elements[i])));
    			break;
    		default:
    			List<Speed> sp = new ArrayList<Speed>();
    			
    			for (int i = 1; i< elements.length; i++)
    				sp.add(new KnotSpeedImpl(new Double(elements[i])));
    			speeds.put(new DegreeBearingImpl(new Double(elements[0])), sp);
    			break;
    			
    		}
    	}
        NavigableMap<Speed, NavigableMap<Bearing, Speed>> mapSpeedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Speed, Bearing> mapBeatAngles = new TreeMap<Speed, Bearing>();
        NavigableMap<Speed, Bearing> mapGybeAngles = new TreeMap<Speed, Bearing>();
        NavigableMap<Speed, Speed> mapBeatSOG = new TreeMap<Speed, Speed>();
        NavigableMap<Speed, Speed> mapGybeSOG = new TreeMap<Speed, Speed>();
    	
        for (int i = 0; i < velocity.size(); i++ ) {
        	NavigableMap<Bearing, Speed> speedTableLine = new TreeMap<Bearing, Speed>();
        	for (Entry<Bearing, List<Speed>> e : speeds.entrySet()) {
        		speedTableLine.put(e.getKey(), e.getValue().get(i));
        	}
        	mapSpeedTable.put(velocity.get(i), speedTableLine);
        	mapBeatAngles.put(velocity.get(i), beatAngles.get(i));
        	mapGybeAngles.put(velocity.get(i), gybeAngles.get(i));
        	mapBeatSOG.put(velocity.get(i), beatVMG.get(i));
        	mapGybeSOG.put(velocity.get(i), runVMG.get(i));
        }
        
    	PolarDiagram polarDiagramFromFile = new PolarDiagram49(mapSpeedTable,mapBeatAngles,mapGybeAngles,mapBeatSOG,mapGybeSOG);
    	*/
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
