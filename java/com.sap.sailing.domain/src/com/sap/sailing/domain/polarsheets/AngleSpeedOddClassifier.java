package com.sap.sailing.domain.polarsheets;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AngleSpeedOddClassifier implements OddFixClassifier {
    
    
    private static final Logger logger = Logger.getLogger(AngleSpeedOddClassifier.class.getName());

    @Override
    public boolean classifiesAsOdd(PolarFix fix) {
        double angleToWind = fix.getAngleToWind();
        double speedInKnots = fix.getBoatSpeed().getKnots();
        if (angleToWind < 20 && angleToWind > -20) {
            if (speedInKnots > 2) {
                logger.log(Level.INFO, "Boat goes fast against the wind. Data-Point should have been excluded during maneuver check.");
                return true;
            }
        }
        return false;
    }

}
