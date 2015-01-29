package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.LegType;

public class StartLineAdvantageDTO implements Serializable{
    
    private static final long serialVersionUID = 502350559068890424L;
    
    public LegType legTypeOfFirstLegInTrackedRace;
    public double liveWindStartLineAdvantage;
    public double averageWindStartLineAdvantage;
    public double liveGeometricStartLineAdvantage;
    public double averageGeometricStartLineAdvantage;
    
    public StartLineAdvantageDTO(){}
}
