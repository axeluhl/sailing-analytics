package com.sap.sse.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Keep timing statistics for a service, an object, or some access method. Each access is expected to be notified to
 * this object by calling {@link #recordTiming(TimePoint, Duration)}. In turn, this object will maintain averages about
 * access timings for one or more durations from "now" backwards into the past, grouped by a set of "ages" that can
 * be specified at {@link #TimingStats(Duration...) construction}. These averages can be obtained by calling
 * {@link #getAverageDurations()} for the current time.
 * <p>
 * 
 * For example, an object of this type can be configured to keep statistics about the last 10s, 30s, 1min, and 5min, for
 * each keeping the average, minimum and maximum time.
 * <p>
 * 
 * The boundary durations are inclusive, meaning that an entry {@link #recordTiming(TimePoint, Duration) recorded} is
 * considered part of the range represented by the age passed to the constructor if it is exactly of that age at the
 * time of recording.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TimingStats {
    /**
     * Compares by the {@link #getWhen() time point of the event}. If that is equal, uses the stable and, within this
     * {@link TimingStats} object unique, {@link #getId() ID} to disambiguate.
     */
    private static class LogEntry implements Comparable<LogEntry> {
        private final TimePoint when;
        private final Duration howLong;
        private final long id;
        public LogEntry(TimePoint when, Duration howLong, long id) {
            super();
            this.when = when;
            this.howLong = howLong;
            this.id = id;
        }
        public TimePoint getWhen() {
            return when;
        }
        public Duration getHowLong() {
            return howLong;
        }
        public long getId() {
            return id;
        }
        @Override
        public int compareTo(LogEntry o) {
            int result = getWhen().compareTo(o.getWhen());
            if (result == 0) {
                result = Long.compare(getId(), o.getId());
            }
            return result;
        }
        @Override
        public String toString() {
            return "LogEntry [when=" + when + ", howLong=" + howLong + ", id=" + id + "]";
        }
    }
    
    /**
     * Keys are the durations for which to maintain averages; the values are maintained transactionally; the elements in
     * represent the sum of all {@link LogEntry#getHowLong() durations} of all {@link #logEntries} that are
     * no older (considering the time point when {@link #updateStatsBasedOnNewNow()} was called last) than
     * the corresponding key tells.
     */
    private final Map<Duration, AtomicLong> agesForAveragesAndTheirDurationSumsInMillis;
    
    /**
     * All log entries, with those removed by {@link #updateStatsBasedOnNewNow()} that are more than
     * {@link #agesForAverages}{@code [agesForAverages.length-1]} before {@link MillisecondsTimePoint#now() now}. The
     * entries are sorted by their "natural order" defined by {@link LogEntry#compareTo(LogEntry)} which sorts by the
     * {@link LogEntry#getWhen() time point} of the event (with the {@link LogEntry#getId() ID} as a secondary sorting
     * criterion for uniqueness).
     */
    private final ConcurrentSkipListSet<LogEntry> logEntries;

    private AtomicLong idCounter;
    
    /**
     * The last time point when {@link #updateStatsBasedOnNewNow()} was invoked to align
     * the averages based on a new time.
     */
    private TimePoint lastNow;

    public TimingStats(Duration... agesForAverages) {
        this(MillisecondsTimePoint.now(), agesForAverages);
    }
    
    TimingStats(TimePoint now, Duration... agesForAverages) {
        if (agesForAverages.length == 0) {
            throw new IllegalArgumentException("At least one age must be provided");
        }
        lastNow = now;
        idCounter = new AtomicLong(0);
        agesForAveragesAndTheirDurationSumsInMillis = new HashMap<>();
        for (final Duration ageForAverages : agesForAverages) {
            agesForAveragesAndTheirDurationSumsInMillis.put(ageForAverages, new AtomicLong(0));
        }
        logEntries = new ConcurrentSkipListSet<>();
    }
    
    public void recordTiming(TimePoint when, Duration howLong) {
        final TimePoint newNow = MillisecondsTimePoint.now();
        recordTiming(newNow, when, howLong);
    }
    
    void recordTiming(TimePoint newNow, TimePoint when, Duration howLong) {
        updateStatsBasedOnNewNow(newNow);
        final Duration ageOfNewEntry = when.until(newNow);
        logEntries.add(new LogEntry(when, howLong, idCounter.getAndIncrement()));
        for (final Entry<Duration, AtomicLong> e : agesForAveragesAndTheirDurationSumsInMillis.entrySet()) {
            if (ageOfNewEntry.compareTo(e.getKey()) <= 0) {
                e.getValue().addAndGet(howLong.asMillis());
            }
        }
    }

    /**
     * Purges aged events from their old slots and updates the {@link #durationsSumsInMillis} accordingly. When the
     * method returns, the durations stored as values in {@link #agesForAveragesAndTheirDurationSumsInMillis} are
     * consistent with {@link #logEntries} based on {@link #lastNow} for determining the {@link LogEntry}'s ages.
     * Entries with a {@link LogEntry#getWhen()} more than the greatest key in
     * {@link #agesForAveragesAndTheirDurationSumsInMillis} before {@code newNow} will be removed from
     * {@link #logEntries}, and of course from their respective {@link #durationsSumsInMillis}.
     * <p>
     * 
     * For each key in {@link #agesForAveragesAndTheirDurationSumsInMillis} and based on the {@link #lastNow} time point and
     * {@code newNow}, finds all {@link LogEntry entries} in {@link #logEntries} that aged beyond the key duration.
     * For each such entry their {@link LogEntry#getHowLong()} duration is subtracted from the duration stored in
     * the value of {@link #agesForAveragesAndTheirDurationSumsInMillis}.<p>
     * 
     * Eventually, the {@link LogEntry entries} older than the greatest key from {@link #agesForAveragesAndTheirDurationSumsInMillis}
     * are removed from {@link #logEntries}.<p>
     * 
     * @param newNow if earlier than {@link #lastNow}, this call is a no-op and the method returns immediately
     */
    private void updateStatsBasedOnNewNow(final TimePoint newNow) {
        // TODO synchronization / locking?
        if (newNow.after(lastNow)) {
            Duration oldestAge = Duration.NULL;
            for (final Entry<Duration, AtomicLong> e : agesForAveragesAndTheirDurationSumsInMillis.entrySet()) {
                if (e.getKey().compareTo(oldestAge) > 0) {
                    oldestAge = e.getKey();
                }
                // An entry was considered in e's duration sum if and only if at lastNow its age,
                // which is LogEntry.getWhen().until(lastNow) was less than or equal to e's key duration.
                // It shall no longer be in e's duration sum if LogEntry().getWhen().until(newNow) is greater than
                // e's key duration.
                // Therefore, in order to find exactly those LogEntry elements that were but no longer are in the
                // right time interval we need to obtain all LogEntry objects whose getWhen() is at or after
                // lastNow.minus(e.getKey()) and before newNow.minus(e.getKey()).
                final TimePoint removeIntervalStart = lastNow.minus(e.getKey());
                final TimePoint removeIntervalEnd = newNow.minus(e.getKey());
                if (removeIntervalStart.before(removeIntervalEnd)) {
                    for (final LogEntry entrySkippingToNext : logEntries.subSet(
                            timePointLogEntry(removeIntervalStart), /* fromInclusive */ true,
                            timePointLogEntry(removeIntervalEnd), /* toInclusive */ false)) {
                        e.getValue().addAndGet(-entrySkippingToNext.getHowLong().asMillis());
                    }
                }
            }
            final Iterator<LogEntry> removingIter = logEntries.headSet(timePointLogEntry(newNow.minus(oldestAge)), /* inclusive */ false).iterator();
            while (removingIter.hasNext()) {
                removingIter.next();
                removingIter.remove();
            }
            lastNow = newNow;
        }
    }
    
    /**
     * Keyed by the durations provided to this object's {@link #TimingStats(Duration...) constructor},
     * the result contains the average duration of the requests that are no older than the key
     * {@link Duration} at the time of calling this method. The value for a key will be {@code null}
     * in case there are no requests in the respective time range.
     */
    public Map<Duration, Duration> getAverageDurations() {
        final TimePoint now = MillisecondsTimePoint.now();
        return getAverageDurations(now);
    }
    
    Map<Duration, Duration> getAverageDurations(TimePoint now) {
        updateStatsBasedOnNewNow(now);
        final LogEntry dummyLogEntryForNow = timePointLogEntry(now);
        final Map<Duration, Duration> result = new HashMap<>();
        for (final Entry<Duration, AtomicLong> e : agesForAveragesAndTheirDurationSumsInMillis.entrySet()) {
            final int eventCount = logEntries.subSet(
                    timePointLogEntry(now.minus(e.getKey())), /* fromInclusive */ true,
                    dummyLogEntryForNow, /* toInclusive */ true).size();
            result.put(e.getKey(), eventCount == 0 ? null : new MillisecondsDurationImpl(e.getValue().get() / eventCount));
        }
        return result;
    }
    
    private LogEntry timePointLogEntry(TimePoint when) {
        return new LogEntry(when, /* howLong */ null, 0);
    }
}
