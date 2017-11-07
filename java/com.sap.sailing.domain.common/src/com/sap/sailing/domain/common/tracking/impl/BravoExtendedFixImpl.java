package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.sensordata.BravoExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.sensordata.ColumnMetadata;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;

/**
 * Implementation of {@link BravoExtendedFix}. {@link BravoExtendedFix} adds more measures compared to {@link BravoFix}.
 */
public class BravoExtendedFixImpl extends BravoFixImpl implements BravoExtendedFix {
    private static final long serialVersionUID = 5622321493028301922L;

    public BravoExtendedFixImpl(DoubleVectorFix fix) {
        super(fix);
    }

    @Override
    protected ColumnMetadata resolveMetadataFromValueName(String valueName) {
        return BravoExtendedSensorDataMetadata.byColumnName(valueName);
    }

    @Override
    public Double getPortDaggerboardRake() {
        return fix.get(BravoExtendedSensorDataMetadata.DB_RAKE_PORT.getColumnIndex());
    }

    @Override
    public Double getStbdDaggerboardRake() {
        return fix.get(BravoExtendedSensorDataMetadata.DB_RAKE_STBD.getColumnIndex());
    }

    @Override
    public Double getPortRudderRake() {
        return fix.get(BravoExtendedSensorDataMetadata.RUDDER_RAKE_PORT.getColumnIndex());
    }

    @Override
    public Double getStbdRudderRake() {
        return fix.get(BravoExtendedSensorDataMetadata.RUDDER_RAKE_STBD.getColumnIndex());
    }

    @Override
    public Bearing getMastRotation() {
        final Double bearingDeg = fix.get(BravoExtendedSensorDataMetadata.MAST_ROTATION.getColumnIndex());
        return bearingDeg == null ? null : new DegreeBearingImpl(bearingDeg);
    }

    @Override
    public Double getLeeway() {
        return fix.get(BravoExtendedSensorDataMetadata.LEEWAY.getColumnIndex());
    }

    @Override
    public Double getSet() {
        return fix.get(BravoExtendedSensorDataMetadata.SET.getColumnIndex());
    }

    @Override
    public Double getDrift() {
        return fix.get(BravoExtendedSensorDataMetadata.DRIFT.getColumnIndex());
    }

    @Override
    public Distance getDepth() {
        final Double depthInMeters = fix.get(BravoExtendedSensorDataMetadata.DEPTH.getColumnIndex());
        return depthInMeters == null ? null : new MeterDistance(depthInMeters);
    }

    @Override
    public Bearing getRudder() {
        final Double rudderAngleDeg = fix.get(BravoExtendedSensorDataMetadata.RUDDER.getColumnIndex());
        return rudderAngleDeg == null ? null : new DegreeBearingImpl(rudderAngleDeg);
    }

    @Override
    public Double getForestayLoad() {
        return fix.get(BravoExtendedSensorDataMetadata.FORESTAY_LOAD.getColumnIndex());
    }

    @Override
    public Bearing getTackAngle() {
        final Double tackAngleDeg = fix.get(BravoExtendedSensorDataMetadata.TACK_ANGLE.getColumnIndex());
        return tackAngleDeg == null ? null : new DegreeBearingImpl(tackAngleDeg);
    }

    @Override
    public Bearing getRake() {
        final Double rakeDeg = fix.get(BravoExtendedSensorDataMetadata.RAKE_DEG.getColumnIndex());
        return rakeDeg == null ? null : new DegreeBearingImpl(rakeDeg);
    }

    @Override
    public Double getDeflectorPercentage() {
        return fix.get(BravoExtendedSensorDataMetadata.DEFLECTOR_PERCENTAGE.getColumnIndex());
    }

    @Override
    public Bearing getTargetHeel() {
        final Double targetHeelDeg = fix.get(BravoExtendedSensorDataMetadata.TARGET_HEEL.getColumnIndex());
        return targetHeelDeg == null ? null : new DegreeBearingImpl(targetHeelDeg);
    }

    @Override
    public Distance getDeflector() {
        final Double deflectorMillimeters = fix.get(BravoExtendedSensorDataMetadata.DEFLECTOR_MILLIMETERS.getColumnIndex());
        return deflectorMillimeters == null ? null : new MeterDistance(deflectorMillimeters / 1000.);
    }

    @Override
    public Double getTargetBoatspeedP() {
        return fix.get(BravoExtendedSensorDataMetadata.TARGET_BOATSPEED_P.getColumnIndex());
    }
}
