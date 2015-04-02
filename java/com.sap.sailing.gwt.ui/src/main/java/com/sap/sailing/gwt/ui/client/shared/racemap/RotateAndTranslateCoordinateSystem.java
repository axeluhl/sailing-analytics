package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public class RotateAndTranslateCoordinateSystem implements CoordinateSystem {
    private final Position zeroZero;
    private final Bearing equator;
    private final Bearing rotationAngle;
    
    /**
     * @param zeroZero
     *            where to map {lat: 0, lng: 0} to in this coordinate system
     * @param equator
     *            the bearing to which to map the equator; 90deg east would give the default equator direction.
     */
    public RotateAndTranslateCoordinateSystem(Position zeroZero, Bearing equator) {
        super();
        this.zeroZero = zeroZero;
        this.equator = equator;
        rotationAngle = new DegreeBearingImpl(90).getDifferenceTo(equator);
    }

    @Override
    public Position map(Position position) {
        return position.getLocalCoordinates(zeroZero, equator);
    }

    @Override
    public Bearing map(Bearing bearing) {
        return bearing.add(rotationAngle);
    }
}
