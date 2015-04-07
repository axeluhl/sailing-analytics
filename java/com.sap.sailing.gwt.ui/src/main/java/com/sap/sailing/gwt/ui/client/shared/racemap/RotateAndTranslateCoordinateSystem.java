package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

/**
 * Translates and rotates a coordinate space. The translation is specified by providing the position
 * to which to map {lat: 0, lng: 0}. The rotation is defined by a new equator direction. Here, 90deg would
 * keep the usual equator direction. The coordinate system is fixed once constructed, so its translation
 * and rotation parameters cannot be altered. The rationale behind this is to force clients to re-do all
 * the mappings done so far because inconsistencies would result if over time positions and bearings are
 * mapped through different transformations, yet displayed together.
 * 
 * @author Axel Uhl (D043530)
 *
 */
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

    @Override
    public double mapDegreeBearing(double trueBearingInDegrees) {
        return (trueBearingInDegrees + rotationAngle.getDegrees()) % 360.;
    }
}
