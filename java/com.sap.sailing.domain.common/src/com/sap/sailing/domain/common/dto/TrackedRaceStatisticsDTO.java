package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.Duration;

public class TrackedRaceStatisticsDTO implements Serializable {
    private static final long serialVersionUID = -2123085927945474642L;

    /** Indicators if statistical data is available for a date or not */
    public boolean hasGPSData;
    public boolean hasAudioData;
    public boolean hasVideoData;
    public boolean hasMeasuredWindData;
    public boolean hasLegProgressData;
    public boolean hasLeaderOrWinnerData;

    public Duration averageGPSDataSampleInterval;
    public Integer measuredWindSourcesCount;
    public Duration averageWindDataSampleInterval;
    public Integer audioTracksCount;
    public Integer videoTracksCount;
    
    public Integer totalLegsCount;
    public Integer currentLegNo;
    public CompetitorAndBoatDTO leaderOrWinner;
    /**
     * Default constructor for GWT-Serialization
     */
    public TrackedRaceStatisticsDTO() {
        hasGPSData = false;
        hasAudioData= false;
        hasVideoData= false;
        hasMeasuredWindData = false;
        hasLegProgressData = false;
        hasLeaderOrWinnerData = false;
    }
}
