package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Date;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sse.common.Util;

/**
 * A weigher that uses a {@link Position} and a {@link TimePoint} to compute a confidence based on time and space
 * distance. If the <code>fix</code> or the <code>request</code> parameter in a call to
 * {@link #getConfidence(com.sap.sse.common.Util.Pair, com.sap.sse.common.Util.Pair)} have a <code>null</code>
 * {@link Position} then no distance-based confidence is considered, and only the time difference is taken into account.
 * Otherwise, the time-based confidence and the distance-based confidence are multiplied to result in the total
 * confidence.
 * <p>
 * 
 * For calculating the distances, this weigher uses an approximation based on the euclidian mapping of latitude and
 * longitude, assuming that when multiplying the longitudes with cos(lat) we can approximately apply cartesian geometry.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class PositionDTOAndDateWeigher implements Weigher<Util.Pair<PositionDTO, Date>> {
    private static final long serialVersionUID = -262428237738496818L;
    private final long halfConfidenceAfterMilliseconds;
    private final double halfConfidenceDistanceNauticalMiles;
    private final double cosineOfAverageLatitude;
    
    public static interface AverageLatitudeProvider {
        double getAverageLatitudeDeg();
    }
    
    public PositionDTOAndDateWeigher(long halfConfidenceAfterMilliseconds, Distance halfConfidenceDistance,
            AverageLatitudeProvider averageLatitudeDeg) {
        try {
            this.cosineOfAverageLatitude = Math.cos(averageLatitudeDeg.getAverageLatitudeDeg()/180.*Math.PI);
        } catch (Exception e) {
            throw new RuntimeException("Internal error", e);
        }
        this.halfConfidenceAfterMilliseconds = halfConfidenceAfterMilliseconds;
        this.halfConfidenceDistanceNauticalMiles = halfConfidenceDistance.getNauticalMiles();
    }
    
    @Override
    public double getConfidence(Util.Pair<PositionDTO, Date> fix, Util.Pair<PositionDTO, Date> request) {
        final double timeConfidence;
        {
            double x = Math.abs(fix.getB().getTime() - request.getB().getTime());
            double c = halfConfidenceAfterMilliseconds;
            double y = halfConfidenceAfterMilliseconds;
            timeConfidence = c / (x + y);
        }
        final double distanceConfidence;
        if (fix.getA() != null && request.getA() != null) {
            double x = getApproximateNauticalMileDistance(fix.getA(), request.getA());
            double c = halfConfidenceDistanceNauticalMiles;
            double y = c;
            distanceConfidence = c / (x + y);
        } else {
            distanceConfidence = 1;
        }
        return distanceConfidence * timeConfidence;
    }

    private double getApproximateNauticalMileDistance(PositionDTO p1, PositionDTO p2) {
        final double latDiffDeg = Math.abs(p1.latDeg - p2.latDeg);
        final double normalizedLngDiffDeg = cosineOfAverageLatitude * Math.abs(p1.lngDeg - p2.lngDeg);
        // One degree of latitude or one degree of longitude at the equator each correspond to 60 nautical miles.
        return Math.sqrt(latDiffDeg*latDiffDeg + normalizedLngDiffDeg*normalizedLngDiffDeg) / 60.;
    }
}
