package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.player.TimeRangeProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public abstract class CachedRaceDataProvider<C, D> {

    private static final Logger LOGGER = Logger.getLogger(CachedRaceDataProvider.class.getName());

    private final Supplier<TimePoint> startOfTrackingProvider, endOfTrackingProvider, liveTimePointProvider;
    private final Supplier<Boolean> isReplayingProvider;
    private final Function<D, TimePoint> dataTimePointWithOffsetProvider;
    private final Map<C, CompetitorDataCache> cache = new HashMap<>();
    private final boolean triggerFullUpdateOnNewData;

    public CachedRaceDataProvider(final TimeRangeProvider timeRangeProvider, final Timer timer,
            final Function<D, Date> dataTimePointProvider, final Supplier<Long> dataOffsetProvider,
            final boolean triggerFullUpdateOnNewData) {
        final Function<Supplier<Date>, Supplier<TimePoint>> converter = s -> () -> new MillisecondsTimePoint(s.get());
        this.startOfTrackingProvider = converter.apply(timeRangeProvider::getFromTime);
        this.endOfTrackingProvider = converter.apply(timeRangeProvider::getToTime);
        this.liveTimePointProvider = timer::getLiveTimePoint;
        this.isReplayingProvider = () -> timer.getPlayMode() == PlayModes.Replay;
        this.dataTimePointWithOffsetProvider = data -> new MillisecondsTimePoint(
                dataTimePointProvider.apply(data).getTime() + dataOffsetProvider.get());
        this.triggerFullUpdateOnNewData = triggerFullUpdateOnNewData;
    }

    protected abstract void loadData(final Map<C, TimeRange> competitorTimeRanges, final boolean incremental,
            final AsyncCallback<Map<C, List<D>>> callback);

    protected abstract void onCompetitorsDataChange(final Iterable<C> updatedCompetitors);

    public final void removeAllCompetitors() {
        this.cache.clear();
    }

    public final void removeCompetitor(final C competitor) {
        this.cache.remove(competitor);
    }

    public final boolean hasCachedData() {
        return this.cache.values().stream().anyMatch(CompetitorDataCache::hasCachedRecords);
    }

    public final void updateCompetitorData() {
        this.update(cache.keySet(), true);
    }

    public final void ensureCompetitor(final C competitor) {
        this.ensureCompetitors(Collections.singleton(competitor));
    }

    public final void ensureCompetitors(final Iterable<C> competitors) {
        final Set<C> competitorsToUpdate = new HashSet<>();
        for (final C comp : competitors) {
            if (this.cache.putIfAbsent(comp, new CompetitorDataCache()) == null) {
                competitorsToUpdate.add(comp);
            }
        }
        this.update(competitorsToUpdate, false);
    }

    public final Map<C, List<D>> getCachedData() {
        final Map<C, List<D>> result = new HashMap<>(cache.size());
        for (Entry<C, CompetitorDataCache> entry : cache.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getCachedRecords());
        }
        return result;
    }

    private void update(final Iterable<C> competitors, final boolean incremental) {
        final Map<C, TimeRange> competitorsToLoadDataFor = getCompetitorsToLoadDataFor(competitors, incremental);
        if (!competitorsToLoadDataFor.isEmpty()) {
            this.loadData(competitorsToLoadDataFor, incremental, new AsyncCallback<Map<C, List<D>>>() {

                @Override
                public void onSuccess(Map<C, List<D>> result) {
                    final Set<C> competitorsToTriggerFullUpdateFor = new HashSet<>();
                    final Set<C> competitorsWithChangedData = new HashSet<>();
                    for (Entry<C, List<D>> entry : result.entrySet()) {
                        final C competitor = entry.getKey();
                        final List<D> loadedRecords = entry.getValue();
                        if (!loadedRecords.isEmpty()) {
                            final CompetitorDataCache data = cache.get(competitor);
                            if (data != null) {
                                if (incremental && triggerFullUpdateOnNewData && !data.hasCachedRecords()) {
                                    competitorsToTriggerFullUpdateFor.add(competitor);
                                }
                                competitorsWithChangedData.add(competitor);
                                data.updateCachedRecords(loadedRecords, incremental);
                            }
                        }
                    }
                    if (!competitorsWithChangedData.isEmpty()) {
                        CachedRaceDataProvider.this.onCompetitorsDataChange(competitorsWithChangedData);
                    }
                    CachedRaceDataProvider.this.update(competitorsToTriggerFullUpdateFor, false);
                }

                @Override
                public void onFailure(Throwable caught) {
                    LOGGER.log(Level.SEVERE, "Failed to load competitor data!", caught);
                }
            });
        }
    }

    private Map<C, TimeRange> getCompetitorsToLoadDataFor(final Iterable<C> competitors, final boolean incremental) {
        final Map<C, TimeRange> competitorTimeRanges = new HashMap<>();
        for (final C comp : competitors) {
            final CompetitorDataCache data = cache.get(comp);
            if (data != null) {
                data.getUpdateTimeRange(incremental).ifPresent(timeRange -> competitorTimeRanges.put(comp, timeRange));
            }
        }
        return competitorTimeRanges;
    }

    private class CompetitorDataCache {

        private final List<D> records = new ArrayList<>();

        private final Optional<TimeRange> getUpdateTimeRange(final boolean incremental) {
            final Optional<TimeRange> result;
            if (records.isEmpty() || !incremental) {
                result = Optional.of(new TimeRangeImpl(startOfTrackingProvider.get(), getEndTimePoint(), true));
            } else {
                if (isReplayingProvider.get()) {
                    result = Optional.empty();
                } else {
                    final D latestRecord = records.get(records.size() - 1);
                    final TimePoint lastDataTimePointWithOffset = dataTimePointWithOffsetProvider.apply(latestRecord);
                    final TimePoint endTimePoint = getEndTimePoint();
                    if (!endTimePoint.after(lastDataTimePointWithOffset)) {
                        result = Optional.empty();
                    } else {
                        result = Optional.of(new TimeRangeImpl(lastDataTimePointWithOffset, endTimePoint, true));
                    }
                }
            }
            return result;
        }

        private final TimePoint getEndTimePoint() {
            final TimePoint end = endOfTrackingProvider.get(), live = liveTimePointProvider.get();
            return end == null ? live : new MillisecondsTimePoint(Math.min(end.asMillis(), live.asMillis()));
        }

        private final void updateCachedRecords(final List<D> records, final boolean append) {
            if (!append) {
                this.records.clear();
            }
            this.records.addAll(records);
        }

        private final List<D> getCachedRecords() {
            return records;
        }

        private final boolean hasCachedRecords() {
            return !getCachedRecords().isEmpty();
        }
    }

}
