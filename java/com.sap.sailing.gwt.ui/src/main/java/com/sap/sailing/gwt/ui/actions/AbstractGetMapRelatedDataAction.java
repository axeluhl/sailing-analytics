package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

/**
 * A remote data request that is specific to a race in the scope of a leaderboard and a leaderboard group, and for a set
 * of competitors each specifies a time range in which to obtain position data, optionally annotated with detail values
 * of a specific {@link DetailType}.
 * <p>
 * 
 * Requests of this type are asynchronous, meaning in particular that the responses can arrive in an order different
 * from the request order. Even new requests may be assembled and sent before the responses to all outstanding requests
 * have been received. Sometimes a new request may supersede a request with an outstanding response, in which case
 * the callback to the request with the outstanding response may have to be informed that it must not apply the
 * result for one or more competitors. For example, if a request for position data is made with a specific {@link DetailType},
 * and then, before the response for that request has been received, another request, this time for a different
 * {@link DetailType}, is constructed and sent because the user has switched to a different {@link DetailType} in the UI,
 * then the callback of the request with the outstanding response must be told to toss its position data because it
 * would be annotated with values for the wrong {@link DetailType} now.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public abstract class AbstractGetMapRelatedDataAction<T> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<String, Date> from;
    private final Map<String, Date> to;
    private final boolean extrapolate;
    private final DetailType detailType;
    private final String leaderboardName;
    private final String leaderboardGroupName;
    private final UUID leaderboardGroupId;

    public AbstractGetMapRelatedDataAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> from, Map<String, Date> to, boolean extrapolate, DetailType detailType,
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

    protected Map<String, Date> getFromByCompetitorIdAsString() {
        return from;
    }

    protected Map<String, Date> getToByCompetitorIdAsString() {
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
