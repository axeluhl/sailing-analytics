package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface RacePlaceOrder extends Serializable {

    Iterable<Placemark> getPlaces();
    
}
