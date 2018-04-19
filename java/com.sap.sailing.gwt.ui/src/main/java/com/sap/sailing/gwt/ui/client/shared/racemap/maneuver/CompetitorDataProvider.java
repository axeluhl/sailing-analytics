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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public abstract class CompetitorDataProvider<C extends CompetitorDTO, D> {

    private static final Logger LOGGER = Logger.getLogger(CompetitorDataProvider.class.getName());

    private final Supplier<TimePoint> startOfTrackingProvider, endOfTrackingProvider, liveTimePointProvider;
    private final Supplier<Boolean> isReplayingProvider;
    private final Function<D, TimePoint> measurementTimePointProvider;
    private final Map<C, CompetitorDataCache> cache = new HashMap<>();

    public CompetitorDataProvider(final Supplier<Date> startOfTrackingProvider,
            final Supplier<Date> endOfTrackingProvider, final Supplier<TimePoint> liveTimePointProvider,
            final Supplier<Boolean> isReplayingProvider, final Function<D, Long> measurementTimePointMillisProvider) {
        this.startOfTrackingProvider = () -> new MillisecondsTimePoint(startOfTrackingProvider.get());
        this.endOfTrackingProvider = () -> new MillisecondsTimePoint(endOfTrackingProvider.get());
        this.liveTimePointProvider = liveTimePointProvider;
        this.isReplayingProvider = isReplayingProvider;
        this.measurementTimePointProvider = m -> new MillisecondsTimePoint(measurementTimePointMillisProvider.apply(m));
    }

    protected abstract void loadData(final boolean incremental, final Map<C, TimeRange> competitorTimeRanges,
            final AsyncCallback<Map<C, List<D>>> callback);

    protected abstract void onCompetitorsDataChange(final Iterable<C> updatedCompetitors);

    protected boolean triggerFullUpdateAfterIncrementalUpdate(final List<D> existingData, final List<D> updatedData) {
        return false;
    }

    public void removeAllCompetitors() {
        this.cache.clear();
    }

    public void removeCompetitor(final C competitor) {
        this.cache.remove(competitor);
    }

    public boolean hasCachedData() {
        return !this.cache.isEmpty();
    }

    public void updateCompetitorData() {
        this.update(cache.keySet(), true);
    }

    public void ensureCompetitor(final C competitor) {
        this.ensureCompetitors(Collections.singleton(competitor));
    }

    public void ensureCompetitors(final Iterable<C> competitors) {
        final Set<C> competitorsToUpdate = new HashSet<>();
        for (final C comp : competitors) {
            if (this.cache.putIfAbsent(comp, new CompetitorDataCache()) == null) {
                competitorsToUpdate.add(comp);
            }
        }
        this.update(competitorsToUpdate, false);
    }

    private void update(final Iterable<C> competitors, final boolean incremental) {
        final Map<C, TimeRange> compTimeRanges = new HashMap<>();
        competitors.forEach(c -> getCompetitorDataCache(c)
                .ifPresent(d -> d.getUpdateTimeRange(incremental).ifPresent(t -> compTimeRanges.put(c, t))));
        if (!compTimeRanges.isEmpty()) {
            this.loadData(incremental, compTimeRanges, new AsyncCallback<Map<C, List<D>>>() {

                @Override
                public void onSuccess(Map<C, List<D>> result) {
                    final Set<C> competitorsToTriggerFullUpdateFor = new HashSet<>();
                    final Set<C> updatedCompetitors = new HashSet<>();
                    for (Entry<C, List<D>> entry : result.entrySet()) {
                        final C competitor = entry.getKey();
                        final List<D> loadedRecords = entry.getValue();
                        final CompetitorDataCache data = cache.get(competitor);
                        if (data != null) {
                            if (incremental
                                    && triggerFullUpdateAfterIncrementalUpdate(data.getCachedRecords(), loadedRecords)) {
                                competitorsToTriggerFullUpdateFor.add(competitor);
                            }
                            if (!loadedRecords.isEmpty()) {
                                updatedCompetitors.add(competitor);
                            }
                            data.updateCachedRecords(loadedRecords, incremental);
                        }
                    }
                    if (!updatedCompetitors.isEmpty()) {
                        CompetitorDataProvider.this.onCompetitorsDataChange(updatedCompetitors);
                    }
                    CompetitorDataProvider.this.update(competitorsToTriggerFullUpdateFor, false);
                }

                @Override
                public void onFailure(Throwable caught) {
                    LOGGER.log(Level.SEVERE, "Failed to load competitor data!", caught);
                }
            });
        }
    }

    private Optional<CompetitorDataCache> getCompetitorDataCache(final C competitor) {
        return Optional.ofNullable(cache.get(competitor));
    }

    public final Map<C, List<D>> getData() {
        final Map<C, List<D>> result = new HashMap<>(cache.size());
        for (Entry<C, CompetitorDataCache> entry : cache.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getCachedRecords());
        }
        return result;
    }

    private class CompetitorDataCache {

        private final LinkedList<D> records = new LinkedList<>();
        private boolean noDataRequested = true;

        private Optional<TimeRange> getUpdateTimeRange(final boolean incremental) {
            final Optional<TimeRange> result;
            if (noDataRequested || records.isEmpty() || !incremental) {
                result = Optional.of(new TimeRangeImpl(startOfTrackingProvider.get(), getEndTimePoint(), true));
            } else {
                if (isReplayingProvider.get()) {
                    result = Optional.empty();
                } else {
                    final TimePoint lastestMeasurementTimePoint = measurementTimePointProvider.apply(records.getLast());
                    final TimePoint endTimePoint = getEndTimePoint();
                    if (!endTimePoint.after(lastestMeasurementTimePoint)) {
                        result = Optional.empty();
                    } else {
                        result = Optional.of(new TimeRangeImpl(lastestMeasurementTimePoint, endTimePoint, true));
                    }
                }
            }
            this.noDataRequested = false;
            return result;
        }

        private TimePoint getEndTimePoint() {
            final TimePoint end = endOfTrackingProvider.get(), live = liveTimePointProvider.get();
            return end == null ? live : new MillisecondsTimePoint(Math.min(end.asMillis(), live.asMillis()));
        }

        private void updateCachedRecords(final List<D> records, final boolean append) {
            if (!append) {
                this.records.clear();
            }
            this.records.addAll(records);
        }

        private List<D> getCachedRecords() {
            return records;
        }
    }

}
