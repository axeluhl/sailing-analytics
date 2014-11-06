package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

public class StartLineAdvantageDTO implements Serializable{
    
    private static final long serialVersionUID = 502350559068890424L;
    
    public double liveWindStartLineAdvantage;
    public double averageWindStartLineAdvantage;
    public double liveGeometricStartLineAdvantage;
    public double averageGeometricStartLineAdvantage;
    
    public StartLineAdvantageDTO(){}
}
