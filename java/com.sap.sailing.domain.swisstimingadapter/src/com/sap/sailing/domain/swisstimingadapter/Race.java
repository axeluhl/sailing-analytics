package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.BoatClass;


public interface Race extends WithDescription {
    String getRaceID();
    String getRaceName();
    BoatClass getBoatClass();
}
