package com.sap.sailing.gwt.ui.client;

import java.util.Date;

public interface TimeZoomChangeListener {

    void onTimeZoomChanged(Date zoomStartTimepoint, Date zoomEndTimepoint);

    void onTimeZoomReset();
}
