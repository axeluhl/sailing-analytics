package com.sap.sailing.simulator.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.simulator.PolarDiagram;

public class PolarDiagramBase implements PolarDiagram {

    // the current speed and direction of the wind
    protected SpeedWithBearing wind = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(180));
    //private static Logger logger = Logger.getLogger("com.sap.sailing");
    // the preferred direction of movement
    // is used by optimalDirectionsUpwind() and optimialDirectionsDownwind()
    protected Bearing targetDirection = new DegreeBearingImpl(0);

    protected NavigableMap<Speed, NavigableMap<Bearing, Speed>> speedTable;
    protected NavigableMap<Speed, Bearing> beatAngles;
    protected NavigableMap<Speed, Bearing> gybeAngles;
    protected NavigableMap<Speed, Speed> beatSOG;
    protected NavigableMap<Speed, Speed> gybeSOG;

    // this constructor creates an instance with a hard-coded set of values
    public PolarDiagramBase() {
        // do nothing
    }

    // a constructor that allows a generic set of parameters
    public PolarDiagramBase(NavigableMap<Speed, NavigableMap<Bearing, Speed>> speeds, NavigableMap<Speed, Bearing> beats,
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
                    * (floorSpeed2.getKnots() - floorSpeed1.getKnots()) / (floorBearing2.getRadians() - floorBearing1.getRadians());
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
                    * (ceilingSpeed2.getKnots() - ceilingSpeed1.getKnots()) / (ceilingBearing2.getRadians() - ceilingBearing1.getRadians());
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
                        * (ceilingBeatAngle.getRadians() - floorBeatAngle.getRadians()) / (ceilingSpeed.getKnots() - floorSpeed.getKnots());
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
                    double currentSpeedRight = getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians());
                    if (currentSpeedRight > maxSpeedRight) {
                        maxSpeedRight = currentSpeedRight;
                        estBeatAngleRight = b;
                    }
                } else {
                    double currentSpeedLeft = getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians());
                    if (currentSpeedLeft > maxSpeedLeft) {
                        maxSpeedLeft = currentSpeedLeft;
                        estBeatAngleLeft = b;
                    }
                }
            }
            return new Bearing[] {
                    // windBearing.add(estBeatAngle),
                    // windBearing.add(estBeatAngle.getDifferenceTo(windBearing))
                    estBeatAngleLeft, estBeatAngleRight };
        }

    }

    @Override
    public SpeedWithBearing[] optimalVMGUpwind() {

        Bearing windBearing = wind.getBearing().reverse();
        Bearing estBeatAngleRight = null;
        Bearing estBeatAngleLeft = null;

        Bearing diffWindTarget = windBearing.getDifferenceTo(targetDirection);
        if (diffWindTarget.equals(new DegreeBearingImpl(0))) {
            //
            // target is aligned with wind, i.e. target bearing = 0°
            //
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
                        * (ceilingBeatAngle.getRadians() - floorBeatAngle.getRadians()) / (ceilingSpeed.getKnots() - floorSpeed.getKnots());
            }
            estBeatAngleRight = new RadianBearingImpl(+beatAngle);
            estBeatAngleLeft = new RadianBearingImpl(-beatAngle);

            double speedLeft = this.getSpeedAtBearing(estBeatAngleLeft).getKnots() * Math.cos(estBeatAngleLeft.getRadians());
            double speedRight = this.getSpeedAtBearing(estBeatAngleRight).getKnots() * Math.cos(estBeatAngleRight.getRadians());
            SpeedWithBearing optVMGLeft = new KnotSpeedWithBearingImpl(speedLeft, windBearing.add(estBeatAngleLeft));
            SpeedWithBearing optVMGRight = new KnotSpeedWithBearingImpl(speedRight, windBearing.add(estBeatAngleRight));
            return new SpeedWithBearing[] { optVMGLeft, optVMGRight };

        } else {
            //
            // target is not aligned with wind, i.e. target bearing != 0°
            //
            Set<Bearing> allKeys = new TreeSet<Bearing>(bearingComparator);
            /*
             * for (Double b = 0.0; b < 360.0; b += 5.0) allKeys.add(new DegreeBearingImpl(b));
             */
            Bearing _targetDirection = targetDirection;
            setTargetDirection(new DegreeBearingImpl(0.0));
            Bearing[] optDirectionsUpwind = optimalDirectionsUpwind();

            allKeys.addAll(Arrays.asList(optDirectionsUpwind));
            for (int idx = 0; idx < optDirectionsUpwind.length; idx++) {
                for (int offset = 1; offset <= 5; offset++) {
                    allKeys.add(new DegreeBearingImpl(optDirectionsUpwind[idx].getDegrees() + offset));
                    allKeys.add(new DegreeBearingImpl(optDirectionsUpwind[idx].getDegrees() - offset));
                }
            }
            allKeys.addAll(Arrays.asList(optDirectionsUpwind));
            allKeys.addAll(Arrays.asList(optimalDirectionsDownwind()));
            setTargetDirection(_targetDirection);
            Double maxSpeedRight = 0.0;
            Double maxSpeedLeft = 0.0;
            for (Bearing b : allKeys) {
                if (b.getDifferenceTo(getWind().getBearing()).getDegrees() > 0) {
                    double currentSpeedRight = getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians());
                    if (currentSpeedRight > maxSpeedRight) {
                        maxSpeedRight = currentSpeedRight;
                        estBeatAngleRight = b;
                    }
                } else {
                    double currentSpeedLeft = getSpeedAtBearing(b).getKnots()
                            * Math.cos(b.getDifferenceTo(getTargetDirection()).getRadians());
                    if (currentSpeedLeft > maxSpeedLeft) {
                        maxSpeedLeft = currentSpeedLeft;
                        estBeatAngleLeft = b;
                    }
                }
            }
            SpeedWithBearing optVMGLeft = new KnotSpeedWithBearingImpl(maxSpeedLeft, estBeatAngleLeft);
            SpeedWithBearing optVMGRight = new KnotSpeedWithBearingImpl(maxSpeedRight, estBeatAngleRight);
            return new SpeedWithBearing[] { optVMGLeft, optVMGRight };
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
                        * (ceilingGybeAngle.getRadians() - floorGybeAngle.getRadians()) / (ceilingSpeed.getKnots() - floorSpeed.getKnots());
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
                    if (getSpeedAtBearing(b).getKnots() * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians()) > maxSpeedRight) {
                        maxSpeedRight = getSpeedAtBearing(b).getKnots()
                                * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians());
                        estGybeAngleRight = b;
                    }
                } else if (getSpeedAtBearing(b).getKnots() * Math.cos(b.getDifferenceTo(getTargetDirection().reverse()).getRadians()) > maxSpeedLeft) {
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
    public NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep, Set<Speed> extraSpeeds) {

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> table = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();

        Set<Speed> speedSet = new TreeSet<Speed>(); 
        speedSet.addAll(speedTable.keySet());
        if (extraSpeeds != null) {
            speedSet.addAll(extraSpeeds);
        }
        for (Speed s : speedSet) {
            setWind(new KnotSpeedWithBearingImpl(s.getKnots(), new DegreeBearingImpl(180)));
            NavigableMap<Bearing, Speed> currentTable = new TreeMap<Bearing, Speed>(bearingComparator);
            table.put(s, currentTable);

            for (Double b = 0.0; b < 360.0; b += bearingStep) {
                Bearing bearing = new DegreeBearingImpl(b);
                currentTable.put(bearing, getSpeedAtBearing(bearing));
            }
        }

        return table;
    }
    @Override
    public NavigableMap<Speed, NavigableMap<Bearing, Speed>> polarDiagramPlot(Double bearingStep) {

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
                //DEBUG
                //logger.info("bearing="+bearing.getDegrees());
            }
            //for (Bearing extraBearing : extraBearings)
            	//currentTable.put(extraBearing, getSpeedAtBearing(extraBearing));
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
