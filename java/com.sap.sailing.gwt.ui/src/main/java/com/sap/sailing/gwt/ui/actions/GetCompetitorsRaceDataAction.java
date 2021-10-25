package com.sap.sailing.gwt.ui.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.gwt.client.async.TimeRangeAsyncAction;

public class GetCompetitorsRaceDataAction implements TimeRangeAsyncAction<CompetitorsRaceDataDTO, CompetitorDTO> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final List<CompetitorDTO> competitors;
    private final Date fromDate;
    private final Date toDate;
    private final long stepSizeInMs;
    private final DetailType detailType;
    private final String leaderboarGroupName;
    private final UUID leaderboarGroupId;
    private final String leaderboardName;
    
    public GetCompetitorsRaceDataAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
            List<CompetitorDTO> competitors, Date fromDate, Date toDate, long stepSizeInMs, DetailType detailType,
            String leaderboardGroupName, UUID leaderboardGroupId, String leaderboardName) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.competitors = competitors;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.stepSizeInMs = stepSizeInMs;
        this.detailType = detailType;
        this.leaderboarGroupName = leaderboardGroupName;
        this.leaderboarGroupId = leaderboardGroupId;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public void execute(Map<CompetitorDTO, TimeRange> timeRanges, AsyncCallback<CompetitorsRaceDataDTO> callback) {
        TimeRange timeRange = null;
        for (final Entry<CompetitorDTO, TimeRange> e : timeRanges.entrySet()) {
            timeRange = timeRange == null ? e.getValue() : timeRange.extend(e.getValue());
        }
        sailingService.getCompetitorsRaceData(raceIdentifier, new ArrayList<>(timeRanges.keySet()), timeRange.from().asDate(),
                timeRange.to().asDate(), stepSizeInMs, detailType, leaderboarGroupName, leaderboarGroupId,
                leaderboardName, callback);
    }

    @Override
    public Map<CompetitorDTO, TimeRange> getTimeRanges() {
        // we'll use the same time range for all competitors
        final TimeRange timeRange = TimeRange.create(TimePoint.of(fromDate), TimePoint.of(toDate));
        final Map<CompetitorDTO, TimeRange> result = new HashMap<>();
        for (final CompetitorDTO competitor : competitors) {
            result.put(competitor, timeRange);
        }
        return result;
    }
}
