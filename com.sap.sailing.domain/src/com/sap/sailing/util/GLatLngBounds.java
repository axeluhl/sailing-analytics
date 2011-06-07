package com.sap.sailing.util;

public class GLatLngBounds {
    private final GLatLng southWest;
    private final GLatLng northEast;
    
    public GLatLngBounds(GLatLng southWest, GLatLng northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }

    public GLatLng getSouthWest() {
        return southWest;
    }

    public GLatLng getNorthEast() {
        return northEast;
    }

    public boolean contains(GLatLng p) {
        return p.lat() >= getSouthWest().lat() && p.lat() <= getNorthEast().lat() &&
               (getNorthEast().lng() >= getSouthWest().lng() && p.lng() >= getSouthWest().lng() && p.lng() <= getNorthEast().lng() ||
               // cross date line bounds
                getNorthEast().lng() < getSouthWest().lng() &&
                      (p.lng() <= 180 && p.lng() >= getSouthWest().lng() ||
                       p.lng() >= -180 && p.lng() <= getNorthEast().lng()));
    }

    public boolean containsBounds(GLatLngBounds rect) {
        return this.contains(rect.getSouthWest()) && this.contains(rect.getNorthEast()) ||
               rect.contains(this.getSouthWest()) && rect.contains(this.getNorthEast());
    }

}
