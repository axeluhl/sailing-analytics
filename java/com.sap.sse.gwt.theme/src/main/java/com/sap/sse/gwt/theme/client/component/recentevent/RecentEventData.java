package com.sap.sse.gwt.theme.client.component.recentevent;

import java.util.Date;

import com.google.gwt.user.client.Command;

public interface RecentEventData {
    String getEventImageUrl();
    String getVenue();
    String getLocation();
    Date getEventStart();
    Date getEventEnd();
    Command getCommand();
    String getEventName();
    boolean isLive();
}
