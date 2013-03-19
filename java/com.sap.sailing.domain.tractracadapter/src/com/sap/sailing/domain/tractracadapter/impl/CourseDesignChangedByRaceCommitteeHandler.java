package com.sap.sailing.domain.tractracadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;

public class CourseDesignChangedByRaceCommitteeHandler implements CourseDesignChangedListener {
    
    private JsonSerializer<CourseData> courseDataSerializer;
    
    public CourseDesignChangedByRaceCommitteeHandler() {
        courseDataSerializer = new CourseDataJsonSerializer(
                new WaypointJsonSerializer(
                        new ControlPointJsonSerializer(
                                new MarkJsonSerializer(), 
                                new GateJsonSerializer(new MarkJsonSerializer()))));
    }

    @Override
    public void courseDesignChanged(CourseData newCourseDesign) {
        JSONObject serializedCourseDesign = courseDataSerializer.serialize(newCourseDesign);
        serializedCourseDesign.toJSONString();
        //TODO send serialized course Design to trac Trac
    }

}
