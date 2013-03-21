package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.domain.common.impl.Util.Pair;

public interface TimeRangeWithZoomProvider extends TimeRangeProvider {

    public void addTimeZoomChangeListener(TimeZoomChangeListener listener);

    public void removeTimeZoomChangeListener(TimeZoomChangeListener listener);

    public void setTimeZoom(Date zoomStartTimepoint, Date zoomEndTimepoint, TimeZoomChangeListener... listenersNotToNotify);

    public void resetTimeZoom(TimeZoomChangeListener... listenersNotToNotify);
    
    public Pair<Date, Date> getTimeZoom();
    
    public boolean isZoomed();
}
