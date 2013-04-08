package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.domain.common.impl.Util.Pair;

public interface TimeRangeProvider {

    public void addTimeRangeChangeListener(TimeRangeChangeListener listener);

    public void removeTimeRangeChangeListener(TimeRangeChangeListener listener);

    public void setTimeRange(Date fromTime, Date toTime, TimeRangeChangeListener... listenersNotToNotify);
    
    public Date getFromTime();

    public Date getToTime();

    public Pair<Date, Date> getTimeRange();
}
