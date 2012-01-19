package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorInRaceDTO implements IsSerializable {
    private HashMap<CompetitorDTO, Double[]> raceData;
    private HashMap<CompetitorDTO, Double[]> markPassings;
    
    public CompetitorInRaceDTO(){
        raceData = new HashMap<CompetitorDTO, Double[]>();
        markPassings = new HashMap<CompetitorDTO, Double[]>();
    }

    public void setRaceData(CompetitorDTO competitor, Double[] data){
        raceData.put(competitor, data);
    }
    
    public void setMarkPassingData(CompetitorDTO competitor, Double[] data){
        markPassings.put(competitor, data);
    }
    
    public Double[] getRaceData(CompetitorDTO competitor){
        return raceData.get(competitor);
    }
    
    public Double[] getMarkPassings(CompetitorDTO competitor){
        return markPassings.get(competitor);
    }
}
