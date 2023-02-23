package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.data.HasWindTrackContext;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Equality is based on the identity of the {@link #getWindTrack() wind track} only.
 */
public class WindTrackWithContext implements HasWindTrackContext {

    private final HasTrackedRaceContext trackedRaceContext;
    
    private final String windSourceType;
    
    private final String windSourceName;
    
    private final WindTrack windTrack;
    
    public WindTrackWithContext(HasTrackedRaceContext trackedRaceContext, WindTrack windTrack, WindSource windSource) {
        this.trackedRaceContext = trackedRaceContext;
        this.windTrack = windTrack;
        this.windSourceType = windSource.getType().name();
        this.windSourceName = windSource.getType().name()+(windSource.getId()==null?"":" ("+windSource.getId()+")");
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((windTrack == null) ? 0 : windTrack.hashCode());
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
        WindTrackWithContext other = (WindTrackWithContext) obj;
        if (windTrack == null) {
            if (other.windTrack != null)
                return false;
        } else if (!windTrack.equals(other.windTrack))
            return false;
        return true;
    }

    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }

    @Override
    public String getWindSourceType() {
        return windSourceType;
    }

    @Override
    public String toString() {
        return "WindTrackWithContext [trackedRaceContext=" + trackedRaceContext + ", windSourceType=" + windSourceType
                + ", windSourceName=" + windSourceName + "]";
    }

    @Override
    public String getWindSourceName() {
        return windSourceName;
    }

    @Override
    public WindTrack getWindTrack() {
        return windTrack;
    }

}
