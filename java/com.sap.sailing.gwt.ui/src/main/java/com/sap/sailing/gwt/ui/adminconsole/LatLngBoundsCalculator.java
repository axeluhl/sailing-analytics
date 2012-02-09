package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.maps.client.geom.LatLngBounds;

public interface LatLngBoundsCalculator {

    public LatLngBounds calculateNewBounds(RaceMap forMap);
    
}
