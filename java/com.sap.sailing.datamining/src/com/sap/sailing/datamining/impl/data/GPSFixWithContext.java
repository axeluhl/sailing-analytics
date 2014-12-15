package com.sap.sailing.datamining.impl.data;

import java.util.Locale;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public class GPSFixWithContext implements HasGPSFixContext {
    
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    
    private final GPSFixMoving gpsFix;
    private Wind wind;

    public GPSFixWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext, GPSFixMoving gpsFix) {
        this.trackedLegOfCompetitorContext = trackedLegOfCompetitorContext;
        this.gpsFix = gpsFix;
    }
    
    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitorContext;
    }

    @Override
    public GPSFixMoving getGPSFix() {
        return gpsFix;
    }
    
    @Override
    public ClusterDTO getWindStrengthAsBeaufortCluster() {
        Wind wind = getWind();
        Cluster<?> cluster = Activator.getDefault().getClusterGroups().getWindStrengthInBeaufortCluster().getClusterFor(wind);
        return new ClusterDTO(cluster.getAsLocalizedString(Locale.ENGLISH, Activator.getDefault().getStringMessages()));
    }

    private Wind getWind() {
        if (wind == null) {
            wind = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getTrackedRace()
                    .getWind(gpsFix.getPosition(), gpsFix.getTimePoint());
        }
        return wind;
    }
    
}