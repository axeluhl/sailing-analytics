package com.sap.sailing.datamining.data;

import java.util.Locale;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.Timed;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface HasWind extends HasTrackedLegOfCompetitor, Positioned, Timed {
    @Dimension(messageKey="WindStrengthInBeaufort", ordinal=11)
    default ClusterDTO getWindStrengthAsBeaufortCluster(Locale locale, ResourceBundleStringMessages stringMessages) {
        Wind wind = getWind();
        Cluster<?> cluster = Activator.getClusterGroups().getWindStrengthInBeaufortClusterGroup().getClusterFor(wind);
        return new ClusterDTO(cluster.asLocalizedString(locale, stringMessages));
    }
    
    default Wind getWind() {
        if (getWindInternal() == null) {
            setWindInternal(getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getTrackedRace()
                    .getWind(getPosition(), getTimePoint()));
        }
        return getWindInternal();
    }
    
    Wind getWindInternal();
    
    void setWindInternal(Wind wind);
}
