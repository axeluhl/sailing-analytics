package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;

public class CompetitorRaceDataDTO implements IsSerializable {
    
    private CompetitorDTO competitor;
    private DetailType detailType;
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
        this.markPassingsData = new ArrayList<Triple<String, Long, Double>>(markPassingsData);
        this.raceData = new ArrayList<Pair<Long, Double>>(raceData);
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public void setCompetitor(CompetitorDTO competitor) {
        this.competitor = competitor;
    }

    public DetailType getDetailType() {
        return detailType;
    }

    /**
     * Calculates the time point of the newest event out of the containing data.<br />
     * Therefore a sorting of the data lists is needed, so use as rare as possible.<br />
     * After calling this methods, the containing data is sorted by time.
     * @return The time point of the newest event
     */
    public long getTimePointOfNewestEvent() {
        Collections.sort(markPassingsData, new MarkPassingsDataComparatorByTime());
        Collections.sort(raceData, new RaceDataComparatorByTime());
        Long newestMarkPassingsData = markPassingsData.get(markPassingsData.size() - 1).getB();
        Long newestRaceData = raceData.get(raceData.size() - 1).getA();
        return newestRaceData >= newestMarkPassingsData ? newestRaceData : newestMarkPassingsData;
    }
    
    /**
     * Calculates the earliest timepoint out of the containing data.<br />
     * Therefore a sorting of the data lists is needed, so use as rare as possible.<br />
     * After calling this methods, the containing data is sorted by time.
     * @return The earliest time in the data
     */
    public long getStartTime() {
        Collections.sort(markPassingsData, new MarkPassingsDataComparatorByTime());
        Collections.sort(raceData, new RaceDataComparatorByTime());
        Long newestMarkPassingsData = markPassingsData.get(0).getB();
        Long newestRaceData = raceData.get(0).getA();
        return newestRaceData <= newestMarkPassingsData ? newestRaceData : newestMarkPassingsData;
    }

    public List<Triple<String, Long, Double>> getMarkPassingsData() {
        return markPassingsData;
    }

    public void setMarkPassingsData(Collection<Triple<String, Long, Double>> markPassingsData) {
        this.markPassingsData = new ArrayList<Triple<String, Long, Double>>(markPassingsData);
    }
    
    public void addMarkPassingsData(Collection<Triple<String, Long, Double>> markPassingsDataToAdd) {
        getMarkPassingsData().addAll(markPassingsDataToAdd);
    }

    public List<Pair<Long, Double>> getRaceData() {
        return raceData;
    }

    public void setRaceData(Collection<Pair<Long, Double>> raceData) {
        this.raceData = new ArrayList<Pair<Long, Double>>(raceData);
    }
    
    public void addRaceData(Collection<Pair<Long, Double>> raceData) {
        getRaceData().addAll(raceData);
    }

    public void addAllData(CompetitorRaceDataDTO dataToAdd) {
        addMarkPassingsData(dataToAdd.getMarkPassingsData());
        addRaceData(dataToAdd.getRaceData());
    }
    
    public class MarkPassingsDataComparatorByTime implements Comparator<Triple<String, Long, Double>> {
        @Override
        public int compare(Triple<String, Long, Double> t1, Triple<String, Long, Double> t2) {
            return t1.getB().compareTo(t2.getB());
        }
    }
    
    public class RaceDataComparatorByTime implements Comparator<Pair<Long, Double>> {
        @Override
        public int compare(Pair<Long, Double> p1, Pair<Long, Double> p2) {
            return p1.getA().compareTo(p2.getA());
        }
    }

}
