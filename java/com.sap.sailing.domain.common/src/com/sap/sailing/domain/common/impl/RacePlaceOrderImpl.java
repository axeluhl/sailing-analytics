package com.sap.sailing.domain.common.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class RacePlaceOrderImpl implements RacePlaceOrder, Serializable {
    private static final long serialVersionUID = 7590835541329816755L;
    
    private List<Placemark> places;
    
    RacePlaceOrderImpl() {}
    
    public RacePlaceOrderImpl(List<Placemark> places) {
        this.places = places;
    }

    @Override
    public Iterable<Placemark> getPlaces() {
        return Collections.unmodifiableCollection(places);
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (Placemark place : places) {
            if (first) {
                b.append(place.getCountryCode() + ", " + place.getName());
                first = false;
            } else {
                b.append(" -> " + place.getCountryCode() + ", " + place.getName());
            }
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((places == null) ? 0 : places.hashCode());
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
        return true;
    }

}
