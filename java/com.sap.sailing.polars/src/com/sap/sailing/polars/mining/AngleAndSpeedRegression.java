package com.sap.sailing.polars.mining;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.polars.impl.CubicEquation;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class AngleAndSpeedRegression {
    
    private final IncrementalLeastSquares speedRegression = new IncrementalAnyOrderLeastSquaresImpl(3, false);
    private final IncrementalLeastSquares angleRegression = new IncrementalAnyOrderLeastSquaresImpl(3);
    
    private double maxWindSpeedInKnots = -1;

    public void addData(WindWithConfidence<Pair<Position, TimePoint>> windSpeed,
            BearingWithConfidence<Integer> angleToTheWind, SpeedWithBearingWithConfidence<TimePoint> boatSpeed) {
        double windSpeedInKnots = windSpeed.getObject().getKnots();
        if (windSpeedInKnots > maxWindSpeedInKnots) {
            maxWindSpeedInKnots = windSpeedInKnots;
        }
        speedRegression.addData(windSpeedInKnots, boatSpeed.getObject().getKnots());
        angleRegression.addData(windSpeedInKnots, angleToTheWind.getObject().getDegrees()); 
    }

    public SpeedWithBearingWithConfidence<Void> estimateSpeedAndAngle(Speed windSpeed) throws NotEnoughDataHasBeenAddedException {
        double windSpeedInKnots = windSpeed.getKnots();
        if (windSpeedInKnots > maxWindSpeedInKnots) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        double estimatedSpeed = speedRegression.getOrCreatePolynomialFunction().value(windSpeedInKnots);
        double estimatedAngle = angleRegression.getOrCreatePolynomialFunction().value(windSpeedInKnots);
        Bearing bearing = new DegreeBearingImpl(estimatedAngle);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(estimatedSpeed, bearing);
        return new SpeedWithBearingWithConfidenceImpl<Void>(speedWithBearing, /*FIXME*/ 0.5, null);
    }

    public Set<SpeedWithBearingWithConfidence<Void>> estimateTrueWindSpeedAndAngleCandidates(Speed speedOverGround) throws NotEnoughDataHasBeenAddedException {
        double[] coefficiants = speedRegression.getOrCreatePolynomialFunction().getCoefficients();
        CubicEquation equation = new CubicEquation(coefficiants[2], coefficiants[1], coefficiants[0], -speedOverGround.getKnots());
        
        double[] windSpeedCandidates = equation.solve();
        Set<SpeedWithBearingWithConfidence<Void>> result = new HashSet<>();
        for (int i = 0; i < windSpeedCandidates.length; i++) {
            double windSpeedCandidateInKnots = windSpeedCandidates[i];
            if (windSpeedCandidateInKnots >= 0 && windSpeedCandidateInKnots <= maxWindSpeedInKnots) {
                double angle = 0;
                boolean angleFound;
                try {
                    angle = angleRegression.getOrCreatePolynomialFunction().value(windSpeedCandidateInKnots);
                    angleFound = true;
                } catch(NotEnoughDataHasBeenAddedException e) {
                    angleFound = false;
                }
                if (angleFound) {
                    result.add(new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(
                            windSpeedCandidateInKnots, new DegreeBearingImpl(angle)), 0.5 /*FIXME*/, null));
                }
            }
        }
        
        return result;
    }

    public PolynomialFunction getSpeedRegressionFunction() throws NotEnoughDataHasBeenAddedException {
        return speedRegression.getOrCreatePolynomialFunction();
    }

    public PolynomialFunction getAngleRegressionFunction() throws NotEnoughDataHasBeenAddedException {
        return angleRegression.getOrCreatePolynomialFunction();
    }

}
