package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.sensordata.BravoExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;

public class BravoExtendedFixImpl extends BravoFixImpl implements BravoExtendedFix {
    private static final long serialVersionUID = 5622321493028301922L;

    public BravoExtendedFixImpl(DoubleVectorFix fix) {
        super(fix);
    }

    @Override
    public Bearing getDbRakePort() {
        return new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.DB_RAKE_PORT.getColumnIndex()));
    }

    @Override
    public Bearing getDbRakeStbd() {
        return new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.DB_RAKE_STBD.getColumnIndex()));
    }

    @Override
    public Bearing getRudderRakePort() {
        return new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.RUDDER_RAKE_PORT.getColumnIndex()));
    }

    @Override
    public Bearing getRudderRakeStbd() {
        return new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.RUDDER_RAKE_STBD.getColumnIndex()));
    }

    @Override
    public Bearing getMastRotation() {
        return new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.MAST_ROTATION.getColumnIndex()));
    }

}
