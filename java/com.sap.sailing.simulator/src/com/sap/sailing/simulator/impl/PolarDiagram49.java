package com.sap.sailing.simulator.impl;

import java.util.Comparator;
import java.util.NavigableMap;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;


public class PolarDiagram49 implements PolarDiagram {
	
	private SpeedWithBearing wind;
	
	private NavigableMap<Speed, NavigableMap<Bearing, Speed>> speedTable;
	private NavigableMap<Speed, Bearing> beatAngles;
	private NavigableMap<Speed, Bearing> gybeAngles;
	private NavigableMap<Speed, Speed> beatSOG;
	private NavigableMap<Speed, Speed> gybeSOG;

	public PolarDiagram49(NavigableMap<Speed, NavigableMap<Bearing, Speed>> speeds,
			NavigableMap<Speed, Bearing> beats,
			NavigableMap<Speed, Bearing> gybes,
			NavigableMap<Speed, Speed> beatSOGs,
			NavigableMap<Speed, Speed> gybeSOGs) {
		
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
				
		//TODO
		Bearing relativeBearing = wind.getBearing().reverse().getDifferenceTo(bearing);
		if (relativeBearing.getDegrees() < 0) relativeBearing = relativeBearing.getDifferenceTo(new DegreeBearingImpl(0));
		
		Speed floorWind = speedTable.floorKey(wind);
		Speed ceilingWind = speedTable.ceilingKey(wind);
		
		NavigableMap<Bearing, Speed> floorSpeeds = speedTable.get(floorWind);
		NavigableMap<Bearing, Speed> ceilingSpeeds = speedTable.get(ceilingWind);
		
		//Taylor estimations of order 1
		Speed floorSpeed1 = floorSpeeds.floorEntry(relativeBearing).getValue();
		Speed floorSpeed2 = floorSpeeds.ceilingEntry(relativeBearing).getValue();
		Bearing floorBearing1 = floorSpeeds.floorKey(relativeBearing);
		Bearing floorBearing2 = floorSpeeds.ceilingKey(relativeBearing);
		double floorSpeed = floorSpeed1.getKnots() 
				+ (relativeBearing.getRadians() - floorBearing1.getRadians())
				* (floorSpeed2.getKnots() - floorSpeed1.getKnots())
				/ (floorBearing2.getRadians() - floorBearing1.getRadians());
		
		Speed ceilingSpeed1 = ceilingSpeeds.floorEntry(relativeBearing).getValue();
		Speed ceilingSpeed2 = ceilingSpeeds.ceilingEntry(relativeBearing).getValue();
		Bearing ceilingBearing1 = ceilingSpeeds.floorKey(relativeBearing);
		Bearing ceilingBearing2 = ceilingSpeeds.ceilingKey(relativeBearing);
		double ceilingSpeed = ceilingSpeed1.getKnots()
				+ (relativeBearing.getRadians() - ceilingBearing1.getRadians())
				* (ceilingSpeed2.getKnots() - ceilingSpeed2.getKnots())
				/ (ceilingBearing2.getRadians() - ceilingBearing1.getRadians());
		
		double speed = floorSpeed 
				+ (wind.getKnots() - floorWind.getKnots())
				* (ceilingSpeed - floorSpeed)
				/ (ceilingWind.getKnots() - floorWind.getKnots());
		
		return new KnotSpeedWithBearingImpl(speed, bearing);
	}

	@Override
	public Bearing[] optimalDirectionsUpwind() {
		//TODO
		Bearing windBearing = wind.getBearing().reverse();
		Bearing floorBeatAngle = beatAngles.floorEntry(wind).getValue();
		Bearing ceilingBeatAngle = beatAngles.ceilingEntry(wind).getValue();
		if(floorBeatAngle == null) floorBeatAngle = new DegreeBearingImpl(0);
		if(ceilingBeatAngle == null) ceilingBeatAngle = new DegreeBearingImpl(0);
		Speed floorSpeed = beatAngles.floorKey(wind);
		Speed ceilingSpeed = beatAngles.ceilingKey(wind);
		System.out.println(floorSpeed);
		System.out.println(ceilingSpeed);
		double beatAngle = floorBeatAngle.getRadians() 
				+ (wind.getKnots() - floorSpeed.getKnots())
				* (ceilingBeatAngle.getRadians() - floorBeatAngle.getRadians())
				/ (ceilingSpeed.getKnots() - floorSpeed.getKnots());
		Bearing estBeatAngle = new RadianBearingImpl(beatAngle);
		return new Bearing[] { 
				windBearing.add(estBeatAngle), 
				windBearing.add(estBeatAngle.getDifferenceTo(windBearing)) 
				};
	}

	@Override
	public Bearing[] optimalDirectionsDownwind() {
		//TODO
		Bearing windBearing = wind.getBearing().reverse();
		Bearing floorGybeAngle = gybeAngles.floorEntry(wind).getValue();
		Bearing ceilingGybeAngle = gybeAngles.ceilingEntry(wind).getValue();
		if(floorGybeAngle == null) floorGybeAngle = new DegreeBearingImpl(0);
		if(ceilingGybeAngle == null) ceilingGybeAngle = new DegreeBearingImpl(0);
		Speed floorSpeed = gybeAngles.floorKey(wind);
		Speed ceilingSpeed = gybeAngles.ceilingKey(wind);
		double gybeAngle = floorGybeAngle.getRadians() 
				+ (wind.getKnots() - floorSpeed.getKnots())
				* (ceilingGybeAngle.getRadians() - floorGybeAngle.getRadians())
				/ (ceilingSpeed.getKnots() - floorSpeed.getKnots());
		Bearing estGybeAngle = new RadianBearingImpl(gybeAngle);
		return new Bearing[] { 
				windBearing.add(estGybeAngle), 
				windBearing.add(estGybeAngle.getDifferenceTo(windBearing)) 
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
