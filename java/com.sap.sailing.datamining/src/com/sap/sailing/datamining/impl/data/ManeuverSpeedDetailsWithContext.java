package com.sap.sailing.datamining.impl.data;

import java.util.function.Function;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasManeuverSpeedDetailsContext;
import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;
import com.sap.sailing.datamining.impl.components.ManeuverSpeedDetailsUtils;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsWithContext implements HasManeuverSpeedDetailsContext {

    private final HasManeuverContext maneuverContext;
    private final double[] maneuverSpeedPerTWA;
    private int maneuverEnteringTWA;
    private final ManeuverSpeedDetailsSettings settings;

    public ManeuverSpeedDetailsWithContext(HasManeuverContext maneuverContext, double[] maneuverSpeedPerTWA,
            int enteringTWA, ManeuverSpeedDetailsSettings settings) {
        this.maneuverContext = maneuverContext;
        this.maneuverSpeedPerTWA = maneuverSpeedPerTWA;
        this.maneuverEnteringTWA = enteringTWA;
        this.settings = settings;
    }

    @Override
    public HasManeuverContext getManeuverContext() {
        return maneuverContext;
    }

    @Override
    public NauticalSide getToSide() {
        return maneuverContext.getManeuver().getToSide();
    }

    @Override
    public ManeuverSpeedDetailsStatistic getSpeedSlopeStatistic() {
        double[] speedSlopePerTWA = new double[360];
        double lastSpeedValue = 0;

        Function<Integer, Integer> twaIterationFunction = ManeuverSpeedDetailsUtils
                .getTWAIterationFunctionForManeuverDirection(getToSide());

        for (int twa = maneuverEnteringTWA, i = 0; i < 360; ++i, twa = twaIterationFunction.apply(twa)) {
            if (maneuverSpeedPerTWA[twa] == 0 || lastSpeedValue == 0) {
                speedSlopePerTWA[twa] = 0;
            } else {
                speedSlopePerTWA[twa] = maneuverSpeedPerTWA[twa] - lastSpeedValue;
            }
            lastSpeedValue = maneuverSpeedPerTWA[twa];
        }
        return new ManeuverSpeedDetailsStatisticImpl(speedSlopePerTWA, getToSide(), settings);
    }

    @Override
    public ManeuverSpeedDetailsStatistic getRatioToEnteringSpeedStatistic() {
        double[] speedRatioToBeginningSpeedPerTWA = new double[360];
        double firstSpeedValue = maneuverContext.getManeuverEnteringSpeed();

        Function<Integer, Integer> twaIterationFunction = ManeuverSpeedDetailsUtils
                .getTWAIterationFunctionForManeuverDirection(getToSide());

        for (int twa = maneuverEnteringTWA, i = 0; i < 360; ++i, twa = twaIterationFunction.apply(twa)) {
            if (firstSpeedValue == 0) {
                firstSpeedValue = maneuverSpeedPerTWA[twa];
            }
            if (maneuverSpeedPerTWA[twa] == 0 || firstSpeedValue == 0) {
                speedRatioToBeginningSpeedPerTWA[twa] = 0;
            } else {
                speedRatioToBeginningSpeedPerTWA[twa] = maneuverSpeedPerTWA[twa] / firstSpeedValue;
            }
        }
        return new ManeuverSpeedDetailsStatisticImpl(speedRatioToBeginningSpeedPerTWA, getToSide(), settings);
    }

    @Override
    public ManeuverSpeedDetailsStatistic getRatioToPreviousTWASpeedStatistic() {
        double[] speedRatioToPreviousSpeedPerTWA = new double[360];
        double lastSpeedValue = 0;

        Function<Integer, Integer> twaIterationFunction = ManeuverSpeedDetailsUtils
                .getTWAIterationFunctionForManeuverDirection(getToSide());

        for (int twa = maneuverEnteringTWA, i = 0; i < 360; ++i, twa = twaIterationFunction.apply(twa)) {
            if (maneuverSpeedPerTWA[twa] == 0 || lastSpeedValue == 0) {
                speedRatioToPreviousSpeedPerTWA[twa] = 0;
            } else {
                speedRatioToPreviousSpeedPerTWA[twa] = maneuverSpeedPerTWA[twa] / lastSpeedValue;
            }
            lastSpeedValue = maneuverSpeedPerTWA[twa];
        }
        return new ManeuverSpeedDetailsStatisticImpl(speedRatioToPreviousSpeedPerTWA, getToSide(), settings);
    }

    @Override
    public Double getLowestRatioToEnteringSpeedStatistic() {
        double[] speedRatioPerTWA = getRatioToEnteringSpeedStatistic().getManeuverValuePerTWA();
        double lowestSpeedRatio = 1;
        for (double speedRatio : speedRatioPerTWA) {
            if (speedRatio != 0) {
                if (speedRatio < lowestSpeedRatio) {
                    lowestSpeedRatio = speedRatio;
                }
            }
        }
        return lowestSpeedRatio;
    }

    @Override
    public Double getHighestRatioToEnteringSpeedStatistic() {
        double[] speedRatioPerTWA = getRatioToEnteringSpeedStatistic().getManeuverValuePerTWA();
        double highestSpeedRatio = 1;
        for (double speedRatio : speedRatioPerTWA) {
            if (speedRatio != 0) {
                if (speedRatio > highestSpeedRatio) {
                    highestSpeedRatio = speedRatio;
                }
            }
        }
        return highestSpeedRatio;
    }

    @Override
    public Double getHighestRatioToEnteringSpeedMinusLowestStatistic() {
        double[] speedRatioPerTWA = getRatioToEnteringSpeedStatistic().getManeuverValuePerTWA();
        double highestSpeedRatio = 1;
        double lowestSpeedRatio = 1;
        for (double speedRatio : speedRatioPerTWA) {
            if (speedRatio != 0) {
                if (speedRatio > highestSpeedRatio) {
                    highestSpeedRatio = speedRatio;
                }
                if (speedRatio < lowestSpeedRatio) {
                    lowestSpeedRatio = speedRatio;
                }
            }
        }
        return highestSpeedRatio - lowestSpeedRatio;
    }

    @Override
    public Double getAbsTwaAtLowestRatioToEnteringSpeedStatistic() {
        double[] speedRatioPerTWA = getRatioToEnteringSpeedStatistic().getManeuverValuePerTWA();
        double lowestSpeedRatio = 1;
        double twaAtLowestSpeedRatio = -1;
        for (int encodedTwa = 0; encodedTwa < 360; encodedTwa++) {
            double speedRatio = speedRatioPerTWA[encodedTwa];
            if (speedRatio != 0) {
                if (speedRatio < lowestSpeedRatio) {
                    lowestSpeedRatio = speedRatio;
                    twaAtLowestSpeedRatio = encodedTwa;
                }
            }
        }
        if (twaAtLowestSpeedRatio == -1) {
            return null;
        }
        if (twaAtLowestSpeedRatio > 180) {
            twaAtLowestSpeedRatio = 360 - twaAtLowestSpeedRatio;
        }
        return twaAtLowestSpeedRatio;
    }

    @Override
    public Double getAbsTwaAtHighestRatioToEnteringSpeedStatistic() {
        double[] speedRatioPerTWA = getRatioToEnteringSpeedStatistic().getManeuverValuePerTWA();
        double highestSpeedRatio = 1;
        double twaAtHighestSpeedRatio = -1;
        for (int encodedTwa = 0; encodedTwa < 360; encodedTwa++) {
            double speedRatio = speedRatioPerTWA[encodedTwa];
            if (speedRatio != 0) {
                if (speedRatio > highestSpeedRatio) {
                    highestSpeedRatio = speedRatio;
                    twaAtHighestSpeedRatio = encodedTwa;
                }
            }
        }
        if (twaAtHighestSpeedRatio == -1) {
            return null;
        }
        if (twaAtHighestSpeedRatio > 180) {
            twaAtHighestSpeedRatio = 360 - twaAtHighestSpeedRatio;
        }
        return twaAtHighestSpeedRatio;
    }

}
