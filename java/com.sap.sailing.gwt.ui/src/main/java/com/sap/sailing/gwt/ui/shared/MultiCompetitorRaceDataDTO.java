package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MultiCompetitorRaceDataDTO implements IsSerializable {
    
    private HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData;
    
    public MultiCompetitorRaceDataDTO() {
        this(new HashMap<CompetitorDTO, CompetitorRaceDataDTO>());
    }
    
    public MultiCompetitorRaceDataDTO(HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData) {
        this.raceData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>(raceData);
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


    /**
     * Calculates the time point of the newest event out of the containing data.<br />
     * Therefore a sorting of the data lists is needed, so use as rare as possible.<br />
     * After calling this methods, the containing data is sorted by time.
     * @return The time point of the newest event or -1 if no data is contained
     */
    public long getTimePointOfNewestEvent() {
        long result = -1;
        for (CompetitorRaceDataDTO competitorRaceData : raceData.values()) {
            long raceDataNewestEvent = competitorRaceData.getTimePointOfNewestEvent();
            result = result >= raceDataNewestEvent ? result : raceDataNewestEvent;
        }
        return result;
    }

    
    /**
     * Calculates the earliest timepoint out of the containing data.<br />
     * Therefore a sorting of the data lists is needed, so use as rare as possible.<br />
     * After calling this methods, the containing data is sorted by time.
     * @return The earliest time in the data or -1 if no data is contained
     */
    public long getStartTime() {
        long result = -1;
        for (CompetitorRaceDataDTO competitorRaceData : raceData.values()) {
            long raceDataNewestEvent = competitorRaceData.getStartTime();
            result = result <= raceDataNewestEvent ? result : raceDataNewestEvent;
        }
        return result;
    }
    
}
