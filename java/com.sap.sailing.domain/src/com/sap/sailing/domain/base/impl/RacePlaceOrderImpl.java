package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.RacePlaceOrder;
import com.sap.sailing.geocoding.Placemark;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((places == null) ? 0 : places.hashCode());
        result = prime * result + ((raceName == null) ? 0 : raceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RacePlaceOrderImpl other = (RacePlaceOrderImpl) obj;
        if (places == null) {
            if (other.places != null)
                return false;
        } else if (!places.equals(other.places))
            return false;
        if (raceName == null) {
            if (other.raceName != null)
                return false;
        } else if (!raceName.equals(other.raceName))
            return false;
        return true;
    }

}
