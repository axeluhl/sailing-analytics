package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;

public class CompetitorRaceDataDTO implements IsSerializable {
    
    private CompetitorDTO competitor;
    private DetailType detailType;
    private Date dateOfNewestData; //TODO Javadoc
    /**
     * A: Bouy-Name; B: Data-Date; C: Data
     */
    private List<Triple<String, Date, Double>> markPassingsData;
    /**
     * A: Data-Date; B: Data
     */
    private List<Pair<Date, Double>> raceData;
    
    CompetitorRaceDataDTO() {}
    
    public CompetitorRaceDataDTO(CompetitorDTO competitor, DetailType detailType, 
            Collection<Triple<String, Date, Double>> markPassingsData, Collection<Pair<Date, Double>> raceData) {
        this.competitor = competitor;
        this.detailType = detailType;
        this.markPassingsData = new ArrayList<Triple<String, Date, Double>>();
        addAllMarkPassingsData(markPassingsData);
        this.raceData = new ArrayList<Pair<Date, Double>>();
        addAllRaceData(raceData);
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public DetailType getDetailType() {
        return detailType;
    }
    
    public Date getDateOfNewestData() {
        return dateOfNewestData;
    }

    public List<Triple<String, Date, Double>> getMarkPassingsData() {
        return Collections.unmodifiableList(markPassingsData);
    }
    
    public void addAllMarkPassingsData(Collection<Triple<String, Date, Double>> markPassingsDataToAdd) {
        for (Triple<String, Date, Double> data : markPassingsDataToAdd) {
            addMarkPassingsData(data);
        }
    }
    
    public void addMarkPassingsData(Triple<String, Date, Double> markPassingsDataToAdd) {
        getMarkPassingsData().add(markPassingsDataToAdd);
        //Updating the timePointOfNewestEvent
        if (markPassingsDataToAdd.getB().after(dateOfNewestData)) {
            dateOfNewestData = markPassingsDataToAdd.getB();
        }
    }

    public List<Pair<Date, Double>> getRaceData() {
        return Collections.unmodifiableList(raceData);
    }
    
    public void addAllRaceData(Collection<Pair<Date, Double>> raceDataToAdd) {
        for (Pair<Date, Double> data : raceDataToAdd) {
            addRaceData(data);
        }
    }
    
    public void addRaceData(Pair<Date, Double> raceDataToAdd) {
        getRaceData().add(raceDataToAdd);
        //Updating the timePointOfNewestEvent
        if (raceDataToAdd.getA().after(dateOfNewestData)) {
            dateOfNewestData = raceDataToAdd.getA();
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
