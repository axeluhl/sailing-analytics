package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceGroupJsonSerializer extends ExtendableJsonSerializer<RaceGroup> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COURSE_AREA = "courseArea";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    public static final String FIELD_REGATTA_CONFIGURATION = "procedures";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE = "canBoatsOfCompetitorsChangePerRace";

    private final JsonSerializer<BoatClass> boatClassSerializer;
    private final JsonSerializer<CourseArea> courseAreaSerializer;
    private final JsonSerializer<RegattaConfiguration> configurationSerializer;

    public RaceGroupJsonSerializer(
            JsonSerializer<BoatClass> boatClassSerializer,
            JsonSerializer<CourseArea> courseAreaSerializer,
            JsonSerializer<RegattaConfiguration> configurationSerializer,
            ExtensionJsonSerializer<RaceGroup, ?> extensionSerializer) {
        super(extensionSerializer);
        this.courseAreaSerializer = courseAreaSerializer;
        this.boatClassSerializer = boatClassSerializer;
        this.configurationSerializer = configurationSerializer;
    }

    @Override
    protected JSONObject serializeFields(RaceGroup object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        if (object.getDefaultCourseArea() != null) {
            result.put(FIELD_COURSE_AREA, courseAreaSerializer.serialize(object.getDefaultCourseArea()));
        }
        if (object.getBoatClass() != null) {
            result.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(object.getBoatClass()));
        }
        if (object.getRegattaConfiguration() != null) {
            result.put(FIELD_REGATTA_CONFIGURATION, 
                    configurationSerializer.serialize(object.getRegattaConfiguration()));
        }
        result.put(FIELD_DISPLAY_NAME, object.getDisplayName());
        result.put(FIELD_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE, object.canBoatsOfCompetitorsChangePerRace());
        return result;
    }


}
