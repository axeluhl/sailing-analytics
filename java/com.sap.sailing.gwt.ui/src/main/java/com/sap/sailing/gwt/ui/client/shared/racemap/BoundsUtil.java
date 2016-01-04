package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
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
    
    public static Position getAsPosition(LatLng latLngPosition) {
        return new DegreePosition(latLngPosition.getLatitude(), latLngPosition.getLongitude());
    }

    public static Bounds getAsBounds(Position position) {
        return new BoundsImpl(position, position);
    }
    
    public static LatLngBounds getAsBounds(LatLng position) {
        return LatLngBounds.newInstance(position, position);
    }
    
    public static boolean contains(LatLngBounds doesOrDoesNotContain, LatLngBounds containedOrNotContained) {
        return isCrossesDateLine(doesOrDoesNotContain) == isCrossesDateLine(containedOrNotContained)
                && doesOrDoesNotContain.contains(containedOrNotContained.getNorthEast())
                && doesOrDoesNotContain.contains(containedOrNotContained.getSouthWest());
    }

    public static boolean isCrossesDateLine(LatLngBounds latLngBounds) {
        return latLngBounds.getNorthEast().getLongitude() < latLngBounds.getSouthWest().getLongitude();
    }
    
    public static LatLngBounds intersect(LatLngBounds b1, LatLngBounds other) {
        final double maxSouthLatDeg = Math.max(b1.getSouthWest().getLatitude(), other.getSouthWest().getLatitude());
        final double minNorthLatDeg = Math.min(b1.getNorthEast().getLatitude(), other.getNorthEast().getLatitude());
        final double westDeg;
        if (containsLngDeg(b1, other.getSouthWest().getLongitude())) {
            westDeg = other.getSouthWest().getLongitude();
        } else if (containsLngDeg(other, b1.getSouthWest().getLongitude())) {
            westDeg = b1.getSouthWest().getLongitude();
        } else {
            // no intersection
            westDeg = b1.getSouthWest().getLongitude();
        }
        final double eastDeg;
        if (containsLngDeg(b1, other.getNorthEast().getLongitude())) {
            eastDeg = other.getNorthEast().getLongitude();
        } else if (containsLngDeg(other, b1.getNorthEast().getLongitude())) {
            eastDeg = b1.getNorthEast().getLongitude();
        } else {
            // no intersection
            eastDeg = b1.getSouthWest().getLongitude();
        }
        return LatLngBounds.newInstance(LatLng.newInstance(maxSouthLatDeg, westDeg), LatLng.newInstance(minNorthLatDeg, eastDeg));
    }
    
    private static boolean containsLngDeg(LatLngBounds bounds, double lngDeg) {
        return spansLngDeg(bounds.getSouthWest().getLongitude(), bounds.getNorthEast().getLongitude(), lngDeg);
    }

    private static boolean spansLngDeg(double westLngDeg, double eastLngDeg, double lngDeg) {
        return isCrossingDateLine(westLngDeg, eastLngDeg)
                ? (lngDeg >= westLngDeg && lngDeg <= 180) || (lngDeg >= -180 && lngDeg <= eastLngDeg)
                : westLngDeg <= lngDeg && lngDeg <= eastLngDeg;
    }

    private static boolean isCrossingDateLine(double westLngDeg, double eastLngDeg) {
        return westLngDeg > eastLngDeg;
    }
}
