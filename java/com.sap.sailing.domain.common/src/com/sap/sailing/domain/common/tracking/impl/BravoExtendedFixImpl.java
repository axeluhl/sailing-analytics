package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
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
