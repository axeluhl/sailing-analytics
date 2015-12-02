package com.sap.sailing.dashboards.gwt.shared.dispatch.impl;

import java.net.URL;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.RacingEventService;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardDispatchContextImpl implements DashboardDispatchContext{
    private final RacingEventService racingEventService;
    private final Date currentClientTime;
//    private final Date currentServerTime = new Date();
    private String clientLocaleName;
    private final HttpServletRequest request;

    public  DashboardDispatchContextImpl(Date currentClientTime, RacingEventService racingEventService, String clientLocaleName, HttpServletRequest request) {
        this.currentClientTime = currentClientTime;
        this.racingEventService = racingEventService;
        this.clientLocaleName = clientLocaleName;
        this.request = request;
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        return racingEventService;
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
    
}
