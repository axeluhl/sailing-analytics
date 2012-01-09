package com.sap.sailing.geocoding;

import com.sap.sailing.geocoding.impl.ReverseGeocoderImpl;

public interface ReverseGeocoder {
    ReverseGeocoder INSTANCE = new ReverseGeocoderImpl();
    
    Placemark getPlacemark(double latDeg, double lngDeg);
    
//    List<Placemark>
}
