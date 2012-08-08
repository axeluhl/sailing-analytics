package com.sap.sailing.simulator.impl;

import java.util.NavigableMap;
import java.util.TreeMap;

import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public class PolarDiagram505 extends PolarDiagramBase {

    // this constructor creates an instance with a hard-coded set of values
    public PolarDiagram505() {
        speedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Bearing, Speed> tableRow;

        double cutAngle = 35.0;

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(75), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(90), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(110), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(120), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(135), Speed.NULL);
        // tableRow.put(new DegreeBearingImpl(150), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(180), Speed.NULL);
        speedTable.put(Speed.NULL, tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(5.09));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(5.17));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(5.25));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(5.15));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(5.1));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(5.03));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(4.62));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(2.5));
        speedTable.put(new KnotSpeedImpl(6), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(5.38));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(5.47));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(5.57));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(6.01));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(6.23));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(6.57));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(5.85));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(3.35));
        speedTable.put(new KnotSpeedImpl(8), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(5.67));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(5.77));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(5.88));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(6.94));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(7.47));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(8.26));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(6.97));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(4.25));
        speedTable.put(new KnotSpeedImpl(10), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(6.06));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(6.18));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(6.30));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(7.86));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(8.64));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(9.81));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(7.86));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5));
        speedTable.put(new KnotSpeedImpl(12), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(6.55));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(6.69));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(6.83));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(8.71));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(9.66));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(11.07));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(8.57));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5.75));
        speedTable.put(new KnotSpeedImpl(14), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(7.05));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(7.20));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(7.35));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(9.50));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(10.58));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(12.19));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(9.13));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(6.5));
        speedTable.put(new KnotSpeedImpl(16), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(7.04));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(7.20));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(7.35));
        tableRow.put(new DegreeBearingImpl(110), new KnotSpeedImpl(9.90));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(11.18));
        tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(13.09));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(10.26));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(7.5));
        speedTable.put(new KnotSpeedImpl(20), tableRow);

        NavigableMap<Speed, Bearing> beatAngles = new TreeMap<Speed, Bearing>();
        beatAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(44.0));
        beatAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(44.0));
        beatAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(46.5));
        beatAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(50.0));
        beatAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(52.0));
        beatAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(54.0));
        beatAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(55.0));
        beatAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(56.0));

        double beatScale = 1.0;
        NavigableMap<Speed, Speed> beatSOG = new TreeMap<Speed, Speed>();
        beatSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
        beatSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(5.00 * beatScale));
        beatSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(5.30 * beatScale));
        beatSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(5.60 * beatScale));
        beatSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(6.00 * beatScale));
        beatSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(6.50 * beatScale));
        beatSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(7.00 * beatScale));
        beatSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(7.00 * beatScale));

        NavigableMap<Speed, Bearing> gybeAngles = new TreeMap<Speed, Bearing>();
        gybeAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(141.0));
        gybeAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(141.0));
        gybeAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(141.0));
        gybeAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(139.5));
        gybeAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(137.5));
        gybeAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(139.5));
        gybeAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(142.5));
        gybeAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(150.0));

        NavigableMap<Speed, Speed> gybeSOG = new TreeMap<Speed, Speed>();
        gybeSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
        gybeSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(5.00));
        gybeSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(6.70));
        gybeSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(8.50));
        gybeSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(10.00));
        gybeSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(11.50));
        gybeSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(13.00));
        gybeSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(15.00));

        for (Speed s : speedTable.keySet()) {

            if (beatAngles.containsKey(s) && !speedTable.get(s).containsKey(beatAngles.get(s)))
                speedTable.get(s).put(beatAngles.get(s), beatSOG.get(s));

            if (gybeAngles.containsKey(s) && !speedTable.get(s).containsKey(gybeAngles.get(s)))
                speedTable.get(s).put(gybeAngles.get(s), gybeSOG.get(s));

        }

    }

}
