package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

public class WindAndAdvantagesInfoForStartLineDTO implements Serializable{
    
    private static final long serialVersionUID = -1325150193180234561L;

    public double windDirectionInDegrees;
    public double windSpeedInKnots;
    public StartLineAdvantageDTO startLineAdvantage;
}
