package com.sap.sailing.gwt.ui.client;

import java.util.Date;

public interface TimeZoomChangeListener {

    void onTimeZoom(Date zoomStartTimepoint, Date zoomEndTimepoint);

    void onTimeZoomReset();
}
