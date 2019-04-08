package com.sap.sse.gwt.client.player;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;

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
        final boolean fromChanged = !Util.equalsWithNull(this.fromTime, fromTime);
        final boolean toChanged = !Util.equalsWithNull(this.toTime, toTime);
        if (fromChanged) {
            // Note: java.util.Date is not immutable, therefore only a copy is really safe
            this.fromTime = fromTime != null ? new Date(fromTime.getTime()) : null;
        }
        if (toChanged) {
            // Note: java.util.Date is not immutable, therefore only a copy is really safe
            this.toTime = toTime != null ? new Date(toTime.getTime()) : null;
        }
        if (fromChanged || toChanged) {
            for (TimeRangeChangeListener listener : listeners) {
                if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                    listener.onTimeRangeChanged(fromTime, toTime);
                }
            }
        }
    }
    
    public Util.Pair<Date, Date> getTimeRange() {
        return new Util.Pair<Date, Date>(fromTime, toTime);
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
