package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCArray;

public interface PathChangeHandler {
    void setPath(MVCArray<LatLng> path);
    void insertAt(int index, LatLng position);
    LatLng removeAt(int index);
    void setAt(int index, LatLng position);
    void clear();
}
