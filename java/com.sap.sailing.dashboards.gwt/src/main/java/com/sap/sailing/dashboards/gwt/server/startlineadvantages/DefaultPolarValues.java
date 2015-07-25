package com.sap.sailing.dashboards.gwt.server.startlineadvantages;

import com.sap.sailing.domain.common.ManeuverType;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DefaultPolarValues {

    private final static double TACKING_ANGLE = 40;
    private final static double JIBING_ANGLE = 30;
    private final static double BOAT_SPEED = 10;
    
    public static double getManouvreAngle(ManeuverType maneuverType) {
        double result = 0;
        if (maneuverType.equals(ManeuverType.TACK)) {
            result = TACKING_ANGLE * 2;
        } else if (maneuverType.equals(ManeuverType.JIBE)) {
            result = JIBING_ANGLE * 2;
        }
        return result;
    }
    
    public static double getBoatSpeedForWindAngleAndSpeed(double angle, double speed) {
        double result = BOAT_SPEED;
        if(angle <= 180) {
            result = BOAT_SPEED*(angle/1000);
        } if(angle > 180) {
            result = BOAT_SPEED*((360-angle)/1000);
        }
        return result;
    }
}
