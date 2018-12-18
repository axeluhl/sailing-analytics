package com.sap.sailing.server.gateway.impl.rc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.PlaceHolderDeviceIdentifierJsonHandler;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.TypeBasedServiceFinder;

public class AddEntryToRaceLogJsonPostServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 7704668926551060433L;

    private final static Logger logger = Logger.getLogger(AddEntryToRaceLogJsonPostServlet.class.getName());
    
    private TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceJsonServiceFinder;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	deviceJsonServiceFinder = getServiceFinderFactory()
    			.createServiceFinder(DeviceIdentifierJsonHandler.class);
    	deviceJsonServiceFinder.setFallbackService(new PlaceHolderDeviceIdentifierJsonHandler());
    }

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
        //SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));

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
        
        logger.fine("Post issued for race log of " + leaderboardName + ", " + raceColumnName + ", " + fleetName);
        RacingEventService service = getService();

        Leaderboard leaderboard = service.getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Leaderboard "+StringEscapeUtils.escapeHtml(leaderboardName)+" not found.");
            return;
        }

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Race column "+StringEscapeUtils.escapeHtml(raceColumnName)+" not found.");
            return;
        }

        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fleet "+StringEscapeUtils.escapeHtml(fleetName)+" not found.");
            return;
        }

        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Race Log not found.");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        // TODO: we are removing line feeds here, intended?
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        reader.close();
        
        if (requestBody.length() == 0) {
            logger.fine("Client wants to receive server events");
            sendResponse(response, clientUuid, raceLog, raceLog.getEventsToDeliver(clientUuid));
        } else {
            try {
                logger.fine("Client wants to add a race log event");
                JsonDeserializer<RaceLogEvent> deserializer = RaceLogEventDeserializer.create(
                		getService().getBaseDomainFactory(), new DeviceIdentifierJsonDeserializer(deviceJsonServiceFinder));
                Object requestObject = JSONValue.parseWithException(requestBody.toString());
                JSONObject requestJsonObject = Helpers.toJSONObjectSafe(requestObject);
                logger.fine("JSON requestObject is: " + requestObject.toString());
                RaceLogEvent logEvent = null;
                try {
                    logEvent = deserializer.deserialize(requestJsonObject);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "De-Serialization failed: "+ex.getMessage(), ex);
                    throw ex;
                }
                logger.fine("JSON is deserialized to a RaceLogEvent");
                Iterable<RaceLogEvent> eventsToSendBackToClient = raceLog.add(logEvent, clientUuid);
                sendResponse(response, clientUuid, raceLog, eventsToSendBackToClient);
            } catch (ParseException pe) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        String.format("Invalid JSON in request body:\n%s", pe));
                logger.warning(String.format("Exception while parsing post request: %s", pe.toString()));
            }
        }
    }

    protected void sendResponse(HttpServletResponse response, final UUID clientUuid, RaceLog raceLog, 
            Iterable<RaceLogEvent> eventsToSendBackToClient) throws IOException {
        JsonSerializer<RaceLogEvent> serializer = RaceLogEventSerializer.create(new CompetitorJsonSerializer(),
                new DeviceIdentifierJsonSerializer(deviceJsonServiceFinder));
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
    }

}
