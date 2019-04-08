package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;
import java.util.Date;

public class SwissTimingRaceRecordDTO extends AbstractRaceRecordDTO {
    public String raceId;
    public Date raceStartTime;
    public String regattaName;
    public String seriesName;
    public String fleetName;
    public String raceStatus;
    public String xrrEntriesUrl;

    public String boatClass;
    public String gender;

    SwissTimingRaceRecordDTO() {} // for serialization only

    public SwissTimingRaceRecordDTO(String raceId, String raceName, String regattaName, String seriesName,
            String fleetName, String raceStatus, Date raceStartTime, String xrrEntriesUrl, boolean hasRememberedRegatta) {
        super(raceName, hasRememberedRegatta);
        this.raceId = raceId;
        this.regattaName = regattaName;
        this.seriesName = seriesName;
        this.fleetName = fleetName;
        this.raceStatus = raceStatus;
        this.raceStartTime = raceStartTime;
        this.xrrEntriesUrl = xrrEntriesUrl;
    }

    @Override
    public Iterable<String> getBoatClassNames() {
        return Collections.singleton(boatClass);
    }

    @Override
    public String toString() {
        return "SwissTimingRaceRecordDTO [raceId=" + raceId + ", raceName=" + getName() + ", raceStartTime="
                + raceStartTime + ", regattaName=" + regattaName + ", seriesName=" + seriesName + ", fleetName="
                + fleetName + ", raceStatus=" + raceStatus + ", boatClass=" + boatClass + ", gender=" + gender +
                ", xrrEntriesUrl=" + xrrEntriesUrl + "]";
    }
}
