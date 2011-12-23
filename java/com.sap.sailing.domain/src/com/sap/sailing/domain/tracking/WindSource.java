package com.sap.sailing.domain.tracking;

/**
 * Possible sources for wind data. Used to key and select between different {@link WindTrack}s.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum WindSource {
    /**
     * Manually entered via a web form or received through a REST service call, e.g., from BeTomorrow's estimation
     */
    WEB,
    
    /**
     * Measured using wind sensors
     */
    EXPEDITION,
    
    /**
     * Inferred from the race course layout if the course is known to have its first leg be an upwind leg
     */
    COURSE_BASED,
    
    /**
     * Estimates wind conditions by analyzing the boat tracks; may not have results for all time points, e.g.,
     * because at a given time point all boats may sail on the same tack and hence no averaging between the
     * two tacks is possible. This is the more likely to happen the smaller the fleet tracked is.
     */
    TRACK_BASED_ESTIMATION
}
