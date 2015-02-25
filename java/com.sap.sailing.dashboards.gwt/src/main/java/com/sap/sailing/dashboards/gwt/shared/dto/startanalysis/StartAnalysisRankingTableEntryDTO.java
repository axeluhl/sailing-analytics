package com.sap.sailing.dashboards.gwt.shared.dto.startanalysis;

import java.io.Serializable;

public class StartAnalysisRankingTableEntryDTO implements Serializable{
    
    private static final long serialVersionUID = -1325150193180234561L;
    
    public int rankAtFirstMark;
    public String teamName;
    public double speedAtStartTime;
    public double distanceToLineAtStartTime;
    public String tailColor;
}
