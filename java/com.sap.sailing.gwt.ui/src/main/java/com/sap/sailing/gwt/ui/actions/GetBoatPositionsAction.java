package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.async.TimeRangeAsyncAction;

public class GetBoatPositionsAction extends AbstractGetMapRelatedDataAction<CompactBoatPositionsDTO> implements TimeRangeAsyncAction<CompactBoatPositionsDTO, Pair<String, DetailType>> {
    private final String leaderboardName;
    private final String leaderboardGroupName;
    private final UUID leaderboardGroupId;

    public GetBoatPositionsAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> from, Map<String, Date> to, boolean extrapolate, DetailType detailType,
            String leaderboardName, String leaderboardGroupName, UUID leaderboardGroupId) {
        super(sailingService, raceIdentifier, from, to, extrapolate, detailType, leaderboardName, leaderboardGroupName, leaderboardGroupId);
        this.leaderboardName = leaderboardName;
        this.leaderboardGroupName = leaderboardGroupName;
        this.leaderboardGroupId = leaderboardGroupId;
    }

    @Override
    public void execute(Map<Pair<String, DetailType>, TimeRange> timeRanges, AsyncCallback<CompactBoatPositionsDTO> callback) {
        final Map<String, Date> fromByCompetitorIdAsString = new HashMap<String, Date>();
        final Map<String, Date> toByCompetitorIdAsString = new HashMap<String, Date>();
        for (final Map.Entry<Pair<String, DetailType>, TimeRange> entry : timeRanges.entrySet()) {
            final Date from = entry.getValue().from().asDate();
            final Date to = entry.getValue().to().asDate();
            fromByCompetitorIdAsString.put(entry.getKey().getA(), from);
            toByCompetitorIdAsString.put(entry.getKey().getA(), to);
        }
        getSailingService().getBoatPositions(getRaceIdentifier(), fromByCompetitorIdAsString, toByCompetitorIdAsString,
                isExtrapolate(), getDetailType(), leaderboardName, leaderboardGroupName, leaderboardGroupId, callback);
    }

    @Override
    public Map<Pair<String, DetailType>, TimeRange> getTimeRanges() {
        final Map<Pair<String, DetailType>, TimeRange> timeRangeByCompetitorId = new HashMap<>(getFromByCompetitorIdAsString().size());
        for (final Map.Entry<String, Date> entry : getFromByCompetitorIdAsString().entrySet()) {
            final Date fromDate = entry.getValue();
            final Date toDate = getToByCompetitorIdAsString().get(entry.getKey());
            if (fromDate != null && toDate != null) {
                timeRangeByCompetitorId.put(new Pair<>(entry.getKey(), getDetailType()),
                        new TimeRangeImpl(TimePoint.of(fromDate), TimePoint.of(toDate), /* toIsInclusive */ true));
            }
        }
        return timeRangeByCompetitorId;
    }
}
