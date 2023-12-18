package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public abstract class AbstractGetMapRelatedDataAction<T> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    private final DetailType detailType;
    private final String leaderboardName;
    private final String leaderboardGroupName;
    private final UUID leaderboardGroupId;

    public AbstractGetMapRelatedDataAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, boolean extrapolate, DetailType detailType,
            String leaderboardName, String leaderboardGroupName, UUID leaderboardGroupId) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
        this.detailType = detailType;
        this.leaderboardName = leaderboardName;
        this.leaderboardGroupName = leaderboardGroupName;
        this.leaderboardGroupId = leaderboardGroupId;
    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    protected Map<CompetitorDTO, Date> getFrom() {
        return from;
    }

    protected Map<CompetitorDTO, Date> getTo() {
        return to;
    }

    protected boolean isExtrapolate() {
        return extrapolate;
    }
    
    protected DetailType getDetailType() {
        return detailType;
    }

    protected String getLeaderboardName() {
        return leaderboardName;
    }

    protected String getLeaderboardGroupName() {
        return leaderboardGroupName;
    }

    protected UUID getLeaderboardGroupId() {
        return leaderboardGroupId;
    }
}
