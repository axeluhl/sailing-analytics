package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;

public class AddEntryToRaceLogJsonPostServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 7704668926551060433L;

    public static final String PARAMS_LEADERBOARD_NAME = "leaderboard";
    public static final String PARAMS_RACE_COLUMN_NAME = "raceColumn";
    public static final String PARAMS_RACE_FLEET_NAME = "fleet";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

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

        JsonDeserializer<RaceLogEvent> deserializer = RaceLogEventDeserializer.create(DomainFactory.INSTANCE);

        try {
            Object requestBody = JSONValue.parseWithException(request.getReader());
            JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);

            RaceLogEvent logEvent = deserializer.deserialize(requestObject);
            raceColumn.getRaceLog(fleet).add(logEvent);

        } catch (ParseException pe) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Invalid JSON in request body:\n%s", pe));
            return;
        }

    }

}
