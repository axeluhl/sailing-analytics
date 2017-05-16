package com.sap.sse.gwt.client.player;

import java.util.Date;

public interface TimeZoomChangeListener {

    void onTimeZoomChanged(Date zoomStartTimepoint, Date zoomEndTimepoint);

    void onTimeZoomReset();
}
