package com.sap.sailing.dashboards.gwt.shared.dto.startanalysis;

import java.io.Serializable;

import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;

public class WindAndAdvantagesInfoForStartLineDTO implements Serializable{
    
    private static final long serialVersionUID = -1325150193180234561L;

    public double windDirectionInDegrees;
    public double windSpeedInKnots;
    public StartLineAdvantageDTO startLineAdvantage;
}
