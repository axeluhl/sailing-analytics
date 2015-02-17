package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sse.common.Util.Pair;

public class CubicSpeedRegressions {
    
    private final Map<Pair<LegType, Tack>, IncrementalLeastSquares> regressions;
    
    public CubicSpeedRegressions() {
        regressions = new HashMap<>();
        for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
            for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {
                regressions.put(new Pair<LegType, Tack>(legType, tack),
                        new IncrementalAnyOrderLeastSquaresImpl(3));
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
        IncrementalLeastSquares leastSquares = regressions.get(new Pair<LegType, Tack>(legType, tack));
        leastSquares.addData(fix.getWindSpeed().getObject().getKnots(), fix.getBoatSpeed().getObject().getKnots());
    }

    public double estimateSpeedInKnots(LegType legType, Tack tack, double windSpeedInKnots) {
        double result;
        try {
            result = regressions.get(new Pair<LegType, Tack>(legType, tack)).getOrCreatePolynomialFunction().value(windSpeedInKnots);
        } catch (NotEnoughDataHasBeenAddedException e) {
            //FIXME try to come up with something smarter
            result = 0;
        }
        return result;
    }

}
