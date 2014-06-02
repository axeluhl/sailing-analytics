package com.sap.sse.gwt.client.player;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.UtilNew.Pair;

public class TimeRangeModel implements TimeRangeProvider {

    /**
     * The 'from' time of the time interval. May be <code>null</code> if not yet initialized.
     */
    protected Date fromTime;
    
    /**
     * The 'to' time of the time interval. May be <code>null</code> if not yet initialized.
     */
    protected Date toTime;
    
    protected final Set<TimeRangeChangeListener> listeners;
    
    public TimeRangeModel() {
        listeners = new HashSet<TimeRangeChangeListener>();
    }

    @Override
    public void addTimeRangeChangeListener(TimeRangeChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTimeRangeChangeListener(TimeRangeChangeListener listener) {
        listeners.remove(listener);
    }

    public void setTimeRange(final Date fromTime, final Date toTime, TimeRangeChangeListener... listenersNotToNotify) {
        this.fromTime = fromTime != null ? new Date(fromTime.getTime()) : null;
        this.toTime = toTime != null ? new Date(toTime.getTime()) : null;

        for (TimeRangeChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onTimeRangeChanged(fromTime, toTime);
            }
        }
    }
    
    public Pair<Date, Date> getTimeRange() {
        return new Pair<Date, Date>(fromTime, toTime);
    }

    public Date getFromTime() {
        return fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    @Override
    public String toString() {
        return "TimeRangeModel [fromTime=" + fromTime + ", toTime=" + toTime + "]";
    }
}
