package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.List;

import com.google.gwt.maps.client.base.LatLng;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

/**
 * Creates tails and their styles for the map.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TailFactory {
    Colorline createTail(CompetitorDTO competitor, List<LatLng> points);
    
    ColorlineOptions createTailStyle(CompetitorDTO competitor, DisplayMode displayMode);
}
