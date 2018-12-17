package com.sap.sailing.dashboards.gwt.shared.dispatch;

import java.util.Date;
import java.util.Locale;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.DashboardLiveRaceProvider;
import com.sap.sailing.dashboards.gwt.shared.MovingAveragesCache;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardDispatchContext extends DispatchContext {

    @GwtIncompatible
    RacingEventService getRacingEventService();
    
    @GwtIncompatible
    PolarDataService getPolarDataService();
    
    @GwtIncompatible
    DashboardLiveRaceProvider getDashboardLiveRaceProvider();
    
    @GwtIncompatible
    MovingAveragesCache getMovingAveragesCache();

    @GwtIncompatible
    String getClientLocaleName();

    @GwtIncompatible
    Locale getClientLocale();

    @GwtIncompatible
    Date getCurrentClientTime();
}
