package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.maps.client.geom.LatLngBounds;

public interface LatLngBoundsCalculator {
    LatLngBounds calculateNewBounds(RaceMap forMap);
}
