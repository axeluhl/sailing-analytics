package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sse.common.Duration;

public class RaceListRaceDTO extends RaceMetadataDTO {
    
    private Duration duration;
    private CompetitorDTO winner;
    // TODO: fields for wind(!?), video and audio counters

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
    
}
