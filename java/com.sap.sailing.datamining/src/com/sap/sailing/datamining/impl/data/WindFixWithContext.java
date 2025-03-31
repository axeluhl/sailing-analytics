package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasWindFixContext;
import com.sap.sailing.datamining.data.HasWindTrackContext;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.impl.TimeRangeImpl;

/**
 * Equality is based on the {@link #getWind() wind fix} only.
 */
public class WindFixWithContext implements HasWindFixContext {
    private static final long serialVersionUID = -4537126043228674949L;

    private final HasWindTrackContext windTrackContext;
    
    private final String windSourceType;
    
    private final Wind wind;

    public WindFixWithContext(HasWindTrackContext windTrackContext, Wind wind, String windSourceType) {
        this.windTrackContext = windTrackContext;
        this.wind = wind;
        this.windSourceType = windSourceType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((wind == null) ? 0 : wind.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WindFixWithContext other = (WindFixWithContext) obj;
        if (wind == null) {
            if (other.wind != null)
                return false;
        } else if (!wind.equals(other.wind))
            return false;
        return true;
    }

    @Override
    public HasWindTrackContext getWindTrackContext() {
        return windTrackContext;
    }

    @Override
    public Wind getWind() {
        return wind;
    }
    
    @Override
    public double getWindFromDegrees() {
        return getWind().getFrom().getDegrees();
    }

    @Override
    public double getWindSpeedInKnots() {
        return getWind().getKnots();
    }

    @Override
    public String getWindSourceType() {
        return windSourceType;
    }

    @Override
    public boolean isInTrackingInterval() {
        final TrackedRace trackedRace = getWindTrackContext().getTrackedRaceContext().getTrackedRace();
        return trackedRace != null && new TimeRangeImpl(trackedRace.getStartOfTracking(), trackedRace.getEndOfTracking()).includes(getTimePoint());
    }

    @Override
    public boolean isInRace() {
        final TrackedRace trackedRace = getWindTrackContext().getTrackedRaceContext().getTrackedRace();
        return trackedRace != null && new TimeRangeImpl(trackedRace.getStartOfRace(), trackedRace.getEndOfRace()).includes(getTimePoint());
    }

    @Override
    public String toString() {
        return "WindFixWithContext [trackedRaceContext=" + windTrackContext + ", windSourceType=" + windSourceType
                + ", wind=" + wind + "]";
    }
}
