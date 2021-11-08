package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.async.TimeRangeAsyncAction;

public class GetBoatPositionsAction implements TimeRangeAsyncAction<CompactBoatPositionsDTO, String> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    private final DetailType detailType;
    private final String leaderboardName;
    private final String leaderboardGroupName;
    private final UUID leaderboardGroupId;

    public GetBoatPositionsAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
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

    @Override
    public void execute(Map<String, TimeRange> timeRanges, AsyncCallback<CompactBoatPositionsDTO> callback) {
        final Map<String, Date> fromByCompetitorIdAsString = new HashMap<String, Date>();
        final Map<String, Date> toByCompetitorIdAsString = new HashMap<String, Date>();
        for (final Map.Entry<String, TimeRange> entry : timeRanges.entrySet()) {
            final Date from = entry.getValue().from().asDate();
            final Date to = entry.getValue().to().asDate();
            fromByCompetitorIdAsString.put(entry.getKey(), from);
            toByCompetitorIdAsString.put(entry.getKey(), to);
        }
        sailingService.getBoatPositions(raceIdentifier, fromByCompetitorIdAsString, toByCompetitorIdAsString,
                extrapolate, detailType, leaderboardName, leaderboardGroupName, leaderboardGroupId, callback);
    }

    @Override
    public Map<String, TimeRange> getTimeRanges() {
        final Map<String, TimeRange> timeRangeByCompetitorId = new HashMap<>(from.size());
        for (final Map.Entry<CompetitorDTO, Date> entry : from.entrySet()) {
            final Date fromDate = entry.getValue();
            final Date toDate = to.get(entry.getKey());
            if (fromDate != null && toDate != null) {
                timeRangeByCompetitorId.put(entry.getKey().getIdAsString(),
                        new TimeRangeImpl(TimePoint.of(fromDate), TimePoint.of(toDate), /* toIsInclusive */ true));
            }
        }
        return timeRangeByCompetitorId;
    }
}
