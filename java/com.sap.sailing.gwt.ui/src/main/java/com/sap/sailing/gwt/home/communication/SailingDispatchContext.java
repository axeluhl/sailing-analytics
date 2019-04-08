package com.sap.sailing.gwt.home.communication;

import java.util.Date;
import java.util.Locale;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.windfinder.WindFinderTrackerFactory;
import com.sap.sailing.news.EventNewsService;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.statistics.TrackedRaceStatisticsCache;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;

/**
 * {@link DispatchContext} implementation, which is passed to backend-side {@link SailingAction} executions to provide
 * access to the required services related to the sailing domain.
 */
public interface SailingDispatchContext extends DispatchContext {
    @GwtIncompatible
    RacingEventService getRacingEventService();
    
    @GwtIncompatible
    WindFinderTrackerFactory getWindFinderTrackerFactory();

    @GwtIncompatible
    EventNewsService getEventNewsService();
    
    @GwtIncompatible
    TrackedRaceStatisticsCache getTrackedRaceStatisticsCache();

    @GwtIncompatible
    String getClientLocaleName();

    @GwtIncompatible
    Locale getClientLocale();

    @GwtIncompatible
    Date getCurrentClientTime();
    
    <T> T getPreferenceForCurrentUser(String preferenceKey);
    
    void setPreferenceForCurrentUser(String preferenceKey, Object preference);
}
