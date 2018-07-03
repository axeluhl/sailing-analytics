package com.sap.sailing.gwt.home.communication.eventview;

import com.sap.sse.gwt.dispatch.shared.commands.DTO;

public class SeriesReferenceDTO implements DTO {

    private String seriesDisplayName;
    private String seriesLeaderboardGroupName;

    protected SeriesReferenceDTO() {
    }

    public SeriesReferenceDTO(String seriesDisplayName, String seriesLeaderboardGroupName) {
        super();
        this.seriesDisplayName = seriesDisplayName;
        this.seriesLeaderboardGroupName = seriesLeaderboardGroupName;
    }

    public String getSeriesDisplayName() {
        return seriesDisplayName;
    }

    public String getSeriesLeaderboardGroupName() {
        return seriesLeaderboardGroupName;
    }
}
