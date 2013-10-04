package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.List;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.Polyline;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface TailFactory {
    Polyline createTail(CompetitorDTO competitor, List<LatLng> points);
}
