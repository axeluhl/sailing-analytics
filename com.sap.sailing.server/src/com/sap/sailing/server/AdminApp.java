package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.Util.Pair;

public class AdminApp extends HttpServlet {
    private static final long serialVersionUID = -6849138354941569249L;
    
    private static final String PARAM_ACTION = "action";
    
    private static final String ACTION_NAME_LIST_EVENTS = "listevents";

    private static final String ACTION_NAME_ADD_EVENT = "addevent";

    private static final String ACTION_NAME_STOP_EVENT = "stopevent";
    
    private static final String ACTION_NAME_LIST_WINDTRACKERS = "listwindtrackers";
    
    private static final String ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "receiveexpeditionwind";

    private static final String ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "stopreceivingexpeditionwind";

    private static final String PARAM_NAME_EVENTNAME = "eventname";

    private static final String PARAM_NAME_RACENAME = "racename";

    private static final String PARAM_NAME_PORT = "port";


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
                } else if (ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND.equals(action)) {
                    startReceivingExpeditionWindForRace(req, resp);
                } else if (ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND.equals(action)) {
                    stopReceivingExpeditionWindForRace(req, resp);
                } else if (ACTION_NAME_LIST_WINDTRACKERS.equals(action)) {
                    listWindTrackers(req, resp);
                }
            } else {
                resp.getWriter().println("Hello admin!");
            }
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }
    
    private void listWindTrackers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONArray windTrackers = new JSONArray();
        for (Pair<Event, RaceDefinition> eventAndRace : service.getWindTrackedRaces()) {
            JSONObject windTracker = new JSONObject();
            windTracker.put("eventname", eventAndRace.getA().getName());
            windTracker.put("racename", eventAndRace.getB().getName());
            windTrackers.add(windTracker);
        }
        windTrackers.writeJSONString(resp.getWriter());
    }

    private void stopReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws SocketException, IOException {
        RaceDefinition race = getRaceDefinition(req);
        if (race == null) {
            resp.getWriter().println("No such race");
        } else {
            service.stopTrackingWind(getEvent(req), race);
        }
    }

    private RaceDefinition getRaceDefinition(HttpServletRequest req) {
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

    private void startReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RaceDefinition race = getRaceDefinition(req);
        if (race == null) {
            resp.getWriter().println("Race not found");
        } else {
            int port = Integer.valueOf(req.getParameter(PARAM_NAME_PORT));
            service.startTrackingWind(getEvent(req), race, port);
        }
    }

    private void stopEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException,
            InterruptedException {
        Event event = getEvent(req);
        if (event != null) {
            service.stopTracking(event);
        } else {
            resp.getWriter().println("Event not found");
        }
    }

    private Event getEvent(HttpServletRequest req) {
        Event event = service.getEventByName(req.getParameter(PARAM_NAME_EVENTNAME));
        return event;
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
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put("name", event.getName());
            jsonEvent.put("boatclass", event.getBoatClass().getName());
            JSONArray jsonCompetitors = new JSONArray();
            for (Competitor competitor : event.getCompetitors()) {
                JSONObject jsonCompetitor = new JSONObject();
                jsonCompetitor.put("name", competitor.getName());
                jsonCompetitor.put("nationality", competitor.getTeam().getNationality().getThreeLetterAcronym());
                JSONArray jsonTeam = new JSONArray();
                for (Person sailor : competitor.getTeam().getSailors()) {
                    JSONObject jsonSailor = new JSONObject();
                    jsonSailor.put("name", sailor.getName());
                    jsonSailor.put("description", sailor.getDescription()==null?"":sailor.getDescription());
                    jsonTeam.add(jsonSailor);
                }
                jsonCompetitor.put("team", jsonTeam);
                jsonCompetitors.add(jsonCompetitor);
            }
            jsonEvent.put("competitors", jsonCompetitors);
            JSONArray jsonRaces = new JSONArray();
            for (RaceDefinition race : event.getAllRaces()) {
                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", race.getName());
                jsonRace.put("boatclass", race.getBoatClass()==null?"":race.getBoatClass().getName());
                JSONArray jsonLegs = new JSONArray();
                for (Leg leg : race.getCourse().getLegs()) {
                    JSONObject jsonLeg = new JSONObject();
                    jsonLeg.put("start", leg.getFrom().getName());
                    jsonLeg.put("end", leg.getTo().getName());
                    jsonLegs.add(jsonLeg);
                }
                jsonRace.put("legs", jsonLegs);
                jsonRaces.add(jsonRace);
            }
            jsonEvent.put("races", jsonRaces);
            eventList.add(jsonEvent);
        }
        eventList.writeJSONString(resp.getWriter());
    }
}
