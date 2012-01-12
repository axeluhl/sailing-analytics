package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class RacePlaceOrderImpl extends NamedImpl implements RacePlaceOrder {
    
    private final Iterable<Placemark> places;
    
    public RacePlaceOrderImpl(String raceName, Iterable<Placemark> places) {
        super(raceName);
        this.places = places;
    }

    @Override
    public Iterable<Placemark> getPlaces() {
        return places;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(getName() + "(");
        boolean first = true;
        for (Placemark place : places) {
            if (first) {
                b.append(place.getCountryCode() + ", " + place.getName());
                first = false;
            } else {
                b.append(" -> " + place.getCountryCode() + ", " + place.getName());
            }
        }
        b.append(")");
        return b.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((places == null) ? 0 : places.hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }

}
