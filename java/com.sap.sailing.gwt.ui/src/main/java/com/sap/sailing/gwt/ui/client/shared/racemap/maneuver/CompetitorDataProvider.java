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
        this.update(Collections.singletonList(competitor));
    }

    public void update(final Iterable<C> competitors) {
        final Map<C, Update> competitorUpdates = new HashMap<>();
        competitors.forEach(c -> getData(c).requiresUpdate().ifPresent(u -> competitorUpdates.put(c, u)));
        if (!competitorUpdates.isEmpty()) {
            this.loadData(map(competitorUpdates, Update::getTimeRange), result -> {
                final Set<C> competitorToRefresh = new HashSet<>();
                for (Entry<C, List<D>> entry : result.entrySet()) {
                    final C competitor = entry.getKey();
                    final boolean wasIncrementalUpdate = competitorUpdates.get(competitor).isIncremental();
                    final List<D> records = entry.getValue();
                    final CompetitorData data = CompetitorDataProvider.this.getData(competitor);
                    if (wasIncrementalUpdate && !data.getRecords().isEmpty() && !records.isEmpty()) {
                        competitorToRefresh.add(competitor);
                    }
                    data.update(records, wasIncrementalUpdate);
                }
                CompetitorDataProvider.this.update(competitorToRefresh);
            });
        }
    }

    private CompetitorData getData(final C competitor) {
        return this.cache.computeIfAbsent(competitor, c -> new CompetitorData());
    }

    public final Map<C, List<D>> getData() {
        return map(cache, CompetitorData::getRecords);
    }

    private <S, T> Map<C, T> map(final Map<C, S> source, final Function<S, T> mapper) {
        final Map<C, T> result = new HashMap<>(source.size());
        for (Entry<C, S> entry : source.entrySet()) {
            result.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return result;
    }

    private class CompetitorData {

        private final LinkedList<D> records = new LinkedList<>();
        private boolean noDataRequested = true;

        private Optional<Update> requiresUpdate() {
            final Optional<Update> result;
            if (isReplayingProvider.get()) {
                if (noDataRequested) {
                    result = Update.novating(getStartTimePoint(), getEndTimePoint());
                } else {
                    result = Update.none();
                }
            } else {
                if (noDataRequested) {
                    result = Update.novating(getStartTimePoint(), getEndTimePoint());
                } else {
                    result = Update.incremental(getLatestMeasurementTimePoint(), getEndTimePoint());
                }
            }
            this.noDataRequested = false;
            return result;
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

        private TimePoint getStartTimePoint() {
            return startOfTrackingProvider.get();
        }

        private TimePoint getEndTimePoint() {
            final TimePoint end = endOfTrackingProvider.get(), live = liveTimePointProvider.get();
            return end == null ? live : new MillisecondsTimePoint(Math.max(end.asMillis(), live.asMillis()));
        }

        private TimePoint getLatestMeasurementTimePoint() {
            return measurementTimePointProvider.apply(records.getLast());
        }
    }

    private static class Update {
        private final TimeRange timeRange;
        private final boolean incremental;

        private static Optional<Update> none() {
            return Optional.empty();
        }

        private static Optional<Update> incremental(final TimePoint from, final TimePoint to) {
            return Optional.of(new Update(from, to, true));
        }

        private static Optional<Update> novating(final TimePoint from, final TimePoint to) {
            return Optional.of(new Update(from, to, false));
        }

        private Update(final TimePoint from, final TimePoint to, boolean incremental) {
            this.timeRange = new TimeRangeImpl(from, to, true);
            this.incremental = incremental;
        }

        private TimeRange getTimeRange() {
            return timeRange;
        }

        private boolean isIncremental() {
            return incremental;
        }

    }

}
