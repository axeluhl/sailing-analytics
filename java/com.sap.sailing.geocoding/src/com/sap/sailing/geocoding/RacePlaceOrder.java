package com.sap.sailing.geocoding;

import java.util.List;

public interface RacePlaceOrder {

    String getName();
    List<Placemark> getPlaces();
    void addPlace(Placemark place);
    
}
