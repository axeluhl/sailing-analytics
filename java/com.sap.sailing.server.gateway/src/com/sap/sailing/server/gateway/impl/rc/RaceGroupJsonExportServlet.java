package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorIdJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.RaceCellJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.RaceGroupJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.RaceRowJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.RaceRowsOfSeriesWithRowsSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.SeriesWithRowsJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.SeriesWithRowsOfRaceGroupSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogSerializer;

public class RaceGroupJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 4510175441769759252L;

    private static final String PARAM_COURSE_AREA_FILTER = "courseArea";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String courseAreaFilter = request.getParameter(PARAM_COURSE_AREA_FILTER);
        if (courseAreaFilter == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Need to set a course area filter.");
            return;
        }
        UUID courseAreaId = toUUID(courseAreaFilter);
        if (courseAreaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course are filter must be valid UUID.");
            return;
        }

        CourseArea filterCourseArea = getService().getCourseArea(courseAreaId);
        if (filterCourseArea == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No course area found with given UUID.");
            return;
        }

        JsonSerializer<RaceGroup> serializer = createSerializer();
        JSONArray result = new JSONArray();

        RaceGroupFactory raceGroupFactory = new RaceGroupFactory();
        for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
            if (filterCourseArea.equals(leaderboard.getDefaultCourseArea())) {
                RaceGroup raceGroup = raceGroupFactory.convert(leaderboard);
                result.add(serializer.serialize(raceGroup));
            }
        }

        result.writeJSONString(response.getWriter());
    }

    private UUID toUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    private static JsonSerializer<RaceGroup> createSerializer() {
        return new RaceGroupJsonSerializer(new BoatClassJsonSerializer(), new CourseAreaJsonSerializer(),
                new SeriesWithRowsOfRaceGroupSerializer(new SeriesWithRowsJsonSerializer(
                        new RaceRowsOfSeriesWithRowsSerializer(new RaceRowJsonSerializer(new FleetJsonSerializer(
                                new ColorJsonSerializer()), new RaceCellJsonSerializer(createRaceLogSerializer()))))));

    }

    private static JsonSerializer<RaceLog> createRaceLogSerializer() {
        return new RaceLogSerializer(RaceLogEventSerializer.create(new CompetitorIdJsonSerializer()));
    }

}
