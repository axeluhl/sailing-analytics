package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;

public class BoundsImpl implements Bounds {
    private final Position sw;
    private final Position ne;
    private final boolean crossesDateLine;
    
    public BoundsImpl(Position sw, Position ne) {
        super();
        this.sw = sw;
        this.ne = ne;
        crossesDateLine = ne.getLngDeg() < sw.getLngDeg();
    }

    @Override
    public Position getNorthEast() {
        return ne;
    }

    @Override
    public Position getSouthWest() {
        return sw;
    }

    @Override
    public Bounds intersect(Bounds other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bounds union(Bounds other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean intersects(Bounds other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(Position other) {
        return
                    // lat contained:
                other.getLatDeg() >= getSouthWest().getLatDeg() && other.getLatDeg() <= getNorthEast().getLatDeg() &&
                    // lng contained:
                (isCrossesDateLine()
                    // between SW and date line...
                    ? (other.getLngDeg() <= 180 && other.getLngDeg() >= getSouthWest().getLngDeg()) ||
                    // ...or between date line and NE
                      (other.getLngDeg() >= -180 && other.getLngDeg() <= getNorthEast().getLngDeg())
                    // these bounds are not crossing the date line; simple numeric comparison
                    : other.getLngDeg() >= getSouthWest().getLngDeg() && other.getLngDeg() <= getNorthEast().getLngDeg())
                ;
    }

    @Override
    public boolean contains(Bounds other) {
        return isCrossesDateLine() == other.isCrossesDateLine() && contains(other.getNorthEast()) && contains(other.getSouthWest());
    }

    @Override
    public boolean isCrossesDateLine() {
        return crossesDateLine;
    }
}
