package com.sap.sailing.gwt.home.communication.eventview;

import java.util.Date;

import com.sap.sse.gwt.dispatch.shared.commands.DTO;

public class RegattaMetadataDTO extends RegattaReferenceDTO implements HasRegattaMetadata {
    private int raceCount;
    private int competitorsCount;
    private String boatClass;
    private String boatCategory;
    private String defaultCourseAreaName;
    private String defaultCourseAreaId;
    private Date startDate;
    private Date endDate;
    private RegattaState state;
    private boolean flexibleLeaderboard;
    private RaceDataInfo raceDataInfo;
    
    public RegattaMetadataDTO() {
    }

    @Override
    public int getRaceCount() {
        return raceCount;
    }

    public void setRaceCount(int raceCount) {
        this.raceCount = raceCount;
    }

    @Override
    public int getCompetitorsCount() {
        return competitorsCount;
    }

    public void setCompetitorsCount(int competitorsCount) {
        this.competitorsCount = competitorsCount;
    }

    @Override
    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }

    @Override
    public String getBoatCategory() {
        return boatCategory;
    }

    public void setBoatCategory(String boatCategory) {
        this.boatCategory = boatCategory;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public RegattaState getState() {
        return state;
    }

    public void setState(RegattaState state) {
        this.state = state;
    }

    public String getDefaultCourseAreaName() {
        return defaultCourseAreaName;
    }

    public void setDefaultCourseAreaName(String defaultCourseAreaName) {
        this.defaultCourseAreaName = defaultCourseAreaName;
    }

    public boolean isFlexibleLeaderboard() {
        return flexibleLeaderboard;
    }

    public void setFlexibleLeaderboard(boolean flexibleLeaderboard) {
        this.flexibleLeaderboard = flexibleLeaderboard;
    }

    public String getDefaultCourseAreaId() {
        return defaultCourseAreaId;
    }

    public void setDefaultCourseAreaId(String defaultCourseAreaId) {
        this.defaultCourseAreaId = defaultCourseAreaId;
    }
    
    public RaceDataInfo getRaceDataInfo() {
        return raceDataInfo;
    }
    
    public void setRaceDataInfo(RaceDataInfo raceDataInfo) {
        this.raceDataInfo = raceDataInfo;
    }
    
    @Override
    public int compareTo(RegattaReferenceDTO o) {
        int result = 0;
        if (o instanceof RegattaMetadataDTO) {
            result = boatCategory.compareTo(((RegattaMetadataDTO) o).boatCategory);
        }
        if (result == 0) {
            result = super.compareTo(o);
        }
        return result;
    }

    public static class RaceDataInfo implements DTO {
        private boolean hasGPSData, hasWindData, hasVideoData, hasAudioData;
        
        protected RaceDataInfo() {
        }
        
        public RaceDataInfo(boolean hasGPSData, boolean hasWindData, boolean hasVideoData, boolean hasAudioData) {
            this.hasGPSData = hasGPSData;
            this.hasWindData = hasWindData;
            this.hasVideoData = hasVideoData;
            this.hasAudioData = hasAudioData;
        }

        public boolean hasGPSData() {
            return hasGPSData;
        }

        public boolean hasWindData() {
            return hasWindData;
        }

        public boolean hasVideoData() {
            return hasVideoData;
        }

        public boolean hasAudioData() {
            return hasAudioData;
        }
    }
}
