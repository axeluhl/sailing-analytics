package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;

public class MarksJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 4510175441769759252L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        JSONArray result = new JSONArray();
        final Iterable<Mark> marks = raceColumn.getAvailableMarks(fleet);
        MarkJsonSerializer serializer = new MarkJsonSerializer();
        for (Mark mark : marks) {
            result.add(serializer.serialize(mark));
        }
        setJsonResponseHeader(response);
        result.writeJSONString(response.getWriter());
    }
}
