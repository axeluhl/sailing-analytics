package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceGroupJsonSerializer extends ExtendableJsonSerializer<RaceGroup> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COURSE_AREA = "courseArea";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    public static final String FIELD_DEFAULT_RACING_PROCEDURE = "defaultProcedure";
    public static final String FIELD_DEFAULT_COURSE_DESIGNER = "defaultCourseDesigner";
    public static final String FIELD_RACING_PROCEDURES_CONFIGURATION = "proceduresConfiguration";

    private JsonSerializer<BoatClass> boatClassSerializer;
    private JsonSerializer<CourseArea> courseAreaSerializer;

    public RaceGroupJsonSerializer(
            JsonSerializer<BoatClass> boatClassSerializer,
            JsonSerializer<CourseArea> courseAreaSerializer,
            ExtensionJsonSerializer<RaceGroup, ?> extensionSerializer) {
        super(extensionSerializer);
        this.courseAreaSerializer = courseAreaSerializer;
        this.boatClassSerializer = boatClassSerializer;
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
        
        if (object.getDefaultRacingProcedureType() != null) {
            result.put(FIELD_DEFAULT_RACING_PROCEDURE, object.getDefaultRacingProcedureType().name());
        }
        
        if (object.getDefaultCourseDesignerMode() != null) {
            result.put(FIELD_DEFAULT_COURSE_DESIGNER, object.getDefaultCourseDesignerMode().name());
        }
        
        if (object.getRacingProceduresConfiguration() != null) {
            result.put(FIELD_RACING_PROCEDURES_CONFIGURATION, object.getRacingProceduresConfiguration());
        }

        return result;
    }


}
