package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sse.common.Duration;

public class RaceListRaceDTO extends RaceMetadataDTO {
    
    private Duration duration;
    private CompetitorDTO winner;
    private int windFixesCount;
    private int videoCount;
    private int audioCount;

    @SuppressWarnings("unused")
    private RaceListRaceDTO() {
    }
    
    public RaceListRaceDTO(String regattaName, String raceName) {
        super(regattaName, raceName);
    }

    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    public CompetitorDTO getWinner() {
        return winner;
    }
    public void setWinner(CompetitorDTO winner) {
        this.winner = winner;
    }
    public int getWindFixesCount() {
        return windFixesCount;
    }
    public void setWindFixesCount(int windFixesCount) {
        this.windFixesCount = windFixesCount;
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
