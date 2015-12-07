package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.Comparator;

import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.gwt.dispatch.client.Result;

public class StartLineAdvantageDTO extends AverageDTO implements Result {
    
    public Double startLineAdvantage;
    public Double distanceToRCBoatInMeters;
    public StartlineAdvantageType startLineAdvatageType;
    public Double confidence;
    
    public StartLineAdvantageDTO(){}
    
    public static Comparator<StartLineAdvantageDTO> startlineAdvantageComparatorByAdvantageDesc = new Comparator<StartLineAdvantageDTO>() {
        public int compare(StartLineAdvantageDTO a, StartLineAdvantageDTO b) {
            return (int) (b.startLineAdvantage - a.startLineAdvantage);
        }

    };
    
    public static Comparator<StartLineAdvantageDTO> startlineAdvantageComparatorByDistanceToRCBoatAsc = new Comparator<StartLineAdvantageDTO>() {
        public int compare(StartLineAdvantageDTO a, StartLineAdvantageDTO b) {
            return (int) (a.distanceToRCBoatInMeters - b.distanceToRCBoatInMeters);
        }

    };
}
