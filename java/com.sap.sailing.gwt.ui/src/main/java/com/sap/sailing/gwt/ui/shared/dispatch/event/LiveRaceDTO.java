package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;

public class LiveRaceDTO extends RaceMetadataDTO {

    private Flags lastUpperFlag;
    private Flags lastLowerFlag;
    private boolean lastFlagsAreDisplayed;
    private boolean lastFlagsDisplayedStateChanged;
    
    private Double trueWindFromDeg;
    private Double trueWindSpeedInKnots;
    
    private int course;
    // TODO status

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(RegattaAndRaceIdentifier id) {
        super(id);
    }

    public Flags getLastUpperFlag() {
        return lastUpperFlag;
    }

    public void setLastUpperFlag(Flags lastUpperFlag) {
        this.lastUpperFlag = lastUpperFlag;
    }

    public Flags getLastLowerFlag() {
        return lastLowerFlag;
    }

    public void setLastLowerFlag(Flags lastLowerFlag) {
        this.lastLowerFlag = lastLowerFlag;
    }

    public boolean isLastFlagsAreDisplayed() {
        return lastFlagsAreDisplayed;
    }

    public void setLastFlagsAreDisplayed(boolean lastFlagsAreDisplayed) {
        this.lastFlagsAreDisplayed = lastFlagsAreDisplayed;
    }

    public boolean isLastFlagsDisplayedStateChanged() {
        return lastFlagsDisplayedStateChanged;
    }

    public void setLastFlagsDisplayedStateChanged(boolean lastFlagsDisplayedStateChanged) {
        this.lastFlagsDisplayedStateChanged = lastFlagsDisplayedStateChanged;
    }

    public Double getTrueWindFromDeg() {
        return trueWindFromDeg;
    }

    public void setTrueWindFromDeg(Double trueWindFromDeg) {
        this.trueWindFromDeg = trueWindFromDeg;
    }

    public Double getTrueWindSpeedInKnots() {
        return trueWindSpeedInKnots;
    }

    public void setTrueWindSpeedInKnots(Double trueWindSpeedInKnots) {
        this.trueWindSpeedInKnots = trueWindSpeedInKnots;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }
}
