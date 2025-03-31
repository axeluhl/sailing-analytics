package com.sap.sailing.gwt.ui.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
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
 * The third type parameter ({@link Pair<String, DetailType>}) for {@link TimeRangeAsyncCallback} represents the stringified
 * competitor IDs and the DetailType which form the key for the result structure.
 */
public class GetBoatPositionsCallback
        implements TimeRangeAsyncCallback<CompactBoatPositionsDTO, GPSFixDTOWithSpeedWindTackAndLegTypeIterable, Pair<String, DetailType>> {
    private final DetailType detailType;
    private final AsyncCallback<CompactBoatPositionsDTO> callback;

    public GetBoatPositionsCallback(DetailType detailType, AsyncCallback<CompactBoatPositionsDTO> callback) {
        this.detailType = detailType;
        this.callback = callback;
    }

    @Override
    public Map<Pair<String, DetailType>, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> unzipResult(CompactBoatPositionsDTO result) {
        final Map<Pair<String, DetailType>, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> unzippedResult = new HashMap<>();
        for (final Entry<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> i : result.getBoatPositions().entrySet()) {
            unzippedResult.put(new Pair<>(i.getKey(), detailType), i.getValue());
        }
        return unzippedResult;
    }

    @Override
    public CompactBoatPositionsDTO zipSubResults(Map<Pair<String, DetailType>, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> subResultMap) {
        final Map<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> convertedSubResultMap = new HashMap<>();
        for (final Entry<Pair<String, DetailType>, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> i : subResultMap.entrySet()) {
            convertedSubResultMap.put(i.getKey().getA(), i.getValue());
        }
        return CompactBoatPositionsDTO.fromCompetitorIds(convertedSubResultMap);
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
