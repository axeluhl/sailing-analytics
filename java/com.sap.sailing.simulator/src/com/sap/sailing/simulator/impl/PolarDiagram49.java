package com.sap.sailing.simulator.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
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

    //private static boolean initialized = false;

    public PolarDiagram49() {
    	speedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Bearing, Speed> tableRow;

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(Speed.NULL, tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(6), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(8), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(10), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(12), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(14), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(16), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram49.bearingComparator);
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
        speedTable.put(new KnotSpeedImpl(20), tableRow);

        beatAngles = new TreeMap<Speed, Bearing>();
        beatAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(45));
        beatAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(43.4));
        beatAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(41.8));
        beatAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(38.9));
        beatAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(37.5));
        beatAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(36.8));
        beatAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(36.3));
        beatAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(36.9));

        double beatScale = 1.1;
        beatSOG = new TreeMap<Speed, Speed>();
        beatSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
        beatSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(5.85 * beatScale));
        beatSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(6.98 * beatScale));
        beatSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(7.39 * beatScale));
        beatSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(7.68 * beatScale));
        beatSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(7.89 * beatScale));
        beatSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(7.92 * beatScale));
        beatSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(7.94 * beatScale));

        gybeAngles = new TreeMap<Speed, Bearing>();
        gybeAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(120));
        gybeAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(140));
        gybeAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(142.6));
        gybeAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(148.4));
        gybeAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(154.4));
        gybeAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(162.5));
        gybeAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(169.6));
        gybeAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(172.3));

        gybeSOG = new TreeMap<Speed, Speed>();
        gybeSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
        gybeSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(5.22));
        gybeSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(6.38));
        gybeSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(7.09));
        gybeSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(7.6));
        gybeSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(7.97));
        gybeSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(8.39));
        gybeSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(9.38));

    }

    public PolarDiagram49(NavigableMap<Speed, NavigableMap<Bearing, Speed>> speeds, NavigableMap<Speed, Bearing> beats,
            NavigableMap<Speed, Bearing> gybes, NavigableMap<Speed, Speed> beatSOGs, NavigableMap<Speed, Speed> gybeSOGs) {

        wind = new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(180));

        speedTable = speeds;
        beatAngles = beats;
        gybeAngles = gybes;
        beatSOG = beatSOGs;
        gybeSOG = gybeSOGs;

        for (Speed s : speedTable.keySet()) {

            if (beatAngles.containsKey(s) && !speedTable.get(s).containsKey(beatAngles.get(s)))
                speedTable.get(s).put(beatAngles.get(s), beatSOG.get(s));

            if (gybeAngles.containsKey(s) && !speedTable.get(s).containsKey(gybeAngles.get(s)))
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
        if (relativeBearing.getDegrees() < 0)
            relativeBearing = relativeBearing.getDifferenceTo(new DegreeBearingImpl(0));

        Speed floorWind = speedTable.floorKey(wind);
        Speed ceilingWind = speedTable.ceilingKey(wind);

        if (ceilingWind == null) {
            ceilingWind = floorWind;
        }
        if (floorWind == null) {
            floorWind = ceilingWind;
        }
        
        
        NavigableMap<Bearing, Speed> floorSpeeds = speedTable.get(floorWind);
        NavigableMap<Bearing, Speed> ceilingSpeeds = speedTable.get(ceilingWind);

        // Taylor estimations of order 1
        Speed floorSpeed1 = floorSpeeds.floorEntry(relativeBearing).getValue();
        Speed floorSpeed2 = floorSpeeds.ceilingEntry(relativeBearing).getValue();
        Bearing floorBearing1 = floorSpeeds.floorKey(relativeBearing);
        Bearing floorBearing2 = floorSpeeds.ceilingKey(relativeBearing);
        double floorSpeed;
        if (floorSpeed1.equals(floorSpeed2)) {
            floorSpeed = floorSpeed1.getKnots();
        } else {
            floorSpeed = floorSpeed1.getKnots() + (relativeBearing.getRadians() - floorBearing1.getRadians())
                    * (floorSpeed2.getKnots() - floorSpeed1.getKnots())
                    / (floorBearing2.getRadians() - floorBearing1.getRadians());
        }

        Speed ceilingSpeed1 = ceilingSpeeds.floorEntry(relativeBearing).getValue();
        Speed ceilingSpeed2 = ceilingSpeeds.ceilingEntry(relativeBearing).getValue();
        Bearing ceilingBearing1 = ceilingSpeeds.floorKey(relativeBearing);
        Bearing ceilingBearing2 = ceilingSpeeds.ceilingKey(relativeBearing);
        double ceilingSpeed;
        if (ceilingSpeed1.equals(ceilingSpeed2)) {
            ceilingSpeed = ceilingSpeed1.getKnots();
        } else {
            ceilingSpeed = ceilingSpeed1.getKnots() + (relativeBearing.getRadians() - ceilingBearing1.getRadians())
                    * (ceilingSpeed2.getKnots() - ceilingSpeed1.getKnots())
                    / (ceilingBearing2.getRadians() - ceilingBearing1.getRadians());
        }

        double speed;
        if (floorWind.equals(ceilingWind)) {
            speed = floorSpeed;
        } else {
            speed = floorSpeed + (wind.getKnots() - floorWind.getKnots()) * (ceilingSpeed - floorSpeed)
                    / (ceilingWind.getKnots() - floorWind.getKnots());
        }
        if ((Math.abs(relativeBearing.getDegrees()) < 20)||(Math.abs(relativeBearing.getDegrees()) > 330)) {
            speed = 0.1;
        }

        return new KnotSpeedWithBearingImpl(speed, bearing);
    }

    @Override
    public Bearing[] optimalDirectionsUpwind() {
        Bearing windBearing = wind.getBearing().reverse();
        Bearing floorBeatAngle;
        if (beatAngles.floorEntry(wind) == null) {
            floorBeatAngle = beatAngles.ceilingEntry(wind).getValue();
        } else {
            floorBeatAngle = beatAngles.floorEntry(wind).getValue();
        }
        Bearing ceilingBeatAngle;
        if (beatAngles.ceilingEntry(wind) == null) {
            ceilingBeatAngle = beatAngles.floorEntry(wind).getValue();            
        } else {
            ceilingBeatAngle = beatAngles.ceilingEntry(wind).getValue();
        }
        if (floorBeatAngle == null)
            floorBeatAngle = new DegreeBearingImpl(0);
        if (ceilingBeatAngle == null)
            ceilingBeatAngle = new DegreeBearingImpl(0);

        Speed floorSpeed = beatAngles.floorKey(wind);
        if (floorSpeed == null) {
            floorSpeed = beatAngles.ceilingKey(wind);
        }
        Speed ceilingSpeed = beatAngles.ceilingKey(wind);
        if (beatAngles.ceilingKey(wind) == null) {
            ceilingSpeed = beatAngles.floorKey(wind);
        }
        double beatAngle;
        if (floorSpeed.equals(ceilingSpeed)) {
            beatAngle = floorBeatAngle.getRadians();
        } else {
            beatAngle = floorBeatAngle.getRadians() + (wind.getKnots() - floorSpeed.getKnots())
                    * (ceilingBeatAngle.getRadians() - floorBeatAngle.getRadians())
                    / (ceilingSpeed.getKnots() - floorSpeed.getKnots());
        }
        Bearing estBeatAngleRight = new RadianBearingImpl(+beatAngle);
        Bearing estBeatAngleLeft = new RadianBearingImpl(-beatAngle);

        return new Bearing[] {
                // windBearing.add(estBeatAngle),
                // windBearing.add(estBeatAngle.getDifferenceTo(windBearing))
                windBearing.add(estBeatAngleLeft), windBearing.add(estBeatAngleRight) };
    }

    @Override
    public Bearing[] optimalDirectionsDownwind() {
        // TODO
        Bearing windBearing = wind.getBearing().reverse();
        Bearing floorGybeAngle = gybeAngles.floorEntry(wind).getValue();
        Bearing ceilingGybeAngle = gybeAngles.ceilingEntry(wind).getValue();
        if (floorGybeAngle == null)
            floorGybeAngle = new DegreeBearingImpl(0);
        if (ceilingGybeAngle == null)
            ceilingGybeAngle = new DegreeBearingImpl(0);
        Speed floorSpeed = gybeAngles.floorKey(wind);
        Speed ceilingSpeed = gybeAngles.ceilingKey(wind);
        double gybeAngle;
        if (floorSpeed.equals(ceilingSpeed)) {
            gybeAngle = floorGybeAngle.getRadians();
        } else {
            gybeAngle = floorGybeAngle.getRadians() + (wind.getKnots() - floorSpeed.getKnots())
                    * (ceilingGybeAngle.getRadians() - floorGybeAngle.getRadians())
                    / (ceilingSpeed.getKnots() - floorSpeed.getKnots());
        }
        Bearing estGybeAngle = new RadianBearingImpl(gybeAngle);
        return new Bearing[] { windBearing.add(estGybeAngle),
                windBearing.add(estGybeAngle.getDifferenceTo(windBearing)) };
    }

    public static Comparator<Bearing> bearingComparator = new Comparator<Bearing>() {

        @Override
        public int compare(Bearing o1, Bearing o2) {
            Double d1 = o1.getDegrees();
            if (d1 < 0)
                d1 = 360 + d1;
            Double d2 = o2.getDegrees();
            if (d2 < 0)
                d2 = 360 + d2;
            return d1.compareTo(d2);
        }

    };

    
	@Override
	public long getTurnLoss() {
		// TODO Auto-generated method stub
		return 4000;
	}

	//TO BE REVIEWED
	//not sure I use the right terms and conventions
	@Override
	public WindSide getWindSide(Bearing bearing) {
		WindSide windSide = null;
		if(bearingComparator.compare(bearing, wind.getBearing().reverse()) > 0) windSide = WindSide.LEFT;
		if(bearingComparator.compare(bearing, wind.getBearing().reverse()) < 0) windSide = WindSide.RIGHT;
		if(bearing.equals(wind.getBearing())) windSide = WindSide.OPPOSING;
		if(bearing.equals(wind.getBearing().reverse())) windSide = WindSide.FACING;
		
		return windSide;
	}

	@Override
	public NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(
			Double bearingStep) {
	
		NavigableMap<Speed, NavigableMap<Bearing, Speed>> table = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
		Set<Bearing> extraBearings = new HashSet<Bearing>();
		
		for (Speed s : speedTable.keySet()) {
			setWind(new KnotSpeedWithBearingImpl(s.getKnots(), new DegreeBearingImpl(180)));
			extraBearings.addAll(Arrays.asList(optimalDirectionsUpwind()));
			extraBearings.addAll(Arrays.asList(optimalDirectionsDownwind()));	
		}
		
		for (Speed s : speedTable.keySet()) {
			setWind(new KnotSpeedWithBearingImpl(s.getKnots(), new DegreeBearingImpl(180)));
			NavigableMap<Bearing, Speed> currentTable = new TreeMap<Bearing, Speed>(bearingComparator);
			table.put(s, currentTable);
			
			for (Double b = 0.0; b < 360.0; b += bearingStep) {
				Bearing bearing = new DegreeBearingImpl(b);
				currentTable.put(bearing, getSpeedAtBearing(bearing));
			}
			for (Bearing extraBearing : extraBearings)
				currentTable.put(extraBearing, getSpeedAtBearing(extraBearing));
		}
		
		return table;
	}

	@Override
	public Bearing getTargetDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTargetDirection(Bearing newTargetDirection) {
		// TODO Auto-generated method stub
		
	}

}
