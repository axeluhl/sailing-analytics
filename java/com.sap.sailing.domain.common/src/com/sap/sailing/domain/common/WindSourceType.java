package com.sap.sailing.domain.common;

/**
 * Possible sources for wind data. Used to key and select between different {@link WindTrack}s. Literals
 * are given in descending order of precedence. Particularly, the {@link #COURSE_BASED} source should
 * really only be used if nothing else is known about the wind.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum WindSourceType {
    /**
     * Manually entered via a web form or received through a REST service call, e.g., from BeTomorrow's estimation
     */
    WEB(true, 0.9),
    
    /**
     * Measured using wind sensors
     */
    EXPEDITION(true, 0.9),
    
    /**
     * Estimates wind conditions by analyzing the boat tracks; may not have results for all time points, e.g.,
     * because at a given time point all boats may sail on the same tack and hence no averaging between the
     * two tacks is possible. This is the more likely to happen the smaller the fleet tracked is.
     */
    TRACK_BASED_ESTIMATION(false, 0.5),

    /**
     * Inferred from the race course layout if the course is known to have its first leg be an upwind leg
     */
    COURSE_BASED(false, 0.3),
    
    /**
     * Wind estimation combined from all other wind sources, using <code>TrackedRace.getWind(...)</code>, based on
     * confidences
     */
    COMBINED(false, 0.9);
    
    private final boolean canBeStored;
    
    private final double baseConfidence;
    
    private WindSourceType(boolean canBeStored, double baseConfidence) {
        this.canBeStored = canBeStored;
        this.baseConfidence = baseConfidence;
    }
    
    public boolean canBeStored() {
        return canBeStored;
    }
    
    public double getBaseConfidence() {
        return baseConfidence;
    }
    
}
