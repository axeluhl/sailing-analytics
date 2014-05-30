package com.sap.sailing.server.trackfiles.common;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

/**
 * Provide inference functionality.
 * @author Fredrik Teschke
 *
 */
public abstract class BaseGPSFixImporterImpl implements GPSFixImporter {
    private GPSFix previousFix;
    
    protected void addFixAndInfer(Callback callback, boolean inferSpeedAndBearing, GPSFix fix) {
        if (inferSpeedAndBearing && ! (fix instanceof GPSFixMoving)) {
            if (previousFix == null) {
                //have to infer speed and bearing, but this is the first fix -> drop it
                return;
            }
            SpeedWithBearing speedWithBearing = previousFix.getSpeedAndBearingRequiredToReach(fix);
            fix = new GPSFixMovingImpl(fix.getPosition(), fix.getTimePoint(), speedWithBearing);
        }
        
        previousFix = fix;
        callback.addFix(fix);
    }
}
