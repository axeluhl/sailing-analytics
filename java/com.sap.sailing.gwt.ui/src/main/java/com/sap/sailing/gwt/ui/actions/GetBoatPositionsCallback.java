package com.sap.sailing.gwt.ui.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegTypeIterable;
import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MultiTimeRangeImpl;
import com.sap.sse.gwt.client.async.TimeRangeAsyncCallback;

/**
 * The third type parameter ({@link String}) for {@link TimeRangeAsyncCallback} represents the stringified
 * competitor IDs which form the key for the result structure.
 */
public class GetBoatPositionsCallback
        implements TimeRangeAsyncCallback<CompactBoatPositionsDTO, GPSFixDTOWithSpeedWindTackAndLegTypeIterable, String> {
    private final AsyncCallback<CompactBoatPositionsDTO> callback;

    public GetBoatPositionsCallback(AsyncCallback<CompactBoatPositionsDTO> callback) {
        this.callback = callback;
    }

    @Override
    public Map<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> unzipResult(CompactBoatPositionsDTO result) {
        return result.getBoatPositions();
    }

    @Override
    public CompactBoatPositionsDTO zipSubResults(Map<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> subResultMap) {
        return CompactBoatPositionsDTO.fromCompetitorIds(subResultMap);
    }

    @Override
    public GPSFixDTOWithSpeedWindTackAndLegTypeIterable joinSubResults(TimeRange timeRange,
            List<Pair<TimeRange, GPSFixDTOWithSpeedWindTackAndLegTypeIterable>> toJoin) {
        final List<Pair<TimeRange, GPSFixDTOWithSpeedWindTackAndLegTypeIterable>> toJoinSorted = new ArrayList<>(toJoin);
        toJoinSorted.sort(Comparator.comparing(Pair::getA));
        final List<GPSFixDTOWithSpeedWindTackAndLegType> resultList = new ArrayList<>();
        MultiTimeRange collectedMultiTimeRange = new MultiTimeRangeImpl(new TimeRange[0]);
        for (final Pair<TimeRange, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> e : toJoinSorted) {
            final TimeRange potentiallyWantedTimeRange = e.getA().intersection(timeRange);
            if (potentiallyWantedTimeRange != null && e.getB() != null) {
                final MultiTimeRange toCollectMultiTimeRange = new MultiTimeRangeImpl(potentiallyWantedTimeRange)
                        .subtract(collectedMultiTimeRange);
                if (!toCollectMultiTimeRange.isEmpty()) {
                    for (final GPSFixDTOWithSpeedWindTackAndLegType gpsFix : e.getB()) {
                        if (toCollectMultiTimeRange.includes(TimePoint.of(gpsFix.timepoint))) {
                            resultList.add(gpsFix);
                        }
                    }
                    collectedMultiTimeRange = collectedMultiTimeRange.union(toCollectMultiTimeRange);
                }
            }
        }
        return new GPSFixDTOWithSpeedWindTackAndLegTypeIterable(resultList);
    }

    @Override
    public void onSuccess(CompactBoatPositionsDTO result) {
        callback.onSuccess(result);
    }

    @Override
    public void onFailure(Throwable caught) {
        callback.onFailure(caught);
    }
}
