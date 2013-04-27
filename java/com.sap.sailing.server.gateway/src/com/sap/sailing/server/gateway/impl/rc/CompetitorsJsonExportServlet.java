package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.CompetitorJsonSerializer;

public class CompetitorsJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 4510175441769759252L;

    public static final String PARAMS_LEADERBOARD_NAME = "leaderboard";
    public static final String PARAMS_RACE_COLUMN_NAME = "raceColumn";
    public static final String PARAMS_RACE_FLEET_NAME = "fleet";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String leaderboardName = request.getParameter(PARAMS_LEADERBOARD_NAME);
        if (leaderboardName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", PARAMS_LEADERBOARD_NAME));
            return;
        }

        String raceColumnName = request.getParameter(PARAMS_RACE_COLUMN_NAME);
        if (raceColumnName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", PARAMS_RACE_COLUMN_NAME));
            return;
        }
        
        String fleetName = request.getParameter(PARAMS_RACE_FLEET_NAME);
        if (fleetName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", PARAMS_RACE_FLEET_NAME));
            return;
        }
        
        RacingEventService service = getService();

        Leaderboard leaderboard = service.getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such leaderboard found.");
            return;
        }

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such race column found.");
            return;
        }
        
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such fleet found.");
            return;
        }

        TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
       
        JSONArray result = new JSONArray();
        
        if (trackedRace != null) {
            CompetitorJsonSerializer serializer = new CompetitorJsonSerializer();

            for (Competitor competitor : raceColumn.getRaceDefinition(fleet).getCompetitors()) {
                result.add(serializer.serialize(competitor));
            }
        }
        
        result.writeJSONString(response.getWriter());
        response.setContentType("application/json");
    }
}
