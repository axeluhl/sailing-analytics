package com.sap.sailing.simulator.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;

public class PolarDiagramCSV extends PolarDiagramBase {

    private static final long serialVersionUID = -9219705955440602679L;

    public PolarDiagramCSV(String inputFile) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream csvFile = cl.getResourceAsStream(inputFile);
        InputStreamReader isr = new InputStreamReader(csvFile);
        BufferedReader bfr = new BufferedReader(isr);

        List<Speed> velocities = new ArrayList<Speed>();
        List<Bearing> beatAngles = new ArrayList<Bearing>();
        List<Speed> beatVMG = new ArrayList<Speed>();
        Map<Bearing, List<Speed>> speeds = new HashMap<Bearing, List<Speed>>();
        List<Speed> runVMG = new ArrayList<Speed>();
        List<Bearing> gybeAngles = new ArrayList<Bearing>();

        String line = "";
        String[] elements = null;
        while (true) {

            line = bfr.readLine();
            if (line == null) {
                break;
            }
            elements = line.split(",");
            elements[0] = elements[0].replace(" ", "");
            elements[0] = elements[0].toLowerCase();

            switch (elements[0]) {
            case "windvelocity":
                for (int i = 1; i < elements.length; i++) {
                    velocities.add(new KnotSpeedImpl(new Double(elements[i])));
                }
                break;
            case "beatangles":
                for (int i = 1; i < elements.length; i++) {
                    beatAngles.add(new DegreeBearingImpl(new Double(elements[i])));
                }
                break;
            case "beatsog":
                for (int i = 1; i < elements.length; i++) {
                    beatVMG.add(new KnotSpeedImpl(new Double(elements[i])));
                }
                break;
            case "runsog":
                for (int i = 1; i < elements.length; i++) {
                    runVMG.add(new KnotSpeedImpl(new Double(elements[i])));
                }
                break;
            case "gybeangles":
                for (int i = 1; i < elements.length; i++) {
                    gybeAngles.add(new DegreeBearingImpl(new Double(elements[i])));
                }
                break;
            default:
                List<Speed> sp = new ArrayList<Speed>();

                for (int i = 1; i < elements.length; i++) {
                    if (elements[i].length() > 0) {
                        sp.add(new KnotSpeedImpl(new Double(elements[i])));
                    } else {
                        sp.add(Speed.NULL);
                    }
                }
                speeds.put(new DegreeBearingImpl(new Double(elements[0])), sp);
                break;

            }
        }
        bfr.close();
        isr.close();

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
            mapBeatAngles.put(velocity, beatAngles.get(index));
            mapGybeAngles.put(velocity, gybeAngles.get(index));
            mapBeatSOG.put(velocity, beatVMG.get(index));
            mapGybeSOG.put(velocity, runVMG.get(index));
        }

        //setWind(new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(180)));

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
