package com.sap.sailing.simulator.impl;

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
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindFieldGenerator;
import com.sap.sailing.simulator.impl.WindFieldGeneratorImpl;

public class WindFieldGeneratorBlastImpl extends WindFieldGeneratorImpl implements WindFieldGenerator {

    private SpeedWithBearing[][] speedWithBearing;

    private double blastSizeProbability = 70;
    private double blastEdgeProbability = 30;
    private double blastBearingMean = 0;
    private double blastBearingVar = 8;
    private double defaultWindSpeed = 0.0;
    private double defaultWindBearing = 0.0;
    
    public WindFieldGeneratorBlastImpl(Boundary boundary, WindControlParameters windParameters) {
        super(boundary, windParameters); 
    }

    @Override
    public void generate(TimePoint start, TimePoint end, TimePoint step) {
        super.generate(start, end, step);
        defaultWindSpeed = windParameters.baseWindSpeed;
        defaultWindBearing = windParameters.baseWindBearing;
        initializeSpeedWithBearing();
    }

    private void initializeSpeedWithBearing() {
        if (positions == null || positions.length < 1) {
            return;
        }

        int nrow = positions.length;
        int ncol = positions[0].length;
        speedWithBearing = new KnotSpeedWithBearingImpl[nrow][ncol];

        for (int i = 0; i < nrow; ++i) {
            for (int j = 0; j < ncol; ++j) {
                if (isBlastSeed()) {
                    double blastSpeed = getBlastSpeed();
                    double blastAngle = getBlastAngle();
                    SpeedWithBearing blastSpeedWithBearing = new KnotSpeedWithBearingImpl(blastSpeed,
                        new DegreeBearingImpl(blastAngle));
                    growBlast(i, j, blastSpeedWithBearing);
                } else {
                    speedWithBearing[i][j] = new KnotSpeedWithBearingImpl(defaultWindSpeed,
                            new DegreeBearingImpl(defaultWindBearing));
                }
            }
        }
    }

    private void growBlast(int rowIndex, int colIndex, SpeedWithBearing blastSpeedWithBearing) {
       
        if (speedWithBearing[rowIndex][colIndex] != null) {
            return;
        }
        
        int blastSize = (int) Math.min(getBlastSize(), windParameters.maxBlastSize);
        logger.info("Blast Size:"+ blastSize);
        int nrow = positions.length;
        int ncol = positions[0].length;
        int hSpanStart = Math.max(0, colIndex-blastSize/2);
        int hSpanEnd =  Math.min(colIndex+blastSize-blastSize/2, ncol-1);
        int vSpan = Math.min(rowIndex+blastSize, nrow-1);
        for (int i = rowIndex; i <= vSpan; ++i) {
            for (int j = hSpanStart; j <= hSpanEnd; ++j) {
                speedWithBearing[i][j] = blastSpeedWithBearing;
            }
        }
        
        if (hSpanEnd-hSpanStart > 1) {
            for(int i = rowIndex; i <= vSpan; ++i) {
                if (isBlastCell()) {
                    speedWithBearing[i][hSpanStart] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[i][hSpanStart] = new KnotSpeedWithBearingImpl(defaultWindSpeed,
                            new DegreeBearingImpl(defaultWindBearing));
                }
                if (isBlastCell()) {
                    speedWithBearing[i][hSpanEnd] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[i][hSpanEnd] = new KnotSpeedWithBearingImpl(defaultWindSpeed,
                            new DegreeBearingImpl(defaultWindBearing));
                } 
            }
            for(int j = hSpanStart+1; j <= hSpanEnd-1;++j) {
                if (isBlastCell()) {
                    speedWithBearing[rowIndex][j] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[rowIndex][j] = new KnotSpeedWithBearingImpl(defaultWindSpeed,
                            new DegreeBearingImpl(defaultWindBearing));
                }
                if (isBlastCell()) {
                    speedWithBearing[vSpan][j] = blastSpeedWithBearing;
                } else {
                    speedWithBearing[vSpan][j] = new KnotSpeedWithBearingImpl(defaultWindSpeed,
                            new DegreeBearingImpl(defaultWindBearing));
                }
            }
            
                
        }
    }

    private boolean isBlastSeed() {
        return UniformGen.nextDouble(new LFSR113("BlastSeedStream"), 0, 1) < windParameters.blastProbability/100.0;
    }

    private boolean isBlastCell() {
        return UniformGen.nextDouble(new LFSR113("BlastCellStream"), 0, 1) > this.blastEdgeProbability/100.0;
    }
    
    private double getBlastSpeed() {
        return Math.abs(NormalGen.nextDouble(new LFSR113("BlastSpeedStream"), windParameters.baseWindSpeed
                * windParameters.blastWindSpeed / 100.0,
                windParameters.baseWindSpeed * Math.sqrt(windParameters.blastWindSpeedVar / 100.0)));

    }

    private int getBlastSize() {
        return GeometricGen.nextInt(new MRG32k3a("BlastSizeStream"), blastSizeProbability/100.0);
    }
    
    private double getBlastAngle() {
        return NormalGen.nextDouble(new LFSR113("BlastAngleStream"), blastBearingMean, Math.sqrt(blastBearingVar));

    }

    private SpeedWithBearing getSpeedWithBearing(TimedPosition timedPosition) {
        Position p = timedPosition.getPosition();
        Pair<Integer, Integer> positionIndex = getPositionIndex(p);
        if (positionIndex != null) {
            int rowIndex = positionIndex.getA();
            int colIndex = positionIndex.getB();
            return speedWithBearing[rowIndex][colIndex];
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
}
