package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class CourseJsonExportServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = -6604797426439916738L;
    
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
        SecurityUtils.getSubject()
                .checkPermission(leaderboard.getIdentifier().getStringPermission(DefaultActions.READ));

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
        JSONObject result;
        if (trackedRace != null) {
            SecurityUtils.getSubject()
                    .checkPermission(trackedRace.getIdentifier().getStringPermission(DefaultActions.READ));
            Course course = raceColumn.getRaceDefinition(fleet).getCourse();
            CourseBaseJsonSerializer serializer = new CourseBaseJsonSerializer(
                    new WaypointJsonSerializer(
                            new ControlPointJsonSerializer(
                                    new MarkJsonSerializer(),
                                    new GateJsonSerializer(new MarkJsonSerializer()))));

            result = serializer.serialize(course);
        } else {
            result = new JSONObject();
        }
        setJsonResponseHeader(response);
        result.writeJSONString(response.getWriter());
    }
}
