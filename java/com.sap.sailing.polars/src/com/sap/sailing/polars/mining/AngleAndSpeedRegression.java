package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class AngleAndSpeedRegression {
    
    private final IncrementalLeastSquares speedRegression = new IncrementalAnyOrderLeastSquaresImpl(3, false);
    private final IncrementalLeastSquares angleRegression = new IncrementalAnyOrderLeastSquaresImpl(3);

    public void addData(WindWithConfidence<Pair<Position, TimePoint>> windSpeed,
            BearingWithConfidence<Integer> angleToTheWind, SpeedWithBearingWithConfidence<TimePoint> boatSpeed) {
        double windSpeedInKnots = windSpeed.getObject().getKnots();
        speedRegression.addData(windSpeedInKnots, boatSpeed.getObject().getKnots());
        angleRegression.addData(windSpeedInKnots, angleToTheWind.getObject().getDegrees()); 
    }

    public SpeedWithBearingWithConfidence<Void> estimateSpeedAndAngle(Speed windSpeed) throws NotEnoughDataHasBeenAddedException {
        double windSpeedInKnots = windSpeed.getKnots();
        double estimatedSpeed = speedRegression.getOrCreatePolynomialFunction().value(windSpeedInKnots);
        double estimatedAngle = angleRegression.getOrCreatePolynomialFunction().value(windSpeedInKnots);
        Bearing bearing = new DegreeBearingImpl(estimatedAngle);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(estimatedSpeed, bearing);
        return new SpeedWithBearingWithConfidenceImpl<Void>(speedWithBearing, /*FIXME*/ 0.5, null);
    }

}
