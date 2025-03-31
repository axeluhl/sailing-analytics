package com.sap.sailing.dashboards.gwt.shared.dispatch.impl;

import java.net.URL;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.sap.sailing.dashboards.gwt.shared.DashboardLiveRaceProvider;
import com.sap.sailing.dashboards.gwt.shared.MovingAveragesCache;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardDispatchContextImpl implements DashboardDispatchContext{
    private final RacingEventService racingEventService;
    private final PolarDataService polarDataService;
    private final DashboardLiveRaceProvider dashboardLiveRaceProvider;
    private final MovingAveragesCache movingAveragesCache;
    private final Date currentClientTime;
    private String clientLocaleName;
    private final HttpServletRequest request;

    public DashboardDispatchContextImpl(Date currentClientTime, RacingEventService racingEventService,
            PolarDataService polarDataService, DashboardLiveRaceProvider dashboardLiveRaceProvider,
            MovingAveragesCache movingAveragesCache, String clientLocaleName, HttpServletRequest request) {
        this.currentClientTime = currentClientTime;
        this.racingEventService = racingEventService;
        this.polarDataService = polarDataService;
        this.dashboardLiveRaceProvider = dashboardLiveRaceProvider;
        this.movingAveragesCache = movingAveragesCache;
        this.clientLocaleName = clientLocaleName;
        this.request = request;
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        return racingEventService;
    }
    
    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }
    
    @Override
    public DashboardLiveRaceProvider getDashboardLiveRaceProvider() {
        return dashboardLiveRaceProvider;
    }
    
    @Override
    public Date getCurrentClientTime() {
        return currentClientTime;
    }
    
    @Override
    public String getClientLocaleName() {
        return clientLocaleName;
    }
    
    @Override
    public Locale getClientLocale() {
        return Locale.forLanguageTag(clientLocaleName);
    }
    
    @Override
    public HttpServletRequest getRequest() {
        return request;
    }
    
    @Override
    public URL getRequestBaseURL() throws DispatchException {
        return HomeServiceUtil.getRequestBaseURL(request);
    }

    @Override
    public MovingAveragesCache getMovingAveragesCache() {
        return movingAveragesCache;
    }
}
