package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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

/**
 * Abstract provider for grouped race data which supports full and incremental updates as well as caching functionality.
 * Triggering of updates mainly depends on the supplied {@link TimeRangeProvider time range provider}'s
 * {@link TimeRangeProvider#getFromTime() start} and {@link TimeRangeProvider#getToTime() end} of tracking and the
 * supplied {@link Timer timer}'s {@link Timer#getPlayMode() play mode} and {@link Timer#getLiveTimePoint() live time
 * point} or are forced by adding {@link #ensureEntry(Object) one} or {@link #ensureEntries(Iterable) more} new entries
 * to this race data provider.
 *
 * @param <K>
 *            the actual cache entry type to group data by
 * @param <D>
 *            the actual type of cached data sequences
 */
public abstract class CachedRaceDataProvider<K, D> {

    private static final Logger LOGGER = Logger.getLogger(CachedRaceDataProvider.class.getName());

    private final Supplier<TimePoint> startOfTrackingProvider, endOfTrackingProvider, liveTimePointProvider;
    private final Supplier<Boolean> isReplayingProvider;
    private final Function<D, TimePoint> dataTimePointProvider, dataTimePointWithOffsetProvider;
    private final Map<K, EntryDataCache> cache = new HashMap<>();
    private final boolean triggerFullUpdateOnNewData;

    /**
     * Creates a new {@link CachedRaceDataProvider} instance configured by the provided parameter values.
     * 
     * @param timeRangeProvider
     *            {@link TimeRangeProvider} to determine current start and end of tracking
     * @param timer
     *            {@link Timer} to determine the current {@link PlayModes play mode} and live time point
     * @param dataTimePointProvider
     *            {@link Function} to determine the a data's actual time point
     * @param dataOffsetProvider
     *            {@link Supplier} to determine the offset to load from after the latest received data to avoid loading
     *            of duplicated data in case of micro displacements and shifts for data sequences with specific timing
     * @param triggerFullUpdateOnNewData
     *            flag to determine whether or not the data for an entry should be fully updated if an incremental
     *            update provides new data, e.g. to consolidate data sequences
     */
    public CachedRaceDataProvider(final TimeRangeProvider timeRangeProvider, final Timer timer,
            final Function<D, Date> dataTimePointProvider, final Supplier<Long> dataOffsetProvider,
            final boolean triggerFullUpdateOnNewData) {
        final Function<Supplier<Date>, Supplier<TimePoint>> converter = s -> () -> new MillisecondsTimePoint(s.get());
        this.startOfTrackingProvider = converter.apply(timeRangeProvider::getFromTime);
        this.endOfTrackingProvider = converter.apply(timeRangeProvider::getToTime);
        this.liveTimePointProvider = timer::getLiveTimePoint;
        this.isReplayingProvider = () -> timer.getPlayMode() == PlayModes.Replay;
        this.dataTimePointProvider = dataTimePointProvider.andThen(MillisecondsTimePoint::new);
        final UnaryOperator<TimePoint> addOffsetProvider = timePoint -> timePoint.plus(dataOffsetProvider.get());
        this.dataTimePointWithOffsetProvider = this.dataTimePointProvider.andThen(addOffsetProvider);
        this.triggerFullUpdateOnNewData = triggerFullUpdateOnNewData;
    }

    /**
     * If at least one of this {@link CachedRaceDataProvider provider}'s entries need to be updated, this method is
     * called with the entries to load data for including their respective {@link TimeRange time ranges}, a flag to
     * specify the type of update and a {@link AsyncCallback callback} to actually update this provider's cached data.
     * 
     * @param entryTimeRanges
     *            {@link Map} of the entries and their respective {@link TimeRange time ranges} to load data for
     * @param incremental
     *            flag to specify whether the update is incremental or not
     * @param callback
     *            {@link AsyncCallback} which updates the provider's cached data if it was loaded
     *            {@link AsyncCallback#onSuccess(Object) successfully}
     */
    protected abstract void loadData(final Map<K, TimeRange> entryTimeRanges, final boolean incremental,
            final AsyncCallback<Map<K, List<D>>> callback);

    /**
     * Callback method which is invoked if {@link #loadData(Map, boolean, AsyncCallback) loading} actually contained
     * data sequences for at least one of the provider's entries.
     * 
     * @param updatedEntries
     *            the entries which loading actually contained data for
     */
    protected abstract void onEntriesDataChange(final Iterable<K> updatedEntries);

    /**
     * Removes all entries from this {@link CachedRaceDataProvider provider}.
     */
    public final void removeAllEntries() {
        this.cache.clear();
    }

    /**
     * Removes the specified entry from this {@link CachedRaceDataProvider provider}.
     * 
     * @param entry
     *            the entry to remove
     */
    public final void removeEntry(final K entry) {
        this.cache.remove(entry);
    }

    /**
     * Determine whether or not data sequences for at least one entry are cached by this {@link CachedRaceDataProvider
     * provider}.
     * 
     * @return <code>true</code> if there is a data sequences for at least one entry, <code>false</code> otherwise
     */
    public final boolean hasCachedData() {
        return this.cache.values().stream().anyMatch(EntryDataCache::hasCachedRecords);
    }

    /**
     * Updates all entries of this {@link CachedRaceDataProvider provider} by triggering an according incremental
     * {@link #loadData(Map, boolean, AsyncCallback) data loading} if necessary.
     */
    public final void updateEntryData() {
        this.update(cache.keySet(), true);
    }

    /**
     * Ensures the provided entry in this {@link CachedRaceDataProvider provider} and triggers a full update if the
     * entry has not been present before.
     * 
     * @param entry
     *            the entry to ensure
     */
    public final void ensureEntry(final K entry) {
        this.ensureEntries(Collections.singleton(entry));
    }

    /**
     * Ensures the provided entries in this {@link CachedRaceDataProvider provider} and triggers a full update for those
     * entries which has not been present before.
     * 
     * @param entry
     *            the entries to ensure
     */
    public final void ensureEntries(final Iterable<K> entries) {
        final Set<K> entriesToUpdate = new HashSet<>();
        for (final K entry : entries) {
            if (this.cache.putIfAbsent(entry, new EntryDataCache()) == null) {
                entriesToUpdate.add(entry);
            }
        }
        this.update(entriesToUpdate, false);
    }

    /**
     * Provides access to all cached data sequences which are cache by this {@link CachedRaceDataProvider provider}
     * grouped by the respective entries.
     * 
     * @return {@link Map} of entries and their associated cached data sequences
     */
    public final Map<K, Iterable<D>> getCachedData() {
        final Map<K, Iterable<D>> result = new HashMap<>(cache.size());
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
                                if (incremental && triggerFullUpdateOnNewData && data.hasCachedRecords()) {
                                    entriesToTriggerFullUpdateFor.add(key);
                                }
                                entriesWithChangedData.add(key);
                                if (incremental) {
                                    data.updateCachedRecords(entriesToLoadDataFor.get(key), loadedRecords);
                                } else {
                                    data.updateCachedRecords(loadedRecords);
                                }
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

        private final TreeSet<D> records = new TreeSet<>(Comparator.comparing(dataTimePointProvider));

        /**
         * Determines the {@link TimeRange} for which the data should be updated, if an update is required.
         * 
         * @param incremental
         *            flag to specify whether the update would be incremental or not
         * @return an {@link Optional} contained the {@link TimeRange} for the required update or an
         *         {@link Optional#empty() empty Optional} if no update is required
         */
        private final Optional<TimeRange> getUpdateTimeRange(final boolean incremental) {
            final Optional<TimeRange> result;
            if (records.isEmpty() || !incremental) {
                result = Optional.of(new TimeRangeImpl(startOfTrackingProvider.get(), getEndTimePoint(), true));
            } else {
                if (isReplayingProvider.get()) {
                    result = Optional.empty();
                } else {
                    final TimePoint lastDataTimePointWithOffset = dataTimePointWithOffsetProvider.apply(records.last());
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

        /**
         * Updates the cache in the given {@link TimeRange time range} by removing all existing data which is included
         * and adding the provided data.
         * 
         * @param timeRange
         *            {@link TimeRange} for which included data has to be removed
         * @param loadedRecords
         *            data sequence to add to the cache
         */
        private final void updateCachedRecords(final TimeRange timeRange, final List<D> loadedRecords) {
            for (Iterator<D> iterator = records.iterator(); iterator.hasNext();) {
                if (timeRange.includes(dataTimePointProvider.apply(iterator.next()))) {
                    iterator.remove();
                }
            }
            this.records.addAll(loadedRecords);
        }

        /**
         * Fully updates the cache by clearing existing and adding the provided data.
         * 
         * @param loadedRecords
         *            data sequence to add to the cache
         */
        private final void updateCachedRecords(final List<D> loadedRecords) {
            this.records.clear();
            this.records.addAll(loadedRecords);
        }

        private final Iterable<D> getCachedRecords() {
            return records;
        }

        private final boolean hasCachedRecords() {
            return !records.isEmpty();
        }
    }

}
