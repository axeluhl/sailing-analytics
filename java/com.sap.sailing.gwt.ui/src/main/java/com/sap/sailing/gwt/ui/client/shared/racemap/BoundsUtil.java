package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;

/**
 * Converts between {@link Bounds} and {@link LatLngBounds}.
 * 
 * @author Axel Uhl (D043530)
 */
public class BoundsUtil {
    public static Bounds getAsBounds(LatLngBounds latLngBounds) {
        return new BoundsImpl(new DegreePosition(latLngBounds.getSouthWest().getLatitude(), latLngBounds.getSouthWest().getLongitude()),
                new DegreePosition(latLngBounds.getNorthEast().getLatitude(), latLngBounds.getNorthEast().getLongitude()));
    }
    
    public static LatLngBounds getAsLatLngBounds(Bounds bounds) {
        return LatLngBounds.newInstance(LatLng.newInstance(bounds.getSouthWest().getLatDeg(), bounds.getSouthWest().getLngDeg()),
                LatLng.newInstance(bounds.getNorthEast().getLatDeg(), bounds.getNorthEast().getLngDeg()));
    }
    
    public static Bounds getAsBounds(PositionDTO positionDTO) {
        Position pos = getAsPosition(positionDTO);
        return new BoundsImpl(pos, pos);
    }
    
    public static Position getAsPosition(PositionDTO positionDTO) {
        return new DegreePosition(positionDTO.getLatDeg(), positionDTO.getLngDeg());
    }

    public static Position getAsPosition(LatLng latLngPosition) {
        return new DegreePosition(latLngPosition.getLatitude(), latLngPosition.getLongitude());
    }

    public static Bounds getAsBounds(Position position) {
        return new BoundsImpl(position, position);
    }
}
