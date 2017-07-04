package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.data.HasWindTrackContext;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;

public class WindTrackWithContext implements HasWindTrackContext {

    private final HasTrackedRaceContext trackedRaceContext;
    
    private final String  windSourceType;
    
    private final String windSourceName;
    
    private final WindTrack windTrack;
    
    public WindTrackWithContext(HasTrackedRaceContext trackedRaceContext, WindTrack windTrack, WindSource windSource) {
        this.trackedRaceContext = trackedRaceContext;
        this.windTrack = windTrack;
        this.windSourceType = windSource.getType().name();
        this.windSourceName = windSource.getType().name()+(windSource.getId()==null?"":" ("+windSource.getId()+")");
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
