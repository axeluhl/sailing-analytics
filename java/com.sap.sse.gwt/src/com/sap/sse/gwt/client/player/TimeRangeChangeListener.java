package com.sap.sse.gwt.client.player;

import java.util.Date;

public interface TimeRangeChangeListener {
    void onTimeRangeChanged(Date fromTime, Date toTime);
}
