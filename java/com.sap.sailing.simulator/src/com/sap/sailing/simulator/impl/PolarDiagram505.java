package com.sap.sailing.simulator.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.simulator.PolarDiagram;

public class PolarDiagram505 implements PolarDiagram {

    // the current speed and direction of the wind
    private SpeedWithBearing wind = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(180));

    // the preferred direction of movement
    // is used by optimalDirectionsUpwind() and optimialDirectionsDownwind()
    private Bearing targetDirection = new DegreeBearingImpl(0);

    private NavigableMap<Speed, NavigableMap<Bearing, Speed>> speedTable;
    private NavigableMap<Speed, Bearing> beatAngles;
    private NavigableMap<Speed, Bearing> gybeAngles;
    private NavigableMap<Speed, Speed> beatSOG;
    private NavigableMap<Speed, Speed> gybeSOG;

    // this constructor creates an instance with a hard-coded set of values
    public PolarDiagram505() {
        speedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        NavigableMap<Bearing, Speed> tableRow;

        double cutAngle = 35.0;

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

        tableRow = new TreeMap<Bearing, Speed>(PolarDiagram505.bearingComparator);
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

    // a constructor that allows a generic set of parameters
    public PolarDiagram505(NavigableMap<Speed, NavigableMap<Bearing, Speed>> speeds,
            NavigableMap<Speed, Bearing> beats, NavigableMap<Speed, Bearing> gybes,
            NavigableMap<Speed, Speed> beatSOGs, NavigableMap<Speed, Speed> gybeSOGs) {

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
        if ((Math.abs(relativeBearing.getDegrees()) < 20) || (Math.abs(relativeBearing.getDegrees()) > 330)) {
            speed = 0.1;
        }

        return new KnotSpeedWithBearingImpl(speed, bearing);
    }

    @Override
    public Bearing[] optimalDirectionsUpwind() {
        Bearing windBearing = wind.getBearing().reverse();
        Bearing estBeatAngleRight = null;
        Bearing estBeatAngleLeft = null;
        if (targetDirection.equals(new DegreeBearingImpl(0))) {
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
            estBeatAngleRight = new RadianBearingImpl(+beatAngle);
            estBeatAngleLeft = new RadianBearingImpl(-beatAngle);
            return new Bearing[] {
                    // windBearing.add(estBeatAngle),
                    // windBearing.add(estBeatAngle.getDifferenceTo(windBearing))
                    windBearing.add(estBeatAngleLeft), windBearing.add(estBeatAngleRight) };
        } else {
            Set<Bearing> allKeys = new TreeSet<Bearing>(bearingComparator);
            for (Double b = 0.0; b < 360.0; b += 5.0)
                allKeys.add(new DegreeBearingImpl(b));
            Bearing _targetDirection = targetDirection;
            setTargetDirection(new DegreeBearingImpl(0.0));
            allKeys.addAll(Arrays.asList(optimalDirectionsUpwind()));
            allKeys.addAll(Arrays.asList(optimalDirectionsDownwind()));
            setTargetDirection(_targetDirection);
            Double maxSpeedRight = 0.0;
            Double maxSpeedLeft = 0.0;
            for (Bearing b : allKeys) {
                if (b.getDifferenceTo(getWind().getBearing()).getDegrees() > 0) {
                    if (getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians()) > maxSpeedRight) {
                        maxSpeedRight = getSpeedAtBearing(b).getKnots()
                                * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians());
                        estBeatAngleRight = b;
                    }
                } else if (getSpeedAtBearing(b).getKnots()
                        * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians()) > maxSpeedLeft) {
                    maxSpeedLeft = getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians());
                    estBeatAngleLeft = b;
                }
            }
            return new Bearing[] {
                    // windBearing.add(estBeatAngle),
                    // windBearing.add(estBeatAngle.getDifferenceTo(windBearing))
                    estBeatAngleLeft, estBeatAngleRight };
        }

    }

    @Override
    public Bearing[] optimalDirectionsDownwind() {

        Bearing windBearing = wind.getBearing().reverse();
        Bearing estGybeAngleRight = null;
        Bearing estGybeAngleLeft = null;
        if (getTargetDirection().equals(new DegreeBearingImpl(0))) {
            windBearing = wind.getBearing().reverse();
            estGybeAngleRight = null;
            estGybeAngleLeft = null;
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
            // Bearing estGybeAngle = new RadianBearingImpl(gybeAngle);
            estGybeAngleRight = new RadianBearingImpl(+gybeAngle);
            estGybeAngleLeft = new RadianBearingImpl(-gybeAngle);
            return new Bearing[] { windBearing.add(estGybeAngleRight), windBearing.add(estGybeAngleLeft) };
        } else {

            Set<Bearing> allKeys = new TreeSet<Bearing>(bearingComparator);
            for (Double b = 0.0; b < 360.0; b += 5.0)
                allKeys.add(new DegreeBearingImpl(b));
            Bearing _targetDirection = targetDirection;
            setTargetDirection(new DegreeBearingImpl(0.0));
            allKeys.addAll(Arrays.asList(optimalDirectionsUpwind()));
            allKeys.addAll(Arrays.asList(optimalDirectionsDownwind()));
            setTargetDirection(_targetDirection);
            Double maxSpeedRight = 0.0;
            Double maxSpeedLeft = 0.0;
            for (Bearing b : allKeys) {
                if (b.getDifferenceTo(getWind().getBearing()).getDegrees() > 0) {
                    if (getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians()) > maxSpeedRight) {
                        maxSpeedRight = getSpeedAtBearing(b).getKnots()
                                * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians());
                        estGybeAngleRight = b;
                    }
                } else if (getSpeedAtBearing(b).getKnots()
                        * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians()) > maxSpeedLeft) {
                    maxSpeedLeft = getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians());
                    estGybeAngleLeft = b;
                }
            }
            return new Bearing[] {
                    // windBearing.add(estBeatAngle),
                    // windBearing.add(estBeatAngle.getDifferenceTo(windBearing))
                    estGybeAngleLeft, estGybeAngleRight };
        }
    }

    // a Bearing Comparator useful in the creation of sorted sets of Bearing
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

    // TO BE REVIEWED
    // not sure I use the right terms and conventions
    @Override
    public WindSide getWindSide(Bearing bearing) {
        WindSide windSide = null;
        if (bearingComparator.compare(bearing, wind.getBearing().reverse()) > 0)
            windSide = WindSide.LEFT;
        if (bearingComparator.compare(bearing, wind.getBearing().reverse()) < 0)
            windSide = WindSide.RIGHT;
        if (bearing.equals(wind.getBearing()))
            windSide = WindSide.OPPOSING;
        if (bearing.equals(wind.getBearing().reverse()))
            windSide = WindSide.FACING;

        return windSide;
    }

    // returns a table of Bearing-Speed pairs with a bearingStep granularity
    // for all Speeds in speedTable
    @Override
    public NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep) {

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> table = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
        Set<Bearing> extraBearings = new HashSet<Bearing>();

        for (Speed s : speedTable.keySet()) {
            setWind(new KnotSpeedWithBearingImpl(s.getKnots(), new DegreeBearingImpl(135)));
            extraBearings.addAll(Arrays.asList(optimalDirectionsUpwind()));
            extraBearings.addAll(Arrays.asList(optimalDirectionsDownwind()));
        }

        for (Speed s : speedTable.keySet()) {
            setWind(new KnotSpeedWithBearingImpl(s.getKnots(), new DegreeBearingImpl(135)));
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
        return targetDirection;
    }

    @Override
    public void setTargetDirection(Bearing newTargetDirection) {
        targetDirection = newTargetDirection;
    }

}
