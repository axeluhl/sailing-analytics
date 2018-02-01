package com.sap.sse.gwt.client.player;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;

public class TimeRangeWithZoomModel extends TimeRangeModel implements TimeRangeWithZoomProvider {
    private Date zoomStartTimepoint;
    private Date zoomEndTimepoint;
    private boolean isTimeZoomed;
    
    private final Set<TimeZoomChangeListener> listeners;
    
    public TimeRangeWithZoomModel() {
        isTimeZoomed = false;
        listeners = new HashSet<TimeZoomChangeListener>();
    }

    @Override
    public void addTimeZoomChangeListener(TimeZoomChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTimeZoomChangeListener(TimeZoomChangeListener listener) {
        listeners.remove(listener);
    }

    public void setTimeZoom(Date zoomStartTimepoint, Date zoomEndTimepoint, TimeZoomChangeListener... listenersNotToNotify) {
        this.zoomStartTimepoint = zoomStartTimepoint;
        this.zoomEndTimepoint = zoomEndTimepoint;
        this.isTimeZoomed = true;
        for (TimeZoomChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onTimeZoomChanged(zoomStartTimepoint, zoomEndTimepoint);
            }
        }
    }
    
    public void resetTimeZoom(TimeZoomChangeListener... listenersNotToNotify) {
        isTimeZoomed = false;
        this.zoomStartTimepoint = null;
        this.zoomEndTimepoint = null;
        for (TimeZoomChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onTimeZoomReset();
            }
        }
    }
    
    public Util.Pair<Date, Date> getTimeZoom() {
        return new Util.Pair<Date, Date>(zoomStartTimepoint, zoomEndTimepoint);
    }
    
    public boolean isZoomed() {
        return isTimeZoomed;
    }

    @Override
    public String toString() {
        return "TimeRangeWithZoomModel [fromTime=" + fromTime + ", toTime=" + toTime + ", zoomStartTimepoint=" + zoomStartTimepoint + ", zoomEndTimepoint="
                + zoomEndTimepoint + ", isTimeZoomed=" + isTimeZoomed + "]";
    }
}
