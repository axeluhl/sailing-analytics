package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;

public class LiveRaceDTO extends RaceMetadataDTO {
    
    private FlagStateDTO flagState;
    
    private SimpleWindDTO wind;
    
    private int course;
    
    private Integer totalLegs;
    private Integer currentLeg;
    
    private String boatClass;

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(RegattaAndRaceIdentifier id) {
        super(id);
    }

    public SimpleWindDTO getWind() {
        return wind;
    }

    public void setWind(SimpleWindDTO wind) {
        this.wind = wind;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public Integer getTotalLegs() {
        return totalLegs;
    }

    public void setTotalLegs(Integer totalLegs) {
        this.totalLegs = totalLegs;
    }

    public Integer getCurrentLeg() {
        return currentLeg;
    }

    public void setCurrentLeg(Integer currentLeg) {
        this.currentLeg = currentLeg;
    }

    public FlagStateDTO getFlagState() {
        return flagState;
    }

    public void setFlagState(FlagStateDTO flagState) {
        this.flagState = flagState;
    }

    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }
}
