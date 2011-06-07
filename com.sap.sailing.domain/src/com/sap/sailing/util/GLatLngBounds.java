package com.sap.sailing.util;

import com.sap.sailing.domain.base.Position;

public class GLatLngBounds {
    private final Position southWest;
    private final Position northEast;
    
    public GLatLngBounds(Position southWest, Position northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }

    public Position getSouthWest() {
        return southWest;
    }

    public Position getNorthEast() {
        return northEast;
    }

    public boolean contains(Position p) {
        return p.getLatDeg() >= getSouthWest().getLatDeg() && p.getLatDeg() <= getNorthEast().getLatDeg() &&
               (getNorthEast().getLngDeg() >= getSouthWest().getLngDeg() && p.getLngDeg() >= getSouthWest().getLngDeg() && p.getLngDeg() <= getNorthEast().getLngDeg() ||
               // cross date line bounds
                getNorthEast().getLngDeg() < getSouthWest().getLngDeg() &&
                      (p.getLngDeg() <= 180 && p.getLngDeg() >= getSouthWest().getLngDeg() ||
                       p.getLngDeg() >= -180 && p.getLngDeg() <= getNorthEast().getLngDeg()));
    }

    public boolean containsBounds(GLatLngBounds rect) {
        return this.contains(rect.getSouthWest()) && this.contains(rect.getNorthEast()) ||
               rect.contains(this.getSouthWest()) && rect.contains(this.getNorthEast());
    }

}
