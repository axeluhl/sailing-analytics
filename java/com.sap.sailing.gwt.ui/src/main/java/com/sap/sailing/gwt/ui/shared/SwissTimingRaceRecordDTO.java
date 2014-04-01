package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingRaceRecordDTO implements IsSerializable {
    public String raceId;
    public String raceName;
    public Date raceStartTime;
    public String regattaName;
    public String seriesName;
    public String fleetName;
    public String raceStatus;
    
    public String boatClass;
    public String gender;

    public boolean hasCourse;
    public boolean hasStartlist;
    
    public SwissTimingRaceRecordDTO() {}
    
    public SwissTimingRaceRecordDTO(String raceId, String raceName, String regattaName, String seriesName, String fleetName,
    		String raceStatus, Date raceStartTime) {
        super();
        this.raceId = raceId;
        this.raceName = raceName;
        this.regattaName = regattaName;
        this.seriesName = seriesName;
        this.fleetName = fleetName;
        this.raceStatus = raceStatus;
        this.raceStartTime = raceStartTime;
    }

	@Override
	public String toString() {
		return "SwissTimingRaceRecordDTO [raceId=" + raceId + ", raceName="
				+ raceName + ", raceStartTime=" + raceStartTime
				+ ", regattaName=" + regattaName + ", seriesName=" + seriesName
				+ ", fleetName=" + fleetName + ", raceStatus=" + raceStatus
				+ ", boatClass=" + boatClass + ", gender=" + gender
				+ ", hasCourse=" + hasCourse + ", hasStartlist=" + hasStartlist
				+ "]";
	}
}
