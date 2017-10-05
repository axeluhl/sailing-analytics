package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Bearing;

public interface BravoExtendedFix extends BravoFix {
    Bearing getDaggerBoardRakeAnglePort();
    Bearing getDaggerBoardRakeAngleStbd();
    Bearing getRudderRakeAnglePort();
    Bearing getRudderRakeAngleStbd();
    Bearing getMastRotation();
}
