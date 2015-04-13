package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.Position;

public class WindBotComponentDTO implements Serializable{
    
    private static final long serialVersionUID = 7552196781982269601L;
    
    public String id;
    public double liveWindSpeedInKts;
    public double liveWindDirectionInDegrees;
    public double averageWindSpeedInKts;
    public double averageWindDirectionInDegrees;
    public Position position;
    
    public WindBotComponentDTO() {}
}
