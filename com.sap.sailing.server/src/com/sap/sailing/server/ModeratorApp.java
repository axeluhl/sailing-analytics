package com.sap.sailing.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;

public class ModeratorApp extends Servlet {
    private static final long serialVersionUID = 1333207389294903999L;

    private static final String ACTION_NAME_LIST_EVENTS = "listevents";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter(PARAM_ACTION);
            if (action != null) {
                if (ACTION_NAME_LIST_EVENTS.equals(action)) {
                    listEvents(resp);
                }
            } else {
                resp.getWriter().println("Hello moderator!");
            }
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }


    private void listEvents(HttpServletResponse resp) throws IOException {
        JSONArray eventList = new JSONArray();
        for (Event event : getService().getAllEvents()) {
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
