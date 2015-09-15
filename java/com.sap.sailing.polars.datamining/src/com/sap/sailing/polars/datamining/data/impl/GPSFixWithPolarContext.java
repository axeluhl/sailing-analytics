package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasCompetitorPolarContext;
import com.sap.sailing.polars.datamining.data.HasGPSFixPolarContext;
import com.sap.sailing.polars.datamining.shared.PolarStatistic;
import com.sap.sailing.polars.datamining.shared.PolarStatisticImpl;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public class GPSFixWithPolarContext implements HasGPSFixPolarContext {

    private final GPSFixMoving fix;
    private final TrackedRace trackedRace;
    private final ClusterGroup<Speed> windSpeedRangeGroup;
    private final Competitor competitor;
    private final PolarSheetGenerationSettings settings;
    private final HasCompetitorPolarContext competitorPolarContext;

    public GPSFixWithPolarContext(GPSFixMoving fix, TrackedRace trackedRace, ClusterGroup<Speed> windSpeedRangeGroup, Competitor competitor,
            PolarSheetGenerationSettings settings, HasCompetitorPolarContext competitorPolarContext) {
        this.fix = fix;
        this.trackedRace = trackedRace;
        this.windSpeedRangeGroup = windSpeedRangeGroup;
        this.competitor = competitor;
        this.settings = settings;
        this.competitorPolarContext = competitorPolarContext;
    }

    @Override
    public ClusterDTO getWindSpeedRange() {
        //TODO exclude wind sources and stuff
        Wind wind = trackedRace.getWind(fix.getPosition(), fix.getTimePoint());
        return new ClusterDTO(windSpeedRangeGroup.getClusterFor(wind).toString());
    }

    @Override
    public PolarStatistic getPolarStatistics() {
        return new PolarStatisticImpl(trackedRace, competitor, fix, settings);
    }

    @Override
    public HasCompetitorPolarContext getCompetitorPolarContext() {
        return competitorPolarContext;
    }

}
