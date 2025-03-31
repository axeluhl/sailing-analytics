package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sse.common.Color;
import com.sap.sse.common.Renamable;

public interface DynamicBoat extends Boat, Renamable {
    void setSailId(String sailId);

    void setName(String name);

    void setColor(Color color);
}
