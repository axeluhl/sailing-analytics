package com.sap.sailing.datamining.data;

import java.util.Locale;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface HasGPSFixContext {
    
    @Connector
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    public GPSFixMoving getGPSFix();
    
    @Dimension(messageKey="WindStrengthInBeaufort", ordinal=11)
    public ClusterDTO getWindStrengthAsBeaufortCluster(Locale locale, ResourceBundleStringMessages stringMessages);

}