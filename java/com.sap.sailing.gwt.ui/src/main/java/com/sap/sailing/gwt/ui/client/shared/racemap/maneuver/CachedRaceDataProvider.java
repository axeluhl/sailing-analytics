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

public abstract class CachedRaceDataProvider<K, D> {

    private static final Logger LOGGER = Logger.getLogger(CachedRaceDataProvider.class.getName());

    private final Supplier<TimePoint> startOfTrackingProvider, endOfTrackingProvider, liveTimePointProvider;
    private final Supplier<Boolean> isReplayingProvider;
    private final Function<D, TimePoint> dataTimePointWithOffsetProvider;
    private final Map<K, EntryDataCache> cache = new HashMap<>();
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

    protected abstract void loadData(final Map<K, TimeRange> entryTimeRanges, final boolean incremental,
            final AsyncCallback<Map<K, List<D>>> callback);

    protected abstract void onEntriesDataChange(final Iterable<K> updatedEntries);

    public final void removeAllEntries() {
        this.cache.clear();
    }

    public final void removeEntry(final K entry) {
        this.cache.remove(entry);
    }

    public final boolean hasCachedData() {
        return this.cache.values().stream().anyMatch(EntryDataCache::hasCachedRecords);
    }

    public final void updateEntryData() {
        this.update(cache.keySet(), true);
    }

    public final void ensureEntry(final K entry) {
        this.ensureEntries(Collections.singleton(entry));
    }

    public final void ensureEntries(final Iterable<K> entries) {
        final Set<K> entriesToUpdate = new HashSet<>();
        for (final K entry : entries) {
            if (this.cache.putIfAbsent(entry, new EntryDataCache()) == null) {
                entriesToUpdate.add(entry);
            }
        }
        this.update(entriesToUpdate, false);
    }

    public final Map<K, List<D>> getCachedData() {
        final Map<K, List<D>> result = new HashMap<>(cache.size());
        for (Entry<K, EntryDataCache> entry : cache.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getCachedRecords());
        }
        return result;
    }

    private void update(final Iterable<K> entries, final boolean incremental) {
        final Map<K, TimeRange> entriesToLoadDataFor = getEntriesToLoadDataFor(entries, incremental);
        if (!entriesToLoadDataFor.isEmpty()) {
            this.loadData(entriesToLoadDataFor, incremental, new AsyncCallback<Map<K, List<D>>>() {

                @Override
                public void onSuccess(Map<K, List<D>> result) {
                    final Set<K> entriesToTriggerFullUpdateFor = new HashSet<>();
                    final Set<K> entriesWithChangedData = new HashSet<>();
                    for (Entry<K, List<D>> entry : result.entrySet()) {
                        final K key = entry.getKey();
                        final List<D> loadedRecords = entry.getValue();
                        if (!loadedRecords.isEmpty()) {
                            final EntryDataCache data = cache.get(key);
                            if (data != null) {
                                if (incremental && triggerFullUpdateOnNewData && !data.hasCachedRecords()) {
                                    entriesToTriggerFullUpdateFor.add(key);
                                }
                                entriesWithChangedData.add(key);
                                data.updateCachedRecords(loadedRecords, incremental);
                            }
                        }
                    }
                    if (!entriesWithChangedData.isEmpty()) {
                        CachedRaceDataProvider.this.onEntriesDataChange(entriesWithChangedData);
                    }
                    CachedRaceDataProvider.this.update(entriesToTriggerFullUpdateFor, false);
                }

                @Override
                public void onFailure(Throwable caught) {
                    LOGGER.log(Level.SEVERE, "Failed to load entry data!", caught);
                }
            });
        }
    }

    private Map<K, TimeRange> getEntriesToLoadDataFor(final Iterable<K> entries, final boolean incremental) {
        final Map<K, TimeRange> entriesTimeRanges = new HashMap<>();
        for (final K entry : entries) {
            final EntryDataCache data = cache.get(entry);
            if (data != null) {
                data.getUpdateTimeRange(incremental).ifPresent(timeRange -> entriesTimeRanges.put(entry, timeRange));
            }
        }
        return entriesTimeRanges;
    }

    private class EntryDataCache {

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
