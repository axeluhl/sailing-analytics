package com.sap.sailing.geocoding.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.geocoding.Placemark;
import com.sap.sailing.geocoding.RacePlaceOrder;

public class RacePlaceOrderImpl implements RacePlaceOrder {
    
    private String raceName;
    private List<Placemark> places;

    
    
    public RacePlaceOrderImpl(String raceName) {
        this.raceName = raceName;
        this.places = new ArrayList<Placemark>();
    }

    @Override
    public String getName() {
        return raceName;
    }

    @Override
    public List<Placemark> getPlaces() {
        return places;
    }

    @Override
    public void addPlace(Placemark place) {
        places.add(place);
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(raceName + "(");
        int c = 0;
        for (Placemark place : places) {
            if (c < places.size() - 1) {
                b.append(place.getCountryCode() + ", " + place.getName() + " -> ");
            } else {
                b.append(place.getCountryCode() + ", " + place.getName() + ")");
            }
            c++;
        }
        return b.toString();
    }

}
