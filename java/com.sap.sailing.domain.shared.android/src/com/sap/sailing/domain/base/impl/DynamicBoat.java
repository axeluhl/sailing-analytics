package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;

public interface DynamicBoat extends Boat {
    void setSailId(String newSailId);
    void setBoatClass(BoatClass newBoatClass);
}
