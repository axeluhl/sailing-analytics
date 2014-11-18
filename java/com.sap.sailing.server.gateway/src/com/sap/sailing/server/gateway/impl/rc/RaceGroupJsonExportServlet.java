package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaConfigurationJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceCellJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceGroupJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceRowJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceRowsOfSeriesWithRowsSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.SeriesWithRowsJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.SeriesWithRowsOfRaceGroupSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogSerializer;

public class RaceGroupJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 4510175441769759252L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String courseAreaFilter = request.getParameter(RaceLogServletConstants.PARAM_COURSE_AREA_FILTER);
        if (courseAreaFilter == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Need to set a course area filter.");
            return;
        }
        UUID courseAreaId = toUUID(courseAreaFilter);
        if (courseAreaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course area filter must be valid UUID.");
            return;
        }

        CourseArea filterCourseArea = getService().getCourseArea(courseAreaId);
        if (filterCourseArea == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No course area found with given UUID.");
            return;
        }
        String clientUuidAsString = request.getParameter(RaceLogServletConstants.PARAMS_CLIENT_UUID);
        final UUID clientUuid;
        if (clientUuidAsString == null) {
            clientUuid = null;
        } else {
            clientUuid = UUID.fromString(clientUuidAsString);
        }
        JsonSerializer<RaceGroup> serializer = createSerializer(clientUuid);
        JSONArray result = new JSONArray();

        RaceGroupFactory raceGroupFactory = new RaceGroupFactory();
        for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
            if (filterCourseArea.equals(leaderboard.getDefaultCourseArea())) {
                RaceGroup raceGroup = null;
                if (leaderboard instanceof RegattaLeaderboard) {
                    raceGroup = raceGroupFactory.convert((RegattaLeaderboard) leaderboard);
                } else if (leaderboard instanceof FlexibleLeaderboard) {
                    raceGroup = raceGroupFactory.convert((FlexibleLeaderboard) leaderboard);
                }
                result.add(serializer.serialize(raceGroup));
            }
        }

        result.writeJSONString(response.getWriter());
        response.setContentType("application/json");
    }

    private UUID toUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    private static JsonSerializer<RaceGroup> createSerializer(UUID clientUuid) {
        return new RaceGroupJsonSerializer(new BoatClassJsonSerializer(), new CourseAreaJsonSerializer(),
                RegattaConfigurationJsonSerializer.create(),
                new SeriesWithRowsOfRaceGroupSerializer(new SeriesWithRowsJsonSerializer(
                        new RaceRowsOfSeriesWithRowsSerializer(new RaceRowJsonSerializer(new FleetJsonSerializer(
                                new ColorJsonSerializer()), new RaceCellJsonSerializer(createRaceLogSerializer(clientUuid)))))));

    }

    /**
     * @param clientUuid
     *            used to tell the race log to which client the race log is delivered so that when the client asks later
     *            which race log events are new for that client, the race log already knows which events were already
     *            delivered
     */
    private static JsonSerializer<RaceLog> createRaceLogSerializer(UUID clientUuid) {
        return new RaceLogSerializer(RaceLogEventSerializer.create(CompetitorJsonSerializer.create()), clientUuid);
    }

}
