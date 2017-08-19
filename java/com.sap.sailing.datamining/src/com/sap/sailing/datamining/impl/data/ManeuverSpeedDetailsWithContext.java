package com.sap.sailing.datamining.impl.data;

import java.util.function.Function;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasManeuverSpeedDetailsContext;
import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;
import com.sap.sailing.datamining.impl.components.ManeuverSpeedDetailsUtils;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;

public class ManeuverSpeedDetailsWithContext implements HasManeuverSpeedDetailsContext {
    
    private final HasManeuverContext maneuverContext;
    private final double[] maneuverSpeedPerTWA;
    private int maneuverEnteringTWA;
    private int maneuverExitingTWA;
    private final ManeuverSpeedDetailsSettings settings;

    public ManeuverSpeedDetailsWithContext(HasManeuverContext maneuverContext, double[] maneuverSpeedPerTWA, int enteringTWA, int exitingTWA, ManeuverSpeedDetailsSettings settings) {
        this.maneuverContext = maneuverContext;
        this.maneuverSpeedPerTWA = maneuverSpeedPerTWA;
        this.maneuverEnteringTWA = enteringTWA;
        this.maneuverExitingTWA = exitingTWA;
        this.settings = settings;
    }

    @Override
    public HasManeuverContext getManeuverContext() {
        return maneuverContext;
    }

    @Override
    public ManeuverSpeedDetailsStatistic getSpeedSlopeStatistic() {
        double[] speedSlopePerTWA = new double[360];
        double lastSpeedValue = 0;
        
        Function<Integer, Integer> forNextTWA = ManeuverSpeedDetailsUtils.getNextTWAFunctionForManeuverDirection(maneuverContext.getToSide(), settings);
        
        for(int twa = maneuverEnteringTWA, i = 0; i < 360; ++i, twa = forNextTWA.apply(twa)) {
            if(maneuverSpeedPerTWA[twa] == 0 || lastSpeedValue == 0) {
                speedSlopePerTWA[twa] = 0;
            } else {
                speedSlopePerTWA[twa] = maneuverSpeedPerTWA[twa] - lastSpeedValue;
            }
            lastSpeedValue = maneuverSpeedPerTWA[twa];
        }
        return new ManeuverSpeedDetailsStatisticImpl(speedSlopePerTWA);
    }
    
    @Override
    public ManeuverSpeedDetailsStatistic getRatioToInitialSpeedStatistic() {
        double[] speedRatioToBeginningSpeedPerTWA = new double[360];
        double firstSpeedValue = 0;
        
        Function<Integer, Integer> forNextTWA = ManeuverSpeedDetailsUtils.getNextTWAFunctionForManeuverDirection(maneuverContext.getToSide(), settings);
        
        for(int twa = maneuverEnteringTWA, i = 0; i < 360; ++i, twa = forNextTWA.apply(twa)) {
            if(firstSpeedValue == 0) {
                firstSpeedValue = maneuverSpeedPerTWA[twa];
            }
            if(maneuverSpeedPerTWA[twa] == 0 || firstSpeedValue == 0) {
                speedRatioToBeginningSpeedPerTWA[twa] = 0;
            } else {
                speedRatioToBeginningSpeedPerTWA[twa] = maneuverSpeedPerTWA[twa] / firstSpeedValue;
            }
        }
        return new ManeuverSpeedDetailsStatisticImpl(speedRatioToBeginningSpeedPerTWA);
    }

    @Override
    public ManeuverSpeedDetailsStatistic getRatioToPreviousTWAStatistic() {
        double[] speedRatioToPreviousSpeedPerTWA = new double[360];
        double lastSpeedValue = 0;
        
        Function<Integer, Integer> forNextTWA = ManeuverSpeedDetailsUtils.getNextTWAFunctionForManeuverDirection(maneuverContext.getToSide(), settings);
        
        for(int twa = maneuverEnteringTWA, i = 0; i < 360; ++i, twa = forNextTWA.apply(twa)) {
            if(maneuverSpeedPerTWA[twa] == 0 || lastSpeedValue == 0) {
                speedRatioToPreviousSpeedPerTWA[twa] = 0;
            } else {
                speedRatioToPreviousSpeedPerTWA[twa] = maneuverSpeedPerTWA[twa] / lastSpeedValue;
            }
            lastSpeedValue = maneuverSpeedPerTWA[twa];
        }
        return new ManeuverSpeedDetailsStatisticImpl(speedRatioToPreviousSpeedPerTWA);
    }

    @Override
    public Double getLowestRatioToInitialSpeedStatistic() {
        double[] speedRatioPerTWA = getRatioToInitialSpeedStatistic().getManeuverValuePerTWA();
        double lowestSpeedRatio = 1;
        for (double speedRatio : speedRatioPerTWA) {
            if(speedRatio != 0) {
                if(speedRatio < lowestSpeedRatio) {
                    lowestSpeedRatio = speedRatio;
                }
            }
        }
        return lowestSpeedRatio;
    }
    
    @Override
    public Double getHighestRatioToInitialSpeedStatistic() {
        double[] speedRatioPerTWA = getRatioToInitialSpeedStatistic().getManeuverValuePerTWA();
        double highestSpeedRatio = 1;
        for (double speedRatio : speedRatioPerTWA) {
            if(speedRatio != 0) {
                if(speedRatio > highestSpeedRatio) {
                    highestSpeedRatio = speedRatio;
                }
            }
        }
        return highestSpeedRatio;
    }
    
    @Override
    public Double getHighestRatioToInitialSpeedMinusLowestStatistic() {
        double[] speedRatioPerTWA = getRatioToInitialSpeedStatistic().getManeuverValuePerTWA();
        double highestSpeedRatio = 1;
        double lowestSpeedRatio = 1;
        for (double speedRatio : speedRatioPerTWA) {
            if(speedRatio != 0) {
                if(speedRatio > highestSpeedRatio) {
                    highestSpeedRatio = speedRatio;
                }
                if(speedRatio < lowestSpeedRatio) {
                    lowestSpeedRatio = speedRatio;
                }
            }
        }
        return highestSpeedRatio - lowestSpeedRatio;
    }

    @Override
    public Double getEnteringManeuverSpeedMinusExitingSpeedStatistic() {
        return maneuverSpeedPerTWA[maneuverEnteringTWA] - maneuverSpeedPerTWA[maneuverExitingTWA];
    }
    
    @Override
    public Double getRatioBetweenInitialAndFinalManeuverSpeedStatistic() {
        return maneuverSpeedPerTWA[maneuverEnteringTWA] / maneuverSpeedPerTWA[maneuverExitingTWA];
    }

}

