package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;

public class PolarDiagramGPS extends PolarDiagramBase {

    private static final long serialVersionUID = -9219705955440602679L;
    private final BoatClass boatClass;
    private final PolarDataService polarData;

    public PolarDiagramGPS(BoatClass boatClass, PolarDataService polarData) {
    	this.boatClass = boatClass;
    	this.polarData = polarData;

        List<Speed> windSpeeds = new ArrayList<Speed>();
        List<Bearing> beatAngles = new ArrayList<Bearing>();
        List<Speed> beatSpeed = new ArrayList<Speed>();
        Map<Bearing, List<Speed>> speeds = new HashMap<Bearing, List<Speed>>();
        List<Speed> jibeSpeed = new ArrayList<Speed>();
        List<Bearing> jibeAngles = new ArrayList<Bearing>();

        // initialize wind speeds
        windSpeeds.add(new KnotSpeedImpl(0.0));
        windSpeeds.add(new KnotSpeedImpl(6.0));
        windSpeeds.add(new KnotSpeedImpl(8.0));
        windSpeeds.add(new KnotSpeedImpl(10.0));
        windSpeeds.add(new KnotSpeedImpl(12.0));
        windSpeeds.add(new KnotSpeedImpl(14.0));
        windSpeeds.add(new KnotSpeedImpl(16.0));
        windSpeeds.add(new KnotSpeedImpl(20.0));

        // initialize beat-angles and -speeds
        SpeedWithBearing beatPort;
        SpeedWithBearing beatStar;
        for (int i = 0; i < windSpeeds.size(); i++) {
    		try {
    			beatPort = this.polarData.getAverageSpeedWithBearing(this.boatClass, windSpeeds.get(i), LegType.UPWIND, Tack.PORT, true).getObject();
    			beatStar = this.polarData.getAverageSpeedWithBearing(this.boatClass, windSpeeds.get(i), LegType.UPWIND, Tack.STARBOARD, true).getObject();
    		} catch (NotEnoughDataHasBeenAddedException e) {
    			beatPort = null;
    			beatStar = null;
    		}
    		if (beatStar != null) {
    			Bearing avgBeatAngle = new DegreeBearingImpl((beatStar.getBearing().getDegrees() + (360-beatPort.getBearing().getDegrees())%360)/2.0);
    			beatAngles.add(avgBeatAngle);
    			Speed avgBeatSpeed = new KnotSpeedImpl((beatStar.getKnots() + beatPort.getKnots())/2.0);
    			beatSpeed.add(avgBeatSpeed);
    		} else {
    			beatAngles.add(null);
    			beatSpeed.add(null);    			
    		}
        }

        // initialize jibe-angles and -speeds
        SpeedWithBearing jibePort;
        SpeedWithBearing jibeStar;
        for (int i = 0; i < windSpeeds.size(); i++) {
    		try {
    			jibePort = this.polarData.getAverageSpeedWithBearing(this.boatClass, windSpeeds.get(i), LegType.DOWNWIND, Tack.PORT, false).getObject();
    			jibeStar = this.polarData.getAverageSpeedWithBearing(this.boatClass, windSpeeds.get(i), LegType.DOWNWIND, Tack.STARBOARD, false).getObject();
    		} catch (NotEnoughDataHasBeenAddedException e) {
    			jibePort = null;
    			jibeStar = null;
    		}
    		if (jibeStar != null) {
    			Bearing avgJibeAngle = new DegreeBearingImpl((jibeStar.getBearing().getDegrees() + (360-jibePort.getBearing().getDegrees())%360)/2.0);
    			jibeAngles.add(avgJibeAngle);
    			Speed avgJibeSpeed = new KnotSpeedImpl((jibeStar.getKnots() + jibePort.getKnots())/2.0);
    			jibeSpeed.add(avgJibeSpeed);
    		} else {
    			jibeAngles.add(null);
    			jibeSpeed.add(null);    			
    		}
        }

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> mapSpeedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Speed, Bearing> mapBeatAngles = new TreeMap<Speed, Bearing>();
        NavigableMap<Speed, Bearing> mapJibeAngles = new TreeMap<Speed, Bearing>();
        NavigableMap<Speed, Speed> mapBeatSOG = new TreeMap<Speed, Speed>();
        NavigableMap<Speed, Speed> mapJibeSOG = new TreeMap<Speed, Speed>();

        Speed windSpeed = null;
        Speed boatSpeed = null;
        NavigableMap<Bearing, Speed> speedTableLine = null;

        for (int index = 0; index < windSpeeds.size(); index++) {
            windSpeed = windSpeeds.get(index);
            if (windSpeed.getKnots() == 0.0) {
            	mapBeatSOG.put(new KnotSpeedImpl(0.0), new KnotSpeedImpl(0.0));
            	mapJibeSOG.put(new KnotSpeedImpl(0.0), new KnotSpeedImpl(0.0));
            }
            speedTableLine = new TreeMap<Bearing, Speed>(bearingComparator);
            for (Entry<Bearing, List<Speed>> entry : speeds.entrySet()) {
                if (index >= entry.getValue().size()) {
                    continue;
                }

                boatSpeed = entry.getValue().get(index);

                if (boatSpeed != Speed.NULL) {
                    speedTableLine.put(entry.getKey(), boatSpeed);
                }
            }

            mapSpeedTable.put(windSpeed, speedTableLine);
            if (beatAngles.get(index) != null) {
            	mapBeatAngles.put(windSpeed, beatAngles.get(index));
            }
            if (jibeAngles.get(index) != null) {
            	mapJibeAngles.put(windSpeed, jibeAngles.get(index));
            }
            if (beatSpeed.get(index) != null) {
            	mapBeatSOG.put(windSpeed, beatSpeed.get(index));
            }
            if (jibeSpeed.get(index) != null) {
            	mapJibeSOG.put(windSpeed, jibeSpeed.get(index));
            }
        }

        super.speedTable = mapSpeedTable;
        super.beatAngles = mapBeatAngles;
        super.jibeAngles = mapJibeAngles;
        super.beatSOG = mapBeatSOG;
        super.jibeSOG = mapJibeSOG;

        for (Speed s : super.speedTable.keySet()) {

            if (super.beatAngles.containsKey(s) && !super.speedTable.get(s).containsKey(super.beatAngles.get(s))) {
                super.speedTable.get(s).put(super.beatAngles.get(s), super.beatSOG.get(s));
            }

            if (super.jibeAngles.containsKey(s) && !super.speedTable.get(s).containsKey(super.jibeAngles.get(s))) {
                super.speedTable.get(s).put(super.jibeAngles.get(s), super.jibeSOG.get(s));
            }

        }

    }

}
