package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
import com.sap.sailing.domain.racelog.RaceLogServletConstants;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;

public class AddEntryToRaceLogJsonPostServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 7704668926551060433L;

    private final static Logger logger = Logger.getLogger(AddEntryToRaceLogJsonPostServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        String clientUuidAsString = request.getParameter(RaceLogServletConstants.PARAMS_CLIENT_UUID);
        final UUID clientUuid;
        if (clientUuidAsString == null) {
            clientUuid = null;
        } else {
            clientUuid = UUID.fromString(clientUuidAsString);
        }

        String leaderboardName = request.getParameter(RaceLogServletConstants.PARAMS_LEADERBOARD_NAME);
        if (leaderboardName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", RaceLogServletConstants.PARAMS_LEADERBOARD_NAME));
            return;
        }

        String raceColumnName = request.getParameter(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME);
        if (raceColumnName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME));
            return;
        }

        String fleetName = request.getParameter(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME);
        if (fleetName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", RaceLogServletConstants.PARAMS_RACE_FLEET_NAME));
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
            logger.fine("Post issued for " + leaderboardName + ", " + raceColumnName + ", " + fleetName + " to add a race log event");
            Object requestBody = JSONValue.parseWithException(request.getReader());
            JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.fine("JSON requestObject is: " + requestObject.toString());
            RaceLogEvent logEvent = deserializer.deserialize(requestObject);
            logger.fine("JSON is deserialized to a RaceLogEvent");
            Iterable<RaceLogEvent> eventsToSendBackToClient = raceColumn.getRaceLog(fleet).add(logEvent, clientUuid);
            JsonSerializer<RaceLogEvent> serializer = RaceLogEventSerializer.create(new CompetitorJsonSerializer());
            ServletOutputStream outputStream = response.getOutputStream();
            boolean first = true;
            outputStream.write('[');
            for (RaceLogEvent eventToSendBackToClient : eventsToSendBackToClient) {
                if (first) {
                    first = false;
                } else {
                    outputStream.write(',');
                }
                outputStream.write(serializer.serialize(eventToSendBackToClient).toJSONString().getBytes());
            }
            outputStream.write(']');
        } catch (ParseException pe) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Invalid JSON in request body:\n%s", pe));
            logger.warning(String.format("Exception while parsing post request:\n%s", pe.toString()));
            return;
        }

    }

}
