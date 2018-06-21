package com.sap.sailing.gwt.ui.shared;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public class CompetitorsRaceDataDTO implements IsSerializable {
    
    private DetailType detailType;
    private HashMap<CompetitorDTO, CompetitorRaceDataDTO> competitorsData;
    private Date requestedFromTime;
    private Date requestedToTime;
    
    CompetitorsRaceDataDTO() {}
    
    public CompetitorsRaceDataDTO(DetailType detailType, Date requestedFromTime, Date requestedToTime) {
        this.detailType = detailType;
        this.requestedFromTime = requestedFromTime;
        this.requestedToTime = requestedToTime;
        this.competitorsData = new HashMap<>();
    }
    
    public CompetitorsRaceDataDTO(DetailType detailType, HashMap<CompetitorDTO, CompetitorRaceDataDTO> raceData) {
        this.detailType = detailType;
        this.competitorsData = new HashMap<CompetitorDTO, CompetitorRaceDataDTO>(raceData);
    }
    
    public Set<CompetitorDTO> getCompetitors() {
        return competitorsData.keySet();
    }
    
    public CompetitorRaceDataDTO getCompetitorData(CompetitorDTO competitor) {
        return competitorsData.get(competitor);
    }
    
    /**
     * Sets the data for a competitor, if the <code>detailTypes</code> fit.<br />
     * If the competitor is already contained, the data will be overwritten.
     */
    public void setCompetitorData(CompetitorDTO competitor, CompetitorRaceDataDTO competitorData) {
        if (competitorData != null && detailType == competitorData.getDetailType()) {
            this.competitorsData.put(competitor, competitorData);
        }
    }
    
    /**
     * Replaces the {@link CompetitorRaceDataDTO#markPassingsData} from the {@link CompetitorRaceDataDTO data} of the
     * {@link CompetitorWithBoatDTO} in <code>competitorData</code> with the markPassingsData in <code>competitorData</code>.<br />
     * If the competitor is not contained, nothing happens.
     * 
     * @param competitorData
     */
    public void setCompetitorMarkPassingsData(CompetitorRaceDataDTO competitorData) {
        if (detailType == competitorData.getDetailType()) {
            if (competitorsData.containsKey(competitorData.getCompetitor())) {
                competitorsData.get(competitorData.getCompetitor()).setMarkPassingsData(competitorData.getMarkPassingsData());
            }
        }
    }
    
    /**
     * Adds all {@link CompetitorRaceDataDTO#raceData} in <code>competitorDataToAdd</code> to the existing data of the
     * {@link CompetitorWithBoatDTO} in <code>competitorDataToAdd</code>, if the {@link DetailType DetailTypes} fit.<br />
     * If the competitor is not contained, nothing happens.
     */
    public void addCompetitorRaceData(CompetitorRaceDataDTO competitorDataToAdd) {
        if (detailType == competitorDataToAdd.getDetailType()) {
            if (competitorsData.containsKey(competitorDataToAdd.getCompetitor())) {
                competitorsData.get(competitorDataToAdd.getCompetitor()).addAllRaceData(competitorDataToAdd.getRaceData());
            }
        }
    }
    
    public Collection<CompetitorRaceDataDTO> getAllRaceData() {
        return competitorsData.values();
    }
    
    public DetailType getDetailType() {
        return detailType;
    }

    public Date getDateOfNewestData() {
        Date dateOfNewestData = null;
        for (CompetitorRaceDataDTO competitorRaceData : competitorsData.values()) {
            Date raceDateOfNewestData = competitorRaceData.getDateOfNewestData();
            if (dateOfNewestData == null) {
                dateOfNewestData = raceDateOfNewestData;
            } else {
                dateOfNewestData = (dateOfNewestData.after(raceDateOfNewestData) || raceDateOfNewestData == null) ? dateOfNewestData
                        : raceDateOfNewestData;
            }
        }
        return dateOfNewestData;
    }

    public Date getOldestDateOfNewestData() {
        Date dateOfNewestData = null;
        for (CompetitorRaceDataDTO competitorRaceData : competitorsData.values()) {
            Date raceDateOfNewestData = competitorRaceData.getDateOfNewestData();
            if (dateOfNewestData == null) {
                dateOfNewestData = raceDateOfNewestData;
            } else {
                dateOfNewestData = (dateOfNewestData.before(raceDateOfNewestData) || raceDateOfNewestData == null) ? dateOfNewestData
                        : raceDateOfNewestData;
            }
        }
        return dateOfNewestData;
    }

    public boolean isEmpty() {
        for (CompetitorRaceDataDTO competitorRaceData : competitorsData.values()) {
            if (!competitorRaceData.getRaceData().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(CompetitorWithBoatDTO competitor) {
        return competitorsData.containsKey(competitor);
    }

    public Date getRequestedFromTime() {
        return requestedFromTime;
    }

    public Date getRequestedToTime() {
        return requestedToTime;
    }
    
}
