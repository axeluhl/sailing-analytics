package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;

public class IdentityCoordinateSystem implements CoordinateSystem {
    @Override
    public Position map(Position position) {
        return position;
    }

    @Override
    public Bearing map(Bearing bearing) {
        return bearing;
    }

    @Override
    public double mapDegreeBearing(double trueBearingInDegrees) {
        return trueBearingInDegrees;
    }
}
