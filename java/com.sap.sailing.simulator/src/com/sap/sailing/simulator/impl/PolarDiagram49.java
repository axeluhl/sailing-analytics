package com.sap.sailing.simulator.impl;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.SortedMap;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;


public class PolarDiagram49 implements PolarDiagram {
	
	private SpeedWithBearing wind;
	
	private NavigableMap<Speed, NavigableMap<Bearing, Speed>> speedTable;
	private SortedMap<Speed, Bearing> beatAngles;
	private SortedMap<Speed, Bearing> gybeAngles;
	private SortedMap<Speed, Speed> beatSOG;
	private SortedMap<Speed, Speed> gybeSOG;

	public PolarDiagram49(NavigableMap<Speed, NavigableMap<Bearing, Speed>> speeds,
			SortedMap<Speed, Bearing> beats,
			SortedMap<Speed, Bearing> gybes,
			SortedMap<Speed, Speed> beatSOGs,
			SortedMap<Speed, Speed> gybeSOGs) {
		
		wind = new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(180));
		
		speedTable = speeds;
		beatAngles = beats;
		gybeAngles = gybes;
		beatSOG = beatSOGs;
		gybeSOG = gybeSOGs;
		
		for (Speed s : speedTable.keySet()) {
			
			if ( beatAngles.containsKey(s) && !speedTable.get(s).containsKey(beatAngles.get(s)) ) 
				speedTable.get(s).put(beatAngles.get(s), beatSOG.get(s));
			
			if ( gybeAngles.containsKey(s) && !speedTable.get(s).containsKey(gybeAngles.get(s)) )
				speedTable.get(s).put(gybeAngles.get(s), gybeSOG.get(s));
			
		}
		
	}
	
	@Override
	public SpeedWithBearing getWind() {
		return wind;
	}

	@Override
	public void setWind(SpeedWithBearing newWind) {
		wind = newWind;
	}

	@Override
	public SpeedWithBearing getSpeedAtBearing(Bearing bearing) {
				
		Bearing relativeBearing = wind.getBearing().reverse().getDifferenceTo(bearing);
		if (relativeBearing.getDegrees() < 0) relativeBearing = relativeBearing.getDifferenceTo(new DegreeBearingImpl(0));
		
		Speed floorWind = speedTable.floorKey(wind);
		Speed ceilingWind = speedTable.ceilingKey(wind);
		
		NavigableMap<Bearing, Speed> floorSpeeds = speedTable.get(floorWind);
		NavigableMap<Bearing, Speed> ceilingSpeeds = speedTable.get(ceilingWind);
		
		Speed floorSpeed1 = floorSpeeds.floorEntry(relativeBearing).getValue();
		Speed floorSpeed2 = floorSpeeds.ceilingEntry(relativeBearing).getValue();
		double floorSpeed = floorSpeed1.getKnots()/2 + floorSpeed2.getKnots()/2;
		
		
		Speed ceilingSpeed1 = ceilingSpeeds.floorEntry(relativeBearing).getValue();
		Speed ceilingSpeed2 = ceilingSpeeds.ceilingEntry(relativeBearing).getValue();
		double ceilingSpeed = ceilingSpeed1.getKnots()/2 + ceilingSpeed2.getKnots()/2;
		
		double speed = floorSpeed/2 + ceilingSpeed/2;
		
		return new KnotSpeedWithBearingImpl(speed, bearing);
	}

	@Override
	public Bearing[] optimalDirectionsUpwind() {
		Bearing windBearing = wind.getBearing().reverse();
		return new Bearing[] { 
				windBearing.add(beatAngles.get(wind)), 
				windBearing.add(beatAngles.get(wind).getDifferenceTo(windBearing)) 
				};
	}

	@Override
	public Bearing[] optimalDirectionsDownwind() {
		Bearing windBearing = wind.getBearing().reverse();
		return new Bearing[] { 
				windBearing.add(gybeAngles.get(wind)), 
				windBearing.add(gybeAngles.get(wind).getDifferenceTo(windBearing)) 
				};
	}
	
	public static Comparator<Bearing> bearingComparator = new Comparator<Bearing>() {

		@Override
		public int compare(Bearing o1, Bearing o2) {
			Double d1 = o1.getDegrees();
			if (d1 < 0) d1 = 360 + d1;
			Double d2 = o2.getDegrees();
			if (d2 < 0) d2 = 360 + d2;
			return d1.compareTo(d2);
		}
		
	};
	
}
