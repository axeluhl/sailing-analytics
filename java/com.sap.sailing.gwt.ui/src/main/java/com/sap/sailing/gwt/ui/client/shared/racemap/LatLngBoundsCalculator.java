package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.domain.common.Bounds;

public interface LatLngBoundsCalculator {
    Bounds calculateNewBounds(RaceMap forMap);
}
