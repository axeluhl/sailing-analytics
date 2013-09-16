package com.sap.sailing.domain.common.quadtree.impl;

import com.sap.sailing.domain.common.Position;

public class Bounds {
    private final Position southWest;
    private final Position northEast;
    
    public Bounds(Position southWest, Position northEast) {
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

    public boolean containsBounds(Bounds rect) {
        return this.contains(rect.getSouthWest()) && this.contains(rect.getNorthEast()) ||
               rect.contains(this.getSouthWest()) && rect.contains(this.getNorthEast());
    }

}
