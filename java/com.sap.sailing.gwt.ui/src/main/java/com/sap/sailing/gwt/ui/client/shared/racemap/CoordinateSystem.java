package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Bearing;

/**
 * Maps original coordinates, headings, bearings, and directions to a map coordinate space. The default mapping will
 * simply preserve everything as is. Other mappings may apply a translation and rotation to map the geometry to another
 * map area and rotating it accordingly.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CoordinateSystem {
    Position map(Position position);

    Bearing map(Bearing bearing);
    
    /**
     * Same as {@link #map(Bearing)}, only that the true bearing to be mapped is provided as a degree value.
     * Of course, this method can not only be used for a "bearing" but also for true headings, true
     * courses, true directions, and so on.
     * 
     * @return a degree angle from the interval [0..360)
     */
    double mapDegreeBearing(double trueBearingInDegrees);

    LatLng toLatLng(Position position);

    /**
     * "Unmaps" a map position <code>p</code>, inverting the mapping implemented by the {@link #map(Position)} method.
     * 
     * @param p
     *            a coordinate from the map, such as one obtained from the {@link #map(Position)} operation
     * @return a real-world position <code>result</code> such that {@link #map(Position) map(p)}<code>.equals(result)</code>
     */
    Position getPosition(LatLng p);
}
