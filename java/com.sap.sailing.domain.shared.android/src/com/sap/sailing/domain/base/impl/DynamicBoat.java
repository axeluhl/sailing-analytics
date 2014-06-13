package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatChangeListener;

public interface DynamicBoat extends Boat {
    void setSailId(String newSailId);

    void addBoatChangeListener(BoatChangeListener listener);

    void removeCompetitorChangeListener(BoatChangeListener listener);

}
