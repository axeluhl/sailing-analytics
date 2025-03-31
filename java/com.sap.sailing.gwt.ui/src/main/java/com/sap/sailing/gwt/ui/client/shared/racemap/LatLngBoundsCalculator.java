package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLngBounds;

public interface LatLngBoundsCalculator {
    LatLngBounds calculateNewBounds(RaceMap forMap);
}
