package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.List;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

/**
 * Creates tails and their styles for the map.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TailFactory {
    Polyline createTail(CompetitorDTO competitor, List<LatLng> points);
    
    PolylineOptions createTailStyle(CompetitorDTO competitor, DisplayMode displayMode);
}
