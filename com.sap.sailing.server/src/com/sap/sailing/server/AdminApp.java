package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;

public class AdminApp extends HttpServlet {
    private static final long serialVersionUID = -6849138354941569249L;
    
    private static final String PARAM_ACTION = "action";
    
    private static final String ACTION_NAME_LIST_EVENTS = "listevents";

    private static final String ACTION_NAME_ADD_EVENT = "addevent";

    private static final String ACTION_NAME_STOP_EVENT = "stopevent";

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private RacingEventService service;
    
    public AdminApp() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        // grab the service
        service = (RacingEventService) racingEventServiceTracker.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter(PARAM_ACTION);
            if (action != null) {
                if (ACTION_NAME_LIST_EVENTS.equals(action)) {
                    listEvents(resp);
                } else if (ACTION_NAME_ADD_EVENT.equals(action)) {
                    addEvent(req, resp);
                } else if (ACTION_NAME_STOP_EVENT.equals(action)) {
                    stopEvent(req, resp);
                }
            } else {
                resp.getWriter().println("Hello admin!");
            }
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }
    
    private void stopEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException,
            InterruptedException {
        Event event = service.getEventByName(req.getParameter("eventname"));
        if (event != null) {
            service.stopTracking(event);
        }
    }

    private void addEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException,
            URISyntaxException, FileNotFoundException {
        URL paramURL = new URL(req.getParameter("paramURL"));
        URI liveURI = new URI(req.getParameter("liveURI"));
        URI storedURI = new URI(req.getParameter("storedURI"));
        service.addEvent(paramURL, liveURI, storedURI);
    }

    private void listEvents(HttpServletResponse resp) throws IOException {
        JSONArray eventList = new JSONArray();
        for (Event event : service.getAllEvents()) {
            eventList.add(event.getName());
        }
        eventList.writeJSONString(resp.getWriter());
    }
}
