package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.jaxrs.api.EventRaceStatesSerializer;
import com.sap.sse.common.Util.Pair;

public class EventRaceStatesJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -4820965681871902242L;
    
    private final static Logger logger = Logger.getLogger(EventRaceStatesJsonGetServlet.class.getName());

    private static final String PARAM_NAME_EVENTID = "eventId";
    private static final String PARAM_NAME_FILTER_BY_LEADERBOARD = "filterByLeaderboard";
    private static final String PARAM_NAME_FILTER_BY_COURSEAREA = "filterByCourseArea";
    private static final String PARAM_NAME_FILTER_BY_DAYOFFSET = "filterByDayOffset";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String eventIdParam = request.getParameter(PARAM_NAME_EVENTID);
        String filterByCourseAreaParam = request.getParameter(PARAM_NAME_FILTER_BY_COURSEAREA);
        String filterByLeaderboardParam = request.getParameter(PARAM_NAME_FILTER_BY_LEADERBOARD);
        String filterByDayOffsetParam = request.getParameter(PARAM_NAME_FILTER_BY_DAYOFFSET);

        if (eventIdParam == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "You need to specify a event id using the "+
                    PARAM_NAME_EVENTID + " parameter");
        } else {
            Event event = getService().getEvent(convertIdentifierStringToUuid(eventIdParam));
            if (event == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event "+eventIdParam+" not found");
            } else {
                EventRaceStatesSerializer eventRaceStatesSerializer = new EventRaceStatesSerializer(filterByCourseAreaParam,
                        filterByLeaderboardParam, filterByDayOffsetParam);
                JSONObject result = eventRaceStatesSerializer.serialize(new Pair<Event, Iterable<Leaderboard>>(event, getService().getLeaderboards().values()));
                setJsonResponseHeader(response);
                result.writeJSONString(response.getWriter());
            }
        }
    }

    /**
     * Tries to convert the given identifier to a UUID. When the identifier is not a UUID, null is returned.
     * @param identifierToConvert the identifier as a String
     * @return a UUID when the given identifier is a UUID, null otherwise
     */
    private UUID convertIdentifierStringToUuid(String identifierToConvert) {
        UUID result = null;
        if (identifierToConvert != null) {
            try {
                result = UUID.fromString(identifierToConvert);
            } catch (IllegalArgumentException iae) {
                logger.warning("The identifier " + identifierToConvert + " could not be converted to a UUID");
                //in this case null is returned
            }
        }
        return result;
    }
}
