package com.sap.sailing.gwt.ui.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.DetailType;

public class MultiCompetitorRaceDataDTO implements IsSerializable {
    
    private DetailType detailType;
    private HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData;
    
    MultiCompetitorRaceDataDTO() {}
    
    public MultiCompetitorRaceDataDTO(DetailType detailType) {
        this.detailType = detailType;
        this.raceData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>();
    }
    
    public MultiCompetitorRaceDataDTO(DetailType detailType, HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData) {
        this.detailType = detailType;
        this.raceData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>(raceData);
    }
    
    public Set<CompetitorDTO> getCompetitors() {
        return raceData.keySet();
    }
    
    public CompetitorRaceDataDTO getCompetitorRaceData(CompetitorDTO competitor) {
        return raceData.get(competitor);
    }
    
    /**
     * Sets the data for a competitor, if the <code>detailTypes</code> fit.<br />
     * If the competitor is already contained, the data will be overwritten.
     */
    public void setCompetitorRaceData(CompetitorDTO competitor, CompetitorRaceDataDTO raceData) {
        if (detailType == raceData.getDetailType()) {
            this.raceData.put(competitor, raceData);
        }
    }
    
    /**
     * Adds all data in <code>raceDataToAdd</code> to the existing data of the <code>competitor</code>, if the <code>detailTypes</code> fit.<br />
     * If the <code>competitor</code> is not contained, the data will be
     * {@link MultiCompetitorRaceDataDTO#setCompetitorRaceData(CompetitorDTO, CompetitorRaceDataDTO) set}.
     */
    public void addCompetitorRaceData(CompetitorDTO competitor, CompetitorRaceDataDTO raceDataToAdd) {
        if (detailType == raceDataToAdd.getDetailType()) {
            if (raceData.containsKey(competitor)) {
                raceData.get(competitor).addAllData(raceDataToAdd);
            } else {
                raceData.put(competitor, raceDataToAdd);
            }
        }
    }
    
    public Collection<CompetitorRaceDataDTO> getAllRaceData() {
        return raceData.values();
    }
    
    /**
     * Adds all data in <code>dataToAdd</code>, if the <code>detailTypes</code> fit.<br />
     * The data which are not contained, will be
     * {@link MultiCompetitorRaceDataDTO#setCompetitorRaceData(CompetitorDTO, CompetitorRaceDataDTO) set}.
     * 
     * @param dataToAdd
     */
    public void addAllRaceData(MultiCompetitorRaceDataDTO dataToAdd) {
        //The check if the detailType fits will be done by addCompetitorRaceDataDTO
        for (CompetitorRaceDataDTO competitorDataToAdd : dataToAdd.getAllRaceData()) {
            addCompetitorRaceData(competitorDataToAdd.getCompetitor(), competitorDataToAdd);
        }
    }
    
    public DetailType getDetailType() {
        return detailType;
    }

    public long getTimePointOfNewestEvent() {
        long result = -1;
        for (CompetitorRaceDataDTO competitorRaceData : raceData.values()) {
            long raceDataNewestEvent = competitorRaceData.getTimePointOfNewestEvent();
            result = result >= raceDataNewestEvent ? result : raceDataNewestEvent;
        }
        return result;
    }

    public boolean isEmpty() {
        return raceData.isEmpty();
    }

    public boolean contains(CompetitorDTO competitor) {
        return raceData.containsKey(competitor);
    }
    
}
