package com.sap.sailing.declination;

import com.sap.sailing.declination.impl.DeclinationServiceImpl;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.CentralAngleDistance;

public interface DeclinationService {
    /**
     * A default implementation with a spatial default precision of 1 {@link Mile#METERS_PER_GEOGRAPHICAL_MILE
     * geographical mile} which equals the length of an arc with one degree on a meridian.
     */
    DeclinationService INSTANCE = new DeclinationServiceImpl(new CentralAngleDistance(1./180.*Math.PI));
    
    /**
     * Obtains declination information with the default precision of this declination service in time and space
     * dimensions (e.g., one degree N/S and E/W and one year on the time axis). Particularly the time resolution may
     * benefit from an extrapolation using the {@link DeclinationRecord#getAnnualChange() annual change} specification.
     */
    DeclinationRecord getDeclination(TimePoint timePoint, Position position);
    
    /**
     * Like {@link #getDeclination(TimePoint, Position)}, only that here the caller can specify a required
     * spatial precision which may be higher than the default precision. Increased precision may require an
     * online lookup if no stored value exists for this combination of time/position yet.<p>
     * 
     * The time point precision is the default precision of this service, again corrected automatically by the
     * {@link DeclinationRecord#getAnnualChange() annual change} of the result found for the position.
     */
    DeclinationRecord getDeclination(TimePoint timePoint, Position position, Distance maxDistance);
}
