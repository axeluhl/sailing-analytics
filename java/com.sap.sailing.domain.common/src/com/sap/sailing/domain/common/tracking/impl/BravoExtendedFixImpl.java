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
    public double get(String valueName) {
        BravoExtendedSensorDataMetadata colDefEnum = BravoExtendedSensorDataMetadata.byColumnName(valueName);
        if (colDefEnum == null) {
            throw new IllegalArgumentException("Unknown value \"" + valueName + "\" for " + getClass().getSimpleName());
        }
        int index = colDefEnum.getColumnIndex();
        return fix.get(index);
    }

    @Override
    public double getPortDaggerboardRake() {
        return fix.get(BravoExtendedSensorDataMetadata.DB_RAKE_PORT.getColumnIndex());
    }

    @Override
    public double getStbdDaggerboardRake() {
        return fix.get(BravoExtendedSensorDataMetadata.DB_RAKE_STBD.getColumnIndex());
    }

    @Override
    public double getPortRudderRake() {
        return fix.get(BravoExtendedSensorDataMetadata.RUDDER_RAKE_PORT.getColumnIndex());
    }

    @Override
    public double getStbdRudderRake() {
        return fix.get(BravoExtendedSensorDataMetadata.RUDDER_RAKE_STBD.getColumnIndex());
    }

    @Override
    public Bearing getMastRotation() {
        return new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.MAST_ROTATION.getColumnIndex()));
    }

}
