package com.sap.sailing.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.impl.Activator;
import com.sap.sailing.util.DateParser;
import com.sap.sailing.util.InvalidDateException;

public abstract class SailingServerHttpServlet extends HttpServlet {
    private static final long serialVersionUID = -6514453597593669376L;

    protected static final String PARAM_ACTION = "action";
    
    protected static final String PARAM_NAME_REGATTANAME = "regattaname";

    protected static final String PARAM_NAME_RACENAME = "racename";

    protected static final String PARAM_NAME_TIME = "time";

    protected static final String PARAM_NAME_TIME_MILLIS = "timeasmillis";

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    protected SailingServerHttpServlet() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService();
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

    protected TimePoint getTimePoint(HttpServletRequest req, String nameOfISOTimeParam, String nameOfMillisTime,
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
}
