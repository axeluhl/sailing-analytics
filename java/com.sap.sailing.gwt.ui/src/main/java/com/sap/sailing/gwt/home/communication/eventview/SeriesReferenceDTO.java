package com.sap.sailing.gwt.home.communication.eventview;

import java.util.UUID;

import com.sap.sse.gwt.dispatch.shared.commands.DTO;

public class SeriesReferenceDTO implements DTO {

    private String seriesDisplayName;
    private UUID seriesLeaderboardGroupId;

    protected SeriesReferenceDTO() {
    }

    public SeriesReferenceDTO(String seriesDisplayName, UUID seriesLeaderboardGroupId) {
        super();
        this.seriesDisplayName = seriesDisplayName;
        this.seriesLeaderboardGroupId = seriesLeaderboardGroupId;
    }

    public String getSeriesDisplayName() {
        return seriesDisplayName;
    }

    public UUID getSeriesLeaderboardGroupId() {
        return seriesLeaderboardGroupId;
    }
}
