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
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public class PolarDiagramGPS extends PolarDiagramBase {

    private static final long serialVersionUID = -9219705955440602679L;
    private final BoatClass boatClass;
    private final PolarDataService polarData;

    public PolarDiagramGPS(BoatClass boatClass, PolarDataService polarData) {
    	this.boatClass = boatClass;
    	this.polarData = polarData;

        List<Speed> velocities = new ArrayList<Speed>();
        List<Bearing> beatAngles = new ArrayList<Bearing>();
        List<Speed> beatVMG = new ArrayList<Speed>();
        Map<Bearing, List<Speed>> speeds = new HashMap<Bearing, List<Speed>>();
        List<Speed> runVMG = new ArrayList<Speed>();
        List<Bearing> gybeAngles = new ArrayList<Bearing>();

        // initialize wind speeds
        velocities.add(new KnotSpeedImpl(0.0));
        velocities.add(new KnotSpeedImpl(6.0));
        velocities.add(new KnotSpeedImpl(8.0));
        velocities.add(new KnotSpeedImpl(10.0));
        velocities.add(new KnotSpeedImpl(12.0));
        velocities.add(new KnotSpeedImpl(14.0));
        velocities.add(new KnotSpeedImpl(16.0));
        velocities.add(new KnotSpeedImpl(20.0));

        // initialize beat-angles and -speeds
        //SpeedWithBearing beatPort;
        SpeedWithBearing beatStar;
        for (int i = 0; i < velocities.size(); i++) {
    		try {
    			//beatPort = this.polarData.getAverageSpeedWithBearing(this.boatClass, velocities.get(i), LegType.UPWIND, Tack.PORT).getObject();
    			beatStar = this.polarData.getAverageSpeedWithBearing(this.boatClass, velocities.get(i), LegType.UPWIND, Tack.STARBOARD).getObject();
    		} catch (NotEnoughDataHasBeenAddedException e) {
    			//beatPort = null;
    			beatStar = null;
    		}
    		if (beatStar != null) {
    			beatAngles.add(beatStar.getBearing());
    			beatVMG.add(beatStar);
    		} else {
    			beatAngles.add(null);
    			beatVMG.add(null);    			
    		}
        }

        // initialize jibe-angles and -speeds
        //SpeedWithBearing jibePort;
        SpeedWithBearing jibeStar;
        for (int i = 0; i < velocities.size(); i++) {
    		try {
    			//jibePort = this.polarData.getAverageSpeedWithBearing(this.boatClass, velocities.get(i), LegType.DOWNWIND, Tack.PORT).getObject();
    			jibeStar = this.polarData.getAverageSpeedWithBearing(this.boatClass, velocities.get(i), LegType.DOWNWIND, Tack.STARBOARD).getObject();
    		} catch (NotEnoughDataHasBeenAddedException e) {
    			// TODO Auto-generated catch block
    			//e.printStackTrace();
    			//jibePort = null;
    			jibeStar = null;
    		}
    		if (jibeStar != null) {
    			gybeAngles.add(jibeStar.getBearing());
    			runVMG.add(jibeStar);
    		} else {
    			gybeAngles.add(null);
    			runVMG.add(null);    			
    		}
        }

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> mapSpeedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Speed, Bearing> mapBeatAngles = new TreeMap<Speed, Bearing>();
        NavigableMap<Speed, Bearing> mapGybeAngles = new TreeMap<Speed, Bearing>();
        NavigableMap<Speed, Speed> mapBeatSOG = new TreeMap<Speed, Speed>();
        NavigableMap<Speed, Speed> mapGybeSOG = new TreeMap<Speed, Speed>();

        Speed velocity = null;
        Speed speed = null;
        NavigableMap<Bearing, Speed> speedTableLine = null;

        for (int index = 0; index < velocities.size(); index++) {
            velocity = velocities.get(index);
            if (velocity.getKnots() == 0.0) {
            	mapBeatSOG.put(new KnotSpeedImpl(0.0), new KnotSpeedImpl(0.0));
            	mapGybeSOG.put(new KnotSpeedImpl(0.0), new KnotSpeedImpl(0.0));
            }
            speedTableLine = new TreeMap<Bearing, Speed>(bearingComparator);
            for (Entry<Bearing, List<Speed>> entry : speeds.entrySet()) {
                if (index >= entry.getValue().size()) {
                    continue;
                }

                speed = entry.getValue().get(index);

                if (speed != Speed.NULL) {
                    speedTableLine.put(entry.getKey(), speed);
                }
            }

            mapSpeedTable.put(velocity, speedTableLine);
            if (beatAngles.get(index) != null) {
            	mapBeatAngles.put(velocity, beatAngles.get(index));
            }
            if (gybeAngles.get(index) != null) {
            	mapGybeAngles.put(velocity, gybeAngles.get(index));
            }
            if (beatVMG.get(index) != null) {
            	mapBeatSOG.put(velocity, beatVMG.get(index));
            }
            if (runVMG.get(index) != null) {
            	mapGybeSOG.put(velocity, runVMG.get(index));
            }
        }

        super.speedTable = mapSpeedTable;
        super.beatAngles = mapBeatAngles;
        super.gybeAngles = mapGybeAngles;
        super.beatSOG = mapBeatSOG;
        super.gybeSOG = mapGybeSOG;

        for (Speed s : super.speedTable.keySet()) {

            if (super.beatAngles.containsKey(s) && !super.speedTable.get(s).containsKey(super.beatAngles.get(s))) {
                super.speedTable.get(s).put(super.beatAngles.get(s), super.beatSOG.get(s));
            }

            if (super.gybeAngles.containsKey(s) && !super.speedTable.get(s).containsKey(super.gybeAngles.get(s))) {
                super.speedTable.get(s).put(super.gybeAngles.get(s), super.gybeSOG.get(s));
            }

        }

    }

}
