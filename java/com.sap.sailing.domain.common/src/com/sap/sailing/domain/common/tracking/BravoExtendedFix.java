package com.sap.sailing.domain.common.tracking;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

/**
 * Extended version of {@link BravoFix} that provides access to more measures found in the extended data format.
 */
public interface BravoExtendedFix extends BravoFix {
    Double getPortDaggerboardRake();
    Double getStbdDaggerboardRake();
    Double getPortRudderRake();
    Double getStbdRudderRake();
    Bearing getMastRotation();
    Bearing getLeeway();
    Double getSet();
    Bearing getDrift();
    Distance getDepth();
    Bearing getRudder();
    Double getForestayLoad();
    Double getForestayPressure();
    Bearing getTackAngle();
    Bearing getRake();
    Double getDeflectorPercentage();
    Bearing getTargetHeel();
    Distance getDeflector();
    Double getTargetBoatspeedP();
    Double getExpeditionAWA();
    Double getExpeditionAWS();
    Double getExpeditionTWA();
    Double getExpeditionTWS();
    Double getExpeditionTWD();
    Double getExpeditionBSP();
    Double getExpeditionBSP_TR();
    Double getExpeditionSOG();
    Double getExpeditionCOG();
    Double getExpeditionForestayLoad();
    Double getExpeditionRake();
    Double getExpeditionHDG();
    Double getExpeditionHeel();
    Double getExpeditionTG_Heell();
    Double getExpeditionTmToGun();
    Double getExpeditionTmToBurn();
    Double getExpeditionBelowLn();
    Double getExpeditionCourse();
    Double getExpeditionJibCarStbd();
    Double getExpeditionJibCarPort();
    Double getExpeditionLoadS();
    Double getExpeditionLoadP();
    Double getExpeditionBARO();
    Double getExpeditionMastButt();
    Double getExpeditionRateOfTurn();
}
