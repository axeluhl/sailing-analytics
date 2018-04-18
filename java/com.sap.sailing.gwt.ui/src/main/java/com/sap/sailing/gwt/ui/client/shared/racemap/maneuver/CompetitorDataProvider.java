package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class CompetitorDataProvider<C extends CompetitorDTO, D> {

    private final Supplier<TimePoint> startOfTrackingProvider, endOfTrackingProvider, liveTimePointProvider;
    private final Supplier<Boolean> isReplayingProvider;
    private final Function<D, TimePoint> measurementTimePointProvider;
    private final Map<C, CompetitorData> cache = new HashMap<>();

    public CompetitorDataProvider(final Supplier<Date> startOfTrackingProvider,
            final Supplier<Date> endOfTrackingProvider, final Supplier<TimePoint> liveTimePointProvider,
            final Supplier<Boolean> isReplayingProvider, final Function<D, Date> measurementTimePointProvider) {
        this.startOfTrackingProvider = () -> new MillisecondsTimePoint(startOfTrackingProvider.get());
        this.endOfTrackingProvider = () -> new MillisecondsTimePoint(endOfTrackingProvider.get());
        this.liveTimePointProvider = liveTimePointProvider;
        this.isReplayingProvider = isReplayingProvider;
        this.measurementTimePointProvider = m -> new MillisecondsTimePoint(measurementTimePointProvider.apply(m));
    }

    public void reset() {
        this.cache.clear();
    }

    public void reset(final C competitor) {
        this.cache.remove(competitor);
    }

    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    protected void loadData(final Map<C, TimeRange> competitorTimeRanges, final Consumer<Map<C, List<D>>> callback) {
    }

    public void update(final C competitor) {
        this.update(Collections.singletonList(competitor), false);
    }

    public void update(final Iterable<C> competitors) {
        this.update(competitors, true);
    }

    public void update(final Iterable<C> competitors, final boolean incremental) {
        final Map<C, TimeRange> competitorUpdateTimeRanges = new HashMap<>();
        competitors.forEach(c -> getData(c).getUpdateTimeRange().ifPresent(t -> competitorUpdateTimeRanges.put(c, t)));
        if (!competitorUpdateTimeRanges.isEmpty()) {
            this.loadData(competitorUpdateTimeRanges, result -> {
                final Set<C> competitorToRefresh = new HashSet<>();
                for (Entry<C, List<D>> entry : result.entrySet()) {
                    final C competitor = entry.getKey();
                    final List<D> records = entry.getValue();
                    final CompetitorData data = CompetitorDataProvider.this.getData(competitor);
                    if (incremental && !data.getRecords().isEmpty() && !records.isEmpty()) {
                        competitorToRefresh.add(competitor);
                    }
                    data.update(records, incremental);
                }
                CompetitorDataProvider.this.update(competitorToRefresh, false);
            });
        }
    }

    private CompetitorData getData(final C competitor) {
        return this.cache.computeIfAbsent(competitor, c -> new CompetitorData());
    }

    public final Map<C, List<D>> getData() {
        final Map<C, List<D>> result = new HashMap<>(cache.size());
        for (Entry<C, CompetitorData> entry : cache.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getRecords());
        }
        return result;
    }

    private class CompetitorData {

        private final LinkedList<D> records = new LinkedList<>();
        private boolean noDataRequested = true;

        private Optional<TimeRange> getUpdateTimeRange() {
            final Optional<TimeRange> result;
            if (noDataRequested) {
                result = Optional.of(new TimeRangeImpl(startOfTrackingProvider.get(), getEndTimePoint(), true));
            } else {
                if (isReplayingProvider.get()) {
                    result = Optional.empty();
                } else {
                    final TimePoint lastestMeasurementTimePoint = measurementTimePointProvider.apply(records.getLast());
                    result = Optional.of(new TimeRangeImpl(lastestMeasurementTimePoint, getEndTimePoint(), true));
                }
            }
            this.noDataRequested = false;
            return result;
        }

        private TimePoint getEndTimePoint() {
            final TimePoint end = endOfTrackingProvider.get(), live = liveTimePointProvider.get();
            return end == null ? live : new MillisecondsTimePoint(Math.min(end.asMillis(), live.asMillis()));
        }

        private void update(final List<D> records, final boolean append) {
            if (!append) {
                this.records.clear();
            }
            this.records.addAll(records);
        }

        private List<D> getRecords() {
            return records;
        }
    }

}
