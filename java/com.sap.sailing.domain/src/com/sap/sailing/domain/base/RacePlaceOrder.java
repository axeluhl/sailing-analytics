package com.sap.sailing.domain.base;

import java.util.List;

import com.sap.sailing.geocoding.Placemark;

public interface RacePlaceOrder {

    String getName();
    List<Placemark> getPlaces();
    void addPlace(Placemark place);
    
}
