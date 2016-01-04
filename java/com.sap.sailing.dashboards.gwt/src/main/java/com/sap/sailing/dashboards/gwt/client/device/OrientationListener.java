package com.sap.sailing.dashboards.gwt.client.device;

import com.sap.sse.common.Util.Pair;

public interface OrientationListener {
    
    /**
     * Returns a Pair containing the {@link OrientationType} and the exact angle.
     * */
    public void orientationChanged(Pair <OrientationType, Double> orientation);
}
