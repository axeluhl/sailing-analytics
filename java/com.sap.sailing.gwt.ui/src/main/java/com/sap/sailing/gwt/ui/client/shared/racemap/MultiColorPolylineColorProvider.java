package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;

@FunctionalInterface
public interface MultiColorPolylineColorProvider {
    public String getColor(LatLng position);
}
