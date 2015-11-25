package com.sap.sailing.gwt.home.communication;

import java.util.Date;
import java.util.Locale;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.DispatchContext;
import com.sap.sailing.news.EventNewsService;
import com.sap.sailing.server.RacingEventService;


public interface SailingDispatchContext extends DispatchContext {
    @GwtIncompatible
    RacingEventService getRacingEventService();

    @GwtIncompatible
    EventNewsService getEventNewsService();

    @GwtIncompatible
    String getClientLocaleName();

    @GwtIncompatible
    Locale getClientLocale();

    @GwtIncompatible
    Date getCurrentClientTime();
    
}
