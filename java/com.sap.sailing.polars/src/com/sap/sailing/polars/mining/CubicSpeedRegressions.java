package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.polars.regression.impl.AnyOrderLeastSquaresRegression;
import com.sap.sse.common.Util.Pair;

public class CubicSpeedRegressions {
    
    private final Map<Pair<LegType, Tack>, AnyOrderLeastSquaresRegression> regressions;
    
    public CubicSpeedRegressions() {
        regressions = new HashMap<>();
        for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
            for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {
                regressions.put(new Pair<LegType, Tack>(legType, tack),
                        new AnyOrderLeastSquaresRegression(3));
            }
        }
    }

    public void addFix(GPSFixMovingWithPolarContext fix) {
        final LegType legType;
        final Tack tack;
        final int roundedAngleDeg = fix.getRoundedTrueWindAngle().getAngleDeg();
        if (roundedAngleDeg < -90) {
            legType = LegType.DOWNWIND;
            tack = Tack.PORT;
        } else if (roundedAngleDeg < 0) {
            legType = LegType.UPWIND;
            tack = Tack.PORT;
        } else if (roundedAngleDeg > 90) {
            legType = LegType.DOWNWIND;
            tack = Tack.STARBOARD;
        } else { // roundedAngleDeg > 0 && roundedAngleDeg <= 90
            legType = LegType.UPWIND;
            tack = Tack.STARBOARD;
        }
        AnyOrderLeastSquaresRegression regression = regressions.get(new Pair<LegType, Tack>(legType, tack));
        regression.addData(fix.getWindSpeed().getObject().getKnots(), fix.getBoatSpeed().getObject().getKnots());
    }

    public double estimateSpeedInKnots(LegType legType, Tack tack, double windSpeedInKnots) {
        double[] coeffs =  regressions.get(new Pair<LegType, Tack>(legType, tack)).getCoefficiants();
        double resultSum = 0;
        for (int i = 0; i < coeffs.length; i++) {
            resultSum = resultSum + coeffs[i] * Math.pow(windSpeedInKnots, i);
        }
        return resultSum;
    }

}
