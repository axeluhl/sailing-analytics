package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;

public class StartLineAdvantageDTO implements Serializable{
    
    private static final long serialVersionUID = 502350559068890424L;
    
    public StartlineAdvantageType startLineAdvatageType;
    public double startLineAdvantage;
    public double startlineAdvantageAverage;
    
    public StartLineAdvantageDTO(){}
}
