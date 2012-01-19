package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface RacePlaceOrder extends Serializable {

    Placemark getStartPlace();
    Placemark getFinishPlace();
    
}
