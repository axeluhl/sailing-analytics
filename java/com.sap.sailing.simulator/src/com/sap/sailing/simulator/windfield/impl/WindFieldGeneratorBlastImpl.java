package com.sap.sailing.simulator.windfield.impl;

import java.util.logging.Logger;

import umontreal.iro.lecuyer.randvar.GeometricGen;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.LFSR113;
import umontreal.iro.lecuyer.rng.MRG32k3a;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class WindFieldGeneratorBlastImpl extends WindFieldGeneratorImpl implements WindFieldGenerator {

    private SpeedWithBearing[][] speedWithBearing;

    private double blastSizeProbability = 70;
    private double blastEdgeProbability = 30;
    private double blastBearingMean = 0;
    private double blastBearingVar = 8;
    private double defaultWindSpeed = 0;
    private double defaultWindBearing = 0;
    private SpeedWithBearing defaultSpeedWithBearing;
    /**
     * Number of time units before we cycle through the gusts
     */
    public final int defaultTimeUnits = 20;
    private int timeUnits;

    private static Logger logger = Logger.getLogger(WindFieldGeneratorBlastImpl.class.getName());

    public WindFieldGeneratorBlastImpl(Boundary boundary, WindControlParameters windParameters) {
        super(boundary, windParameters);
    }

    @Override
    public void generate(TimePoint start, TimePoint end, TimePoint step) {
        generate(start, end, step, windParameters.baseWindSpeed, windParameters.baseWindBearing);
    }

    protected void generate(TimePoint start, TimePoint end, TimePoint step, double defaultSpeed, double defaultBearing) {
        super.generate(start, end, step);
        // TODO Check the defaults
        setDefaultWindSpeed(defaultSpeed);
        setDefaultWindBearing(defaultBearing);
        defaultSpeedWithBearing = new KnotSpeedWithBearingImpl(defaultWindSpeed, new DegreeBearingImpl(
                defaultWindBearing));
        initializeSpeedWithBearing();
    }

    private void initializeSpeedWithBearing() {
        if (positions == null || positions.length < 1) {
            return;
        }

        int nrow = positions.length;
        int ncol = positions[0].length;

        timeUnits = nrow + defaultTimeUnits;
        if (startTime != null && endTime != null) {
            timeUnits = (int) ((endTime.asMillis() - startTime.asMillis()) / timeStep.asMillis()) + nrow;
            logger.info("Generating blasts for " + timeUnits + " rows & " + ncol + " columns");
        }
        speedWithBearing = new KnotSpeedWithBearingImpl[timeUnits][ncol];

        //System.out.println("Blast Wind:");
        for (int i = 0; i < timeUnits; ++i) {
            for (int j = 0; j < ncol; ++j) {
                if (isBlastSeed()) {
                    logger.fine("["+i+"]["+j+"] is  a blast seed");
                    double blastSpeed = getBlastSpeed();
                    double blastAngle = getBlastAngle();
                    SpeedWithBearing blastSpeedWithBearing = new KnotSpeedWithBearingImpl(blastSpeed,
                            new DegreeBearingImpl(blastAngle+defaultWindBearing));
                    growBlast(i, j, blastSpeedWithBearing);

                } else {
                    if (speedWithBearing[i][j] == null) {
                        speedWithBearing[i][j] = defaultSpeedWithBearing;
                    }
                }
                //System.out.println("speed: "+speedWithBearing[i][j].getMetersPerSecond()+" angle: "+speedWithBearing[i][j].getBearing().getDegrees());
            }
        }
    }

    private void growBlast(int rowIndex, int colIndex, SpeedWithBearing blastSpeedWithBearing) {

        if (speedWithBearing[rowIndex][colIndex] != null) {
            return;
        }

        int blastSize = (int) Math.min(getBlastSize(), windParameters.maxBlastSize);
        logger.fine("Blast Size:" + blastSize);
        int nrow = timeUnits;
        int ncol = positions[0].length;
        int hSpanStart = Math.max(0, colIndex - blastSize / 2);
        int hSpanEnd = Math.min(colIndex + blastSize - blastSize / 2, ncol - 1);
        int vSpan = Math.min(rowIndex + blastSize, nrow - 1);
        for (int i = rowIndex; i <= vSpan; ++i) {
            for (int j = hSpanStart; j <= hSpanEnd; ++j) {
                speedWithBearing[i][j] = blastSpeedWithBearing;
            }
        }

        if (hSpanEnd - hSpanStart > 1) {
            for (int i = rowIndex; i <= vSpan; ++i) {
                if (isBlastCell()) {
                    speedWithBearing[i][hSpanStart] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[i][hSpanStart] = defaultSpeedWithBearing;
                }
                if (isBlastCell()) {
                    speedWithBearing[i][hSpanEnd] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[i][hSpanEnd] = defaultSpeedWithBearing;
                }
            }
            for (int j = hSpanStart + 1; j <= hSpanEnd - 1; ++j) {
                if (isBlastCell()) {
                    speedWithBearing[rowIndex][j] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[rowIndex][j] = defaultSpeedWithBearing;
                }
                if (isBlastCell()) {
                    speedWithBearing[vSpan][j] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[vSpan][j] = defaultSpeedWithBearing;
                }
            }

        }
    }

    private boolean isBlastSeed() {
        return UniformGen.nextDouble(new LFSR113("BlastSeedStream"), 0, 1) < windParameters.blastProbability / 100.0;
    }

    private boolean isBlastCell() {
        return UniformGen.nextDouble(new LFSR113("BlastCellStream"), 0, 1) > this.blastEdgeProbability / 100.0;
    }

    private double getBlastSpeed() {
        double bSpeedMean = windParameters.baseWindSpeed * (windParameters.blastWindSpeed / 100.0 - (defaultWindSpeed==0 ? 1. : 0.));
        double bSpeedVar = windParameters.baseWindSpeed * windParameters.blastWindSpeed / 100.0 * windParameters.blastWindSpeedVar / 100.0;
        //System.out.println("blast par speed: "+windParameters.blastWindSpeed+" var: "+windParameters.blastWindSpeedVar);
        //System.out.println("blast speed mean: "+bSpeedMean+" var: "+bSpeedVar);
        return NormalGen.nextDouble(new LFSR113("BlastSpeedStream"), bSpeedMean, bSpeedVar);

    }

    private int getBlastSize() {
        return GeometricGen.nextInt(new MRG32k3a("BlastSizeStream"), blastSizeProbability / 100.0);
    }

    private double getBlastAngle() {
        return NormalGen.nextDouble(new LFSR113("BlastAngleStream"), blastBearingMean, blastBearingVar);

    }

    private SpeedWithBearing getSpeedWithBearing(TimedPosition timedPosition) {
        Position p = timedPosition.getPosition();
        Pair<Integer, Integer> positionIndex = getPositionIndex(p);
        if (positionIndex != null) {
            int rowIndex = positionIndex.getA();
            int colIndex = positionIndex.getB();
            int timeIndex = 0;
            if (timedPosition.getTimePoint() != null) {
                timeIndex = getTimeIndex(timedPosition.getTimePoint());
            }
            return speedWithBearing[(rowIndex + timeIndex) % timeUnits][colIndex];
        } else {
            logger.severe("Error finding position " + p);
        }
        return null;
    }

    @Override
    public Wind getWind(TimedPosition timedPosition) {

        return new WindImpl(timedPosition.getPosition(), timedPosition.getTimePoint(),
                getSpeedWithBearing(timedPosition));

    }

    public void setDefaultWindSpeed(double speed) {
        this.defaultWindSpeed = speed;
    }

    public double getDefaultWindSpeed() {
        return this.defaultWindSpeed;
    }

    public void setDefaultWindBearing(double bearing) {
        this.defaultWindBearing = bearing;
    }

    public double getDefaultWindBearing() {
        return this.defaultWindBearing;
    }
}
