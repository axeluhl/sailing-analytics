package com.sap.sailing.domain.common;


public interface RacePlaceOrder extends Named {

    Iterable<Placemark> getPlaces();
    
}
