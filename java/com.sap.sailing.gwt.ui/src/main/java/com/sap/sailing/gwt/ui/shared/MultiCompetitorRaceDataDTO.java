package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MultiCompetitorRaceDataDTO implements IsSerializable {
    
    private long startTime;
    private HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData;
    
    MultiCompetitorRaceDataDTO() {}
    
    public MultiCompetitorRaceDataDTO(long startTime) {
        this.startTime = startTime;
        this.raceData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>();
    }
    
    public MultiCompetitorRaceDataDTO(long startTime, HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData) {
        this.startTime = startTime;
        this.raceData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>(raceData);
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public CompetitorRaceDataDTO getCompetitorRaceData(CompetitorDTO competitor) {
        return raceData.get(competitor);
    }
    
    public void setCompetitorRaceData(CompetitorDTO competitor, CompetitorRaceDataDTO raceData) {
        this.raceData.put(competitor, raceData);
    }
    
    public void addCompetitorRaceData(CompetitorDTO competitor, CompetitorRaceDataDTO raceDataToAdd) {
        getCompetitorRaceData(competitor).addAllData(raceDataToAdd);
    }
    
    public HashMap<CompetitorDTO, CompetitorRaceDataDTO> getAllRaceData() {
        return raceData;
    }
    
    public void setAllRaceData(HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData) {
        this.raceData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>(raceData);
    }
    
    public long getTimePointOfNewestEvent() {
        long result = 0;
        for (CompetitorRaceDataDTO competitorRaceData : raceData.values()) {
            long raceDataNewestEvent = competitorRaceData.getTimePointOfNewestEvent();
            result = result >= raceDataNewestEvent ? result : raceDataNewestEvent;
        }
        return result;
    }
    
}
