package com.sap.sailing.gwt.ui.shared.race;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class RaceMetadataDTO implements IsSerializable {
    
    private RegattaAndRaceIdentifier id;

    private String regattaName;
    private String raceName;
    private FleetMetadataDTO fleet;
    private Date start;
    private String courseArea;

    protected RaceMetadataDTO() {
    }
    
    public RaceMetadataDTO(RegattaAndRaceIdentifier id) {
        this.id = id;
    }
    
    public RegattaAndRaceIdentifier getID() {
        return id;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public FleetMetadataDTO getFleet() {
        return fleet;
    }

    public void setFleet(FleetMetadataDTO fleet) {
        this.fleet = fleet;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public String getCourseArea() {
        return courseArea;
    }

    public void setCourseArea(String courseArea) {
        this.courseArea = courseArea;
    }
}
