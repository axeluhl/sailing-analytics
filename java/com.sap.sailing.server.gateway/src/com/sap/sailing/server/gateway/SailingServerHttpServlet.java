package com.sap.sailing.server.gateway;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.DateParser;

public abstract class SailingServerHttpServlet extends HttpServlet {
    private static final long serialVersionUID = -6514453597593669376L;

    protected static final String PARAM_ACTION = "action";
    
    protected static final String PARAM_NAME_REGATTANAME = "regattaname";

    protected static final String PARAM_NAME_RACENAME = "racename";

    protected static final String PARAM_NAME_TIME = "time";

    protected static final String PARAM_NAME_TIME_MILLIS = "timeasmillis";
    
    private static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext"; 

    private BundleContext context;
    
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private ServiceTracker<SecurityService, SecurityService> securityServiceTracker;
    
    private TypeBasedServiceFinderFactory serviceFinderFactory;

    private ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> tracTracAdapterFactoryTracker;
    
    protected SailingServerHttpServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
       super.init(config);  
       context = (BundleContext) config.getServletContext().getAttribute(OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME);  
       racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
       racingEventServiceTracker.open();
       securityServiceTracker = new ServiceTracker<SecurityService, SecurityService>(context, SecurityService.class.getName(), null);
       securityServiceTracker.open();
       tracTracAdapterFactoryTracker = new ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory>(context, TracTracAdapterFactory.class.getName(), null);
       tracTracAdapterFactoryTracker.open();
       serviceFinderFactory = new CachedOsgiTypeBasedServiceFinderFactory(context);
   }
    
    protected BundleContext getContext() {
        return context;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (racingEventServiceTracker != null) {
            racingEventServiceTracker.close();
        }
    }
    
    public RacingEventService getService() {
        return racingEventServiceTracker.getService();
    }
    
    public SecurityService getSecurityService() {
        return securityServiceTracker.getService();
    }
    
    public TracTracAdapterFactory getTracTracAdapterFactory() {
        return tracTracAdapterFactoryTracker.getService();
    }

    protected Regatta getRegatta(HttpServletRequest req) {
        Regatta regatta = getService().getRegattaByName(req.getParameter(PARAM_NAME_REGATTANAME));
        return regatta;
    }

    protected RaceDefinition getRaceDefinition(HttpServletRequest req) {
        Regatta regatta = getRegatta(req);
        if (regatta != null) {
            String racename = req.getParameter(PARAM_NAME_RACENAME);
            for (RaceDefinition race : regatta.getAllRaces()) {
                if (race.getName().equals(racename)) {
                    return race;
                }
            }
        }
        return null;
    }

    protected RaceDefinition getRaceDefinition(Regatta regatta, HttpServletRequest req) {
        String racename = req.getParameter(PARAM_NAME_RACENAME);
        for (RaceDefinition race : regatta.getAllRaces()) {
            if (race.getName().equals(racename)) {
                return race;
            }
        }
        return null;
    }

    protected TimePoint readTimePointParam(HttpServletRequest req, String nameOfISOTimeParam, String nameOfMillisTime) throws InvalidDateException {
        return readTimePointParam(req, nameOfISOTimeParam, nameOfMillisTime, null);
    }
    
    protected TimePoint readTimePointParam(HttpServletRequest req, String nameOfISOTimeParam, String nameOfMillisTime,
            TimePoint defaultValue) throws InvalidDateException {
        String time = req.getParameter(nameOfISOTimeParam);
        TimePoint timePoint;
        if (time != null && time.length() > 0) {
            timePoint = new MillisecondsTimePoint(DateParser.parse(time).getTime());
        } else {
            String timeAsMillis = req.getParameter(nameOfMillisTime);
            if (timeAsMillis != null && timeAsMillis.length() > 0) {
                timePoint = new MillisecondsTimePoint(Long.valueOf(timeAsMillis));
            } else {
                timePoint = defaultValue;
            }
        }
        return timePoint;
    }

    protected TrackedRace getTrackedRace(HttpServletRequest req) {
        Regatta regatta = getRegatta(req);
        RaceDefinition race = getRaceDefinition(req);
        TrackedRace trackedRace = null;
        if (regatta != null && race != null) {
            trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
        }
        return trackedRace;
    }

    protected TypeBasedServiceFinderFactory getServiceFinderFactory() {
    	return serviceFinderFactory;
    }
}
