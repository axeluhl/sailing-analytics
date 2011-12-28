package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorInRaceDAO implements IsSerializable {
    private HashMap<CompetitorDAO, Double[]> raceData;
    private HashMap<CompetitorDAO, Double[]> markPassings;
    
    public CompetitorInRaceDAO(){
        raceData = new HashMap<CompetitorDAO, Double[]>();
        markPassings = new HashMap<CompetitorDAO, Double[]>();
    }

    public void setRaceData(CompetitorDAO competitor, Double[] data){
        raceData.put(competitor, data);
    }
    
    public void setMarkPassingData(CompetitorDAO competitor, Double[] data){
        markPassings.put(competitor, data);
    }
    
    public Double[] getRaceData(CompetitorDAO competitor){
        return raceData.get(competitor);
    }
    
    public Double[] getMarkPassings(CompetitorDAO competitor){
        return markPassings.get(competitor);
    }
}
