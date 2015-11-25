package com.sap.sailing.gwt.home.communication.race;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.race.wind.AbstractWindDTO;
import com.sap.sse.common.Util;

public abstract class RaceMetadataDTO<WIND extends AbstractWindDTO> extends SimpleRaceMetadataDTO {
    
    private String regattaName;
    private String regattaDisplayName;
    private FleetMetadataDTO fleet;
    private String courseArea;
    private String course;
    private String boatClass;
    private WIND wind;

    protected RaceMetadataDTO() {
    }
    
    public RaceMetadataDTO(String leaderboardName, RegattaAndRaceIdentifier regattaAndRaceIdentifier, String raceName) {
        super(leaderboardName, regattaAndRaceIdentifier, raceName);
        this.regattaName = leaderboardName;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public FleetMetadataDTO getFleet() {
        return fleet;
    }

    public void setFleet(FleetMetadataDTO fleet) {
        this.fleet = fleet;
    }

    public String getCourseArea() {
        return courseArea;
    }

    public void setCourseArea(String courseArea) {
        this.courseArea = courseArea;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }

    public WIND getWind() {
        return wind;
    }

    public void setWind(WIND wind) {
        this.wind = wind;
    }

    public String getRegattaDisplayName() {
        return regattaDisplayName;
    }

    public void setRegattaDisplayName(String regattaDisplayName) {
        this.regattaDisplayName = regattaDisplayName;
    }
    
    @Override
    public int compareTo(SimpleRaceMetadataDTO o) {
        if (o instanceof RaceMetadataDTO<?>) {
            RaceMetadataDTO<?> other = (RaceMetadataDTO<?>) o;
            final int compareByStart = Util.compareToWithNull(getStart(), other.getStart(), false);
            return compareByStart != 0 ? compareByStart : compareBySecondaryCriteria(other);
        }
        return super.compareTo(o);
    }

    private int compareBySecondaryCriteria(RaceMetadataDTO<?> other) {
        int compareByRegattaName = Util.compareToWithNull(getRegattaName(), other.getRegattaName(), true);
        if(compareByRegattaName != 0) {
            return compareByRegattaName;
        }
        int compareByRaceName = getRaceName().compareTo(other.getRaceName());
        if(compareByRaceName != 0) {
            return compareByRaceName;
        }
        int compareByFleet = Util.compareToWithNull(getFleet(), other.getFleet(), true);
        if(compareByFleet != 0) {
            return compareByFleet;
        }
        return getViewState().compareTo(other.getViewState());
    }
}
