package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;

public class CompetitorRaceDataDTO implements IsSerializable {
    
    private CompetitorDTO competitor;
    private DetailType detailType;
    private long timePointOfNewestEvent; //TODO Javadoc
    /**
     * A: Bouy-Name; B: Timepoint; C: Data
     */
    private List<Triple<String, Long, Double>> markPassingsData;
    /**
     * A: Timepoint; B: Data
     */
    private List<Pair<Long, Double>> raceData;
    
    CompetitorRaceDataDTO() {}
    
    public CompetitorRaceDataDTO(CompetitorDTO competitor, DetailType detailType, 
            Collection<Triple<String, Long, Double>> markPassingsData, Collection<Pair<Long, Double>> raceData) {
        this.competitor = competitor;
        this.detailType = detailType;
        this.markPassingsData = new ArrayList<Triple<String, Long, Double>>();
        addAllMarkPassingsData(markPassingsData);
        this.raceData = new ArrayList<Pair<Long, Double>>();
        addAllRaceData(raceData);
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public DetailType getDetailType() {
        return detailType;
    }
    
    public long getTimePointOfNewestEvent() {
        return timePointOfNewestEvent;
    }

    public List<Triple<String, Long, Double>> getMarkPassingsData() {
        return Collections.unmodifiableList(markPassingsData);
    }
    
    public void addAllMarkPassingsData(Collection<Triple<String, Long, Double>> markPassingsDataToAdd) {
        for (Triple<String, Long, Double> data : markPassingsDataToAdd) {
            addMarkPassingsData(data);
        }
    }
    
    public void addMarkPassingsData(Triple<String, Long, Double> markPassingsDataToAdd) {
        getMarkPassingsData().add(markPassingsDataToAdd);
        //Updating the timePointOfNewestEvent
        if (markPassingsDataToAdd.getB() > timePointOfNewestEvent) {
            timePointOfNewestEvent = markPassingsDataToAdd.getB();
        }
    }

    public List<Pair<Long, Double>> getRaceData() {
        return Collections.unmodifiableList(raceData);
    }
    
    public void addAllRaceData(Collection<Pair<Long, Double>> raceDataToAdd) {
        for (Pair<Long, Double> data : raceDataToAdd) {
            addRaceData(data);
        }
    }
    
    public void addRaceData(Pair<Long, Double> raceDataToAdd) {
        getRaceData().add(raceDataToAdd);
        //Updating the timePointOfNewestEvent
        if (raceDataToAdd.getA() > timePointOfNewestEvent) {
            timePointOfNewestEvent = raceDataToAdd.getA();
        }
    }

    /**
     * Adds all data, if the detailTypes fit.
     * @param dataToAdd
     */
    public void addAllData(CompetitorRaceDataDTO dataToAdd) {
        if (detailType == dataToAdd.getDetailType()) {
            addAllMarkPassingsData(dataToAdd.getMarkPassingsData());
            addAllRaceData(dataToAdd.getRaceData());
        }
    }

}
