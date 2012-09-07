package com.sap.sailing.simulator.impl;

import java.util.NavigableMap;
import java.util.TreeMap;

import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public class PolarDiagram49STG extends PolarDiagramBase {

    // this constructor creates an instance with a hard-coded set of values
    public PolarDiagram49STG() {

        speedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Bearing, Speed> tableRow;

        double cutAngle = 30.0;

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(75), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(90), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(105), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(120), Speed.NULL);
        //tableRow.put(new DegreeBearingImpl(135), Speed.NULL);
        // tableRow.put(new DegreeBearingImpl(150), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(180), Speed.NULL);
        speedTable.put(Speed.NULL, tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(4.58));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(4.65));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(4.73));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(5.82));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(6.91));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(5.03));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(4.62));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(2.84));
        speedTable.put(new KnotSpeedImpl(6), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(6.36));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(6.46));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(6.56));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(7.89));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(9.23));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(6.57));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(5.85));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(3.94));
        speedTable.put(new KnotSpeedImpl(8), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(8.14));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(8.27));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(8.40));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(9.93));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(11.45));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(8.26));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(6.97));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5.04));
        speedTable.put(new KnotSpeedImpl(10), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(8.65));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(8.79));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(8.93));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(10.44));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(11.96));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(9.81));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(7.86));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5.36));
        speedTable.put(new KnotSpeedImpl(12), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(9.15));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(9.30));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(9.45));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(10.96));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(12.47));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(11.07));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(8.57));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5.67));
        speedTable.put(new KnotSpeedImpl(14), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(9.45));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(9.61));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(9.77));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(11.49));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(13.21));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(12.19));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(9.13));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(5.86));
        speedTable.put(new KnotSpeedImpl(16), tableRow);

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagramBase.bearingComparator);
        tableRow.put(new DegreeBearingImpl(0), Speed.NULL);
        tableRow.put(new DegreeBearingImpl(cutAngle), new KnotSpeedImpl(1));
        tableRow.put(new DegreeBearingImpl(60), new KnotSpeedImpl(10.15));
        tableRow.put(new DegreeBearingImpl(75), new KnotSpeedImpl(10.33));
        tableRow.put(new DegreeBearingImpl(90), new KnotSpeedImpl(10.50));
        tableRow.put(new DegreeBearingImpl(105), new KnotSpeedImpl(12.32));
        tableRow.put(new DegreeBearingImpl(120), new KnotSpeedImpl(14.14));
        //tableRow.put(new DegreeBearingImpl(135), new KnotSpeedImpl(13.09));
        // tableRow.put(new DegreeBearingImpl(150), new KnotSpeedImpl(10.26));
        tableRow.put(new DegreeBearingImpl(180), new KnotSpeedImpl(6.3));
        speedTable.put(new KnotSpeedImpl(20), tableRow);

        beatAngles = new TreeMap<Speed, Bearing>();
        beatAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(43.0));
        beatAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(43.0));
        beatAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(43.0));
        beatAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(43.0));
        beatAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(44.0));
        beatAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(45.0));
        beatAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(45.6));
        beatAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(47.0));

        double beatScale = 1.0;
        beatSOG = new TreeMap<Speed, Speed>();
        beatSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
        beatSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(4.50 * beatScale));
        beatSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(6.25 * beatScale));
        beatSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(8.00 * beatScale));
        beatSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(8.50 * beatScale));
        beatSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(9.00 * beatScale));
        beatSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(9.30 * beatScale));
        beatSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(10.00 * beatScale));

        gybeAngles = new TreeMap<Speed, Bearing>();
        gybeAngles.put(new KnotSpeedImpl(0), new DegreeBearingImpl(135.0));
        gybeAngles.put(new KnotSpeedImpl(6), new DegreeBearingImpl(135.0));
        gybeAngles.put(new KnotSpeedImpl(8), new DegreeBearingImpl(140.0));
        gybeAngles.put(new KnotSpeedImpl(10), new DegreeBearingImpl(145.0));
        gybeAngles.put(new KnotSpeedImpl(12), new DegreeBearingImpl(150.0));
        gybeAngles.put(new KnotSpeedImpl(14), new DegreeBearingImpl(155.0));
        gybeAngles.put(new KnotSpeedImpl(16), new DegreeBearingImpl(157.0));
        gybeAngles.put(new KnotSpeedImpl(20), new DegreeBearingImpl(160.0));

        gybeSOG = new TreeMap<Speed, Speed>();
        gybeSOG.put(new KnotSpeedImpl(0), new KnotSpeedImpl(0));
        gybeSOG.put(new KnotSpeedImpl(6), new KnotSpeedImpl(8.00));
        gybeSOG.put(new KnotSpeedImpl(8), new KnotSpeedImpl(11.00));
        gybeSOG.put(new KnotSpeedImpl(10), new KnotSpeedImpl(14.00));
        gybeSOG.put(new KnotSpeedImpl(12), new KnotSpeedImpl(15.00));
        gybeSOG.put(new KnotSpeedImpl(14), new KnotSpeedImpl(16.00));
        gybeSOG.put(new KnotSpeedImpl(16), new KnotSpeedImpl(17.00));
        gybeSOG.put(new KnotSpeedImpl(20), new KnotSpeedImpl(19.00));

        for (Speed s : speedTable.keySet()) {

            if (beatAngles.containsKey(s) && !speedTable.get(s).containsKey(beatAngles.get(s)))
                speedTable.get(s).put(beatAngles.get(s), beatSOG.get(s));

            if (gybeAngles.containsKey(s) && !speedTable.get(s).containsKey(gybeAngles.get(s)))
                speedTable.get(s).put(gybeAngles.get(s), gybeSOG.get(s));

        }

    }

}
