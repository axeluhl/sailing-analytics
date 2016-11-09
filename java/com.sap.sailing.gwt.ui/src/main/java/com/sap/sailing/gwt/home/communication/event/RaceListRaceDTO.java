package com.sap.sailing.gwt.home.communication.event;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.wind.WindStatisticsDTO;
import com.sap.sse.common.Duration;

public class RaceListRaceDTO extends RaceMetadataDTO<WindStatisticsDTO> {
    
    private Duration duration;
    private SimpleCompetitorDTO winner;
    private int windSourcesCount;
    private int videoCount;
    private int audioCount;

    @SuppressWarnings("unused")
    private RaceListRaceDTO() {
    }

    public RaceListRaceDTO(String leaderboardName, RegattaAndRaceIdentifier regattaAndRaceIdentifier, String raceName) {
        super(leaderboardName, regattaAndRaceIdentifier, raceName);
    }

    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    public SimpleCompetitorDTO getWinner() {
        return winner;
    }
    public void setWinner(SimpleCompetitorDTO winner) {
        this.winner = winner;
    }
    public int getWindSourcesCount() {
        return windSourcesCount;
    }
    public void setWindSourcesCount(int windFixesCount) {
        this.windSourcesCount = windFixesCount;
    }
    public int getVideoCount() {
        return videoCount;
    }
    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }
    public int getAudioCount() {
        return audioCount;
    }
    public void setAudioCount(int audioCount) {
        this.audioCount = audioCount;
    }
}
