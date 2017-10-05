package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Bearing;

public interface BravoExtendedFix extends BravoFix {
    Bearing getDbRakePort();
    Bearing getDbRakeStbd();
    Bearing getRudderRakePort();
    Bearing getRudderRakeStbd();
    Bearing getMastRotation();
}
