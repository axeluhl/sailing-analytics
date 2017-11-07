package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;

/**
 * Extended version of {@link BravoFix} that provides access to more measures found in the extended data format.
 */
public interface BravoExtendedFix extends BravoFix {
    Double getPortDaggerboardRake();
    Double getStbdDaggerboardRake();
    Double getPortRudderRake();
    Double getStbdRudderRake();
    Bearing getMastRotation();
    Double getLeeway();
    Double getSet();
    Double getDrift();
    Distance getDepth();
    Bearing getRudder();
    Double getForestayLoad();
    Bearing getTackAngle();
    Bearing getRake();
    Double getDeflectorPercentage();
    Bearing getTargetHeel();
    Distance getDeflector();
    Double getTargetBoatspeedP();
}
