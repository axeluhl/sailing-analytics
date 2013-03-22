package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class RegattasJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 1333207389294903999L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONArray regattaList = new JSONArray();
        for (Regatta regatta : getService().getAllRegattas()) {
            JSONObject jsonRegatta = new JSONObject();
            jsonRegatta.put("name", regatta.getName());
            if (regatta.getBoatClass() != null) {
                jsonRegatta.put("boatclass", regatta.getBoatClass().getName());
            }
            JSONArray jsonCompetitors = new JSONArray();
            for (Competitor competitor : regatta.getCompetitors()) {
                JSONObject jsonCompetitor = new JSONObject();
                jsonCompetitor.put("name", competitor.getName());
                jsonCompetitor.put("sailID", competitor.getBoat()==null?"":competitor.getBoat().getSailID());
                jsonCompetitor.put("nationality", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
                CountryCode countryCode = competitor.getTeam().getNationality().getCountryCode();
                jsonCompetitor.put("nationalityISO2", countryCode == null ? "" : countryCode.getTwoLetterISOCode());
                jsonCompetitor.put("nationalityISO3", countryCode == null ? "" : countryCode.getThreeLetterISOCode());
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
            jsonRegatta.put("competitors", jsonCompetitors);
            JSONArray jsonRaces = new JSONArray();
            for (RaceDefinition race : regatta.getAllRaces()) {
                // don't wait for the arrival of a tracked race; just ignore it if it's not currently being tracked
                TrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
                if (trackedRace != null) {
                    JSONObject jsonRace = new JSONObject();
                    jsonRace.put("name", race.getName());
                    jsonRace.put("boatclass", race.getBoatClass() == null ? "" : race.getBoatClass().getName());
                    TimePoint start = trackedRace.getStartOfRace();
                    jsonRace.put("start", start == null ? Long.MAX_VALUE : start.asMillis());
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
            }
            jsonRegatta.put("races", jsonRaces);
            regattaList.add(jsonRegatta);
        }
        setJsonResponseHeader(resp);
        regattaList.writeJSONString(resp.getWriter());
    }
}
