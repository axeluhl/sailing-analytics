package com.sap.sailing.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;

public abstract class Servlet extends HttpServlet {
    private static final long serialVersionUID = -6514453597593669376L;

    protected static final String PARAM_ACTION = "action";
    
    protected static final String PARAM_NAME_EVENTNAME = "eventname";

    protected static final String PARAM_NAME_RACENAME = "racename";

    protected static final String PARAM_NAME_TIME = "time";

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private RacingEventService service;
    
    protected Servlet() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        // grab the service
        service = (RacingEventService) racingEventServiceTracker.getService();
    }

    protected RacingEventService getService() {
        return service;
    }

    protected void setService(RacingEventService service) {
        this.service = service;
    }

    protected Event getEvent(HttpServletRequest req) {
        Event event = getService().getEventByName(req.getParameter(PARAM_NAME_EVENTNAME));
        return event;
    }

    protected RaceDefinition getRaceDefinition(HttpServletRequest req) {
        Event event = getEvent(req);
        if (event != null) {
            String racename = req.getParameter(PARAM_NAME_RACENAME);
            for (RaceDefinition race : event.getAllRaces()) {
                if (racename.equals(race.getName())) {
                    return race;
                }
            }
        }
        return null;
    }
}
