package com.sap.sailing.dashboards.gwt.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;

public class StartLineAdvantageDTO implements IsSerializable {
    
    public StartlineAdvantageType startLineAdvatageType;
    public double startLineAdvantage;
    public double startlineAdvantageAverage;
    public double distanceToRCBoatInMeters;
    
    public StartLineAdvantageDTO(){}
}
