package com.sap.sse.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Keep timing statistics for a service, an object, or some access method.
 * Each access is expected to be notified to this object. In turn, this
 * object will maintain averages about access timings for one or more
 * durations from "now" backwards into the past.<p>
 * 
 * For example, an object of this type can be configured to keep statistics
 * about the last 10s, 30s, 1min, and 5min, for each keeping the average,
 * minimum and maximum time.
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
    }
    
    /**
     * The durations for which to maintain averages; sorted from short to long
     */
    private final Duration[] agesForAverages;
    
    /**
     * All log entries, with those removed by {@link #updateStatsBasedOnNewNow()} that are more than
     * {@link #agesForAverages}{@code [agesForAverages.length-1]} before {@link MillisecondsTimePoint#now() now}. The
     * entries are sorted by their "natural order" defined by {@link LogEntry#compareTo(LogEntry)} which sorts by the
     * {@link LogEntry#getWhen() time point} of the event (with the {@link LogEntry#getId() ID} as a secondary sorting
     * criterion for uniqueness).
     */
    private final ConcurrentSkipListSet<LogEntry> logEntries;

    /**
     * Maintained transactionally, the elements in this array represent the sum of all
     * {@link LogEntry#getHowLong() durations} of all {@link #logEntries} that are no older
     * (considering the time point when {@link #updateStatsBasedOnNewNow()} was called last)
     * than {@link #agesForAverages} for the respective array index.
     */
    private final AtomicLong[] durationsSumsInMillis;
    
    private AtomicLong idCounter;
    
    /**
     * The last time point when {@link #updateStatsBasedOnNewNow()} was invoked to align
     * the averages based on a new time.
     */
    private TimePoint lastNow;

    public TimingStats(Duration... agesForAverages) {
        lastNow = MillisecondsTimePoint.now();
        idCounter = new AtomicLong(0);
        this.agesForAverages = Arrays.copyOf(agesForAverages, agesForAverages.length);
        Arrays.sort(this.agesForAverages);
        logEntries = new ConcurrentSkipListSet<>();
        durationsSumsInMillis = new AtomicLong[agesForAverages.length];
        for (int i=0; i<agesForAverages.length; i++) {
            durationsSumsInMillis[i] = new AtomicLong(0);
        }
    }
    
    public void recordTiming(TimePoint when, Duration howLong) {
        final TimePoint newNow = MillisecondsTimePoint.now();
        updateStatsBasedOnNewNow(newNow);
        final Duration ageOfNewEntry = newNow.until(when);
        logEntries.add(new LogEntry(when, howLong, idCounter.getAndIncrement()));
        for (int i=0; i<agesForAverages.length && agesForAverages[i].compareTo(ageOfNewEntry) >= 0; i++) {
            durationsSumsInMillis[i].addAndGet(howLong.asMillis());
        }
    }

    /**
     * Purges aged events from their old slots and updates the {@link #durationsSumsInMillis} accordingly. When the
     * method returns, {@link #durationsSumsInMillis} is consistent with {@link #logEntries} based on {@link #lastNow}
     * for determining the {@link LogEntry}'s ages. Entries with a {@link LogEntry#getWhen()} more than the greatest
     * {@link #agesForAverages} before {@code newNow} will be removed from {@link #logEntries}, and of course from their
     * respective {@link #durationsSumsInMillis}.
     * <p>
     * 
     * Analyzes all {@link #logEntries} from oldest to newest and computes each entry's {@link LogEntry#getWhen()} age
     * based on the {@code newNow} and on the {@link #lastNow}. It then maps the two ages to the
     * {@link #agesForAverages} groups. If both fall into the same group, there is no change for the event, and we can
     * proceed to the next group. Otherwise, the event dropped out of its old group because of increased age and now has
     * to be added to a new group or has to be eliminated if older than the maximum age of the oldest group. The
     * previous group's {@link #durationsSumsInMillis} is reduced by the entry's {@link LogEntry#getHowLong() duration},
     * and if the event ends up in a new group instead of dropping out of this object's stats, the new group's
     * {@link #durationsSumsInMillis} is increased by the entry's duration.
     */
    private void updateStatsBasedOnNewNow(final TimePoint newNow) {
        // TODO synchronization / locking?
        Iterator<LogEntry> i = logEntries.descendingIterator();
        if (i.hasNext()) {
            final LogEntry latestEntry = i.next();
            
        }
            
        // TODO Auto-generated method stub
        lastNow = newNow;
    }
    
    /**
     * Keyed by the durations provided to this object's {@link #TimingStats(Duration...) constructor},
     * the result contains the average duration of the requests that are no older than the key
     * {@link Duration} at the time of calling this method. The value for a key will be {@code null}
     * in case there are no requests in the respective time range.
     */
    public Map<Duration, Duration> getAverageDurations() {
        final TimePoint now = MillisecondsTimePoint.now();
        final LogEntry dummyLogEntryForNow = timePointLogEntry(now);
        final Map<Duration, Duration> result = new HashMap<>();
        updateStatsBasedOnNewNow(now);
        for (int i=0; i<agesForAverages.length; i++) {
            final int eventCount = logEntries.subSet(
                    timePointLogEntry(now.minus(agesForAverages[i])), /* toInclusive */ false,
                    dummyLogEntryForNow, /* fromInclusive */ true).size();
            result.put(agesForAverages[i], new MillisecondsDurationImpl(durationsSumsInMillis[i].get() / eventCount));
        }
        return result;
    }
    
    private LogEntry timePointLogEntry(TimePoint when) {
        return new LogEntry(when, /* howLong */ null, 0);
    }
}
