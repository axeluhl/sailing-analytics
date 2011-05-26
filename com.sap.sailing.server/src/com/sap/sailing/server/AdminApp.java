package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.Util.Pair;

public class AdminApp extends Servlet {
    private static final long serialVersionUID = -6849138354941569249L;
    
    private static final String ACTION_NAME_ADD_EVENT = "addevent";

    private static final String ACTION_NAME_STOP_EVENT = "stopevent";
    
    private static final String ACTION_NAME_LIST_WINDTRACKERS = "listwindtrackers";
    
    private static final String ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "receiveexpeditionwind";

    private static final String ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "stopreceivingexpeditionwind";

    private static final String PARAM_NAME_PORT = "port";

    public AdminApp() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter(PARAM_ACTION);
            if (action != null) {
                if (ACTION_NAME_ADD_EVENT.equals(action)) {
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
        for (Pair<Event, RaceDefinition> eventAndRace : getService().getWindTrackedRaces()) {
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
            resp.sendError(500, "Race not found");
        } else {
            getService().stopTrackingWind(getEvent(req), race);
        }
    }

    private void startReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RaceDefinition race = getRaceDefinition(req);
        if (race == null) {
            resp.sendError(500, "Race not found");
        } else {
            int port = Integer.valueOf(req.getParameter(PARAM_NAME_PORT));
            getService().startTrackingWind(getEvent(req), race, port);
        }
    }

    private void stopEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException,
            InterruptedException {
        Event event = getEvent(req);
        if (event != null) {
            getService().stopTracking(event);
        } else {
            resp.sendError(500, "Event not found");
        }
    }

    private void addEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException,
            URISyntaxException, FileNotFoundException {
        URL paramURL = new URL(req.getParameter("paramURL"));
        URI liveURI = new URI(req.getParameter("liveURI"));
        URI storedURI = new URI(req.getParameter("storedURI"));
        getService().addEvent(paramURL, liveURI, storedURI);
    }

}
