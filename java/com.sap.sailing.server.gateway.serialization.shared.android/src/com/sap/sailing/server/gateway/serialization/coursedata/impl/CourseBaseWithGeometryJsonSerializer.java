package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import java.util.Map;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseWithGeometryJsonSerializer.CourseGeometry;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Util.Pair;

public class CourseBaseWithGeometryJsonSerializer implements JsonSerializer<Pair<CourseBase, CourseGeometry>> {
    public static final String FIELD_TOTAL_DISTANCE_IN_METERS = "totalDistanceInMeters";
    public static final String FIELD_LEG_DISTANCE_IN_METERS = "legDistanceInMeters";
    public static final String FIELD_LEG_BEARING_TRUE_DEGREES = "legBearingTrueDegrees";

    public static class CourseGeometry {
        private final Distance totalDistance;
        private final Map<Leg, Distance> legDistances;
        private final Map<Leg, Bearing> legBearings;
        
        public CourseGeometry(Distance totalDistance, Map<Leg, Distance> legDistances, Map<Leg, Bearing> legBearings) {
            super();
            this.totalDistance = totalDistance;
            this.legDistances = legDistances;
            this.legBearings = legBearings;
        }

        public Distance getTotalDistance() {
            return totalDistance;
        }

        public Map<Leg, Distance> getLegDistances() {
            return legDistances;
        }

        public Map<Leg, Bearing> getLegBearings() {
            return legBearings;
        }
    }

    private JsonSerializer<Waypoint> waypointSerializer;

    public CourseBaseWithGeometryJsonSerializer(WaypointJsonSerializer waypointSerializer) {
        this.waypointSerializer = waypointSerializer;
    }

    @Override
    public JSONObject serialize(final Pair<CourseBase, CourseGeometry> courseAndOptionalGeometry) {
        final CourseBase course = courseAndOptionalGeometry.getA();
        final CourseGeometry geometry = courseAndOptionalGeometry.getB();
        final JSONObject result = new CourseBaseJsonSerializer(waypointSerializer) {
            @Override
            protected JSONObject serializeWaypoint(Waypoint waypoint) {
                final JSONObject result = super.serializeWaypoint(waypoint);
                if (geometry != null) {
                    final int waypointIndex = course.getIndexOfWaypoint(waypoint);
                    if (waypointIndex >= 0 && course.getLegs().size() > waypointIndex) {
                        final Leg leg = course.getLegs().get(waypointIndex);
                        final Distance distance = geometry.getLegDistances().get(leg);
                        result.put(FIELD_LEG_DISTANCE_IN_METERS, distance==null?null:distance.getMeters());
                        final Bearing bearing = geometry.getLegBearings().get(leg);
                        result.put(FIELD_LEG_BEARING_TRUE_DEGREES, bearing==null?null:bearing.getDegrees());
                    }
                }
                return result;
            }
        }.serialize(course);
        if (courseAndOptionalGeometry.getB() != null) {
            final Distance totalDistance = courseAndOptionalGeometry.getB().getTotalDistance();
            result.put(FIELD_TOTAL_DISTANCE_IN_METERS, totalDistance==null?null:totalDistance.getMeters());
        }
        return result;
    }
}
