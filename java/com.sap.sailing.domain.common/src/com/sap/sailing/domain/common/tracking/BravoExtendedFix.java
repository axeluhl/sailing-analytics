package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Bearing;

/**
 * Extended version of {@link BravoFix} that provides access to more measures found in the extended data format.
 */
public interface BravoExtendedFix extends BravoFix {
    double getPortDaggerboardRake();
    double getStbdDaggerboardRake();
    double getPortRudderRake();
    double getStbdRudderRake();
    Bearing getMastRotation();
}
