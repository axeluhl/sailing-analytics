package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.Comparator;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;

public class StartLineAdvantageDTO extends AverageDTO implements IsSerializable {
    
    public double startLineAdvantage;
    public double distanceToRCBoatInMeters;
    public StartlineAdvantageType startLineAdvatageType;
    public double confidence;
    
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
