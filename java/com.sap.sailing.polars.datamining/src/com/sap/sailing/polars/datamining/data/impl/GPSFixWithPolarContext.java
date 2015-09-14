package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasGPSFixPolarContext;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public class GPSFixWithPolarContext implements HasGPSFixPolarContext {

    private final GPSFixMoving fix;
    private final TrackedRace trackedRace;
    private final ClusterGroup<Speed> windSpeedRangeGroup;

    public GPSFixWithPolarContext(GPSFixMoving fix, TrackedRace trackedRace, ClusterGroup<Speed> windSpeedRangeGroup) {
        this.fix = fix;
        this.trackedRace = trackedRace;
        this.windSpeedRangeGroup = windSpeedRangeGroup;
    }

    @Override
    public ClusterDTO getWindSpeedRange() {
        //TODO exclude wind sources and stuff
        Wind wind = trackedRace.getWind(fix.getPosition(), fix.getTimePoint());
        return new ClusterDTO(windSpeedRangeGroup.getClusterFor(wind).toString());
    }

}
