package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorAndBoatJsonSerializer;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.PublicReadableActions;

public class CompetitorsAndBoatsJsonExportServlet extends AbstractJsonHttpServlet {
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
        SecurityUtils.getSubject().checkPermission(leaderboard.getIdentifier().getStringPermission(DefaultActions.READ));
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
        final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
        if (trackedRace != null) {
            SecurityUtils.getSubject().checkPermission(trackedRace.getIdentifier().getStringPermission(DefaultActions.READ));
        }
        for (Competitor competitor : leaderboard.getCompetitors(raceColumn, fleet)) {
            Boat boat = leaderboard.getBoatOfCompetitor(competitor, raceColumn, fleet);
            if (getSecurityService().hasCurrentUserExplicitPermissions(competitor, PublicReadableActions.READ_PUBLIC) &&
                    getSecurityService().hasCurrentUserExplicitPermissions(boat, PublicReadableActions.READ_PUBLIC)) {
                // suppress those competitors / boats from the result for which the current user does not even have READ_PUBLIC permissions
                CompetitorAndBoatJsonSerializer competitorsAndBoatsSerializer = CompetitorAndBoatJsonSerializer.create(
                        getSecurityService().hasCurrentUserReadPermission(competitor));
                JSONObject serializeCompetitorAndBoat = competitorsAndBoatsSerializer.serialize(new Pair<Competitor, Boat>(competitor, boat));
                result.add(serializeCompetitorAndBoat);
            }
        }
        setJsonResponseHeader(response);
        result.writeJSONString(response.getWriter());
    }
}
