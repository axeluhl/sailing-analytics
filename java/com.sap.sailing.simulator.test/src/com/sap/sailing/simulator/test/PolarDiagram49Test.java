package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;
import java.util.NavigableMap;
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
    public void testPolarDiagram49_1() {
    	
    	NavigableMap<Speed, NavigableMap<Bearing, Speed>> table = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
    	NavigableMap<Bearing, Speed> tableRow; 
    	
    	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(60), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(75), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(90), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(110), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(120), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(135), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(150), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(180), Speed.NULL);
    	table.put(Speed.NULL, tableRow);
    	
    	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(6.57));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(7.01));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(7.36));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(7.31));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(6.73));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(6.39));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(5.58));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(4.62));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(2.5));
    	table.put(new KnotSpeedImpl(6), tableRow);
    	
    	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(7.78));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(8.13));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(8.38));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(8.31));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(8.04));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(7.79));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(7));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(5.85));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(3));
    	table.put(new KnotSpeedImpl(8), tableRow);
    	
    	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(8.34));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(8.66));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(9.01));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(8.95));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(8.87));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(8.64));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(8.02));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(6.97));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(4));
    	table.put(new KnotSpeedImpl(10), tableRow);
    	
    	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(8.64));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(8.95));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(9.39));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(9.5));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(9.54));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(9.3));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(8.71));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(7.86));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5));
    	table.put(new KnotSpeedImpl(12), tableRow);
    	
     	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(8.84));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(9.15));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(9.62));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(9.98));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(10.08));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(9.95));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(9.29));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(8.57));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(6));
    	table.put(new KnotSpeedImpl(14), tableRow);
    	
     	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(8.95));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(9.28));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(9.8));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(10.28));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(10.44));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(10.72));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(9.88));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(9.13));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(7));
    	table.put(new KnotSpeedImpl(16), tableRow);
    	
     	tableRow = new TreeMap<Bearing,Speed>(PolarDiagram49.bearingComparator);
    	tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
    	tableRow.put(new DegreeBearingImpl(52), new KnotSpeedImpl(8.91));
    	tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(9.34));
    	tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(10.01));
    	tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(10.67));
    	tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(11.17));
    	tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(11.81));
    	tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(11.47));
    	tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(10.26));
    	tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(7.5));
    	table.put(new KnotSpeedImpl(20), tableRow);
    	
    	NavigableMap<Speed, Bearing> beatAngles = new TreeMap<Speed, Bearing>();
    	beatAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(45));
    	beatAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(43.4));
    	beatAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(41.8));
    	beatAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(38.9));
    	beatAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(37.5));
    	beatAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(36.8));
    	beatAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(36.3));
    	beatAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(36.9));
    	
    	NavigableMap<Speed, Speed> beatSOG = new TreeMap<Speed, Speed>();
    	beatSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
    	beatSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(5.85));
    	beatSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(6.98));
    	beatSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(7.39));
    	beatSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(7.68));
    	beatSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(7.89));
    	beatSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(7.92));
    	beatSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(7.94));
    	
    	NavigableMap<Speed, Bearing> gybeAngles = new TreeMap<Speed, Bearing>();
    	gybeAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(120));
    	gybeAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(140));
    	gybeAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(142.6));
    	gybeAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(148.4));
    	gybeAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(154.4));
    	gybeAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(162.5));
    	gybeAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(169.6));
    	gybeAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(172.3));
    	
    	NavigableMap<Speed, Speed> gybeSOG = new TreeMap<Speed, Speed>();
    	gybeSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
    	gybeSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(5.22));
    	gybeSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(6.38));
    	gybeSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(7.09));
    	gybeSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(7.6));
    	gybeSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(7.97));
    	gybeSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(8.39));
    	gybeSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(9.38));
    	
    	PolarDiagram polarDiagram = new PolarDiagram49(table, beatAngles, gybeAngles, beatSOG, gybeSOG);
    	SpeedWithBearing newWind = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(180));
    	polarDiagram.setWind(newWind);
    	
    	System.out.println(polarDiagram.getSpeedAtBearing(new DegreeBearingImpl(52)));
    	//for (Bearing b: polarDiagram.optimalDirectionsUpwind()) System.out.println(b);
    	
    	assertEquals("no test",1,1);
    	
    }

}
