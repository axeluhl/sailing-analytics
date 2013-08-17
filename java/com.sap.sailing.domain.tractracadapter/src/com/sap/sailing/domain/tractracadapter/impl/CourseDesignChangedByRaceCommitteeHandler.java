package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;

public class CourseDesignChangedByRaceCommitteeHandler extends UpdateHandler implements CourseDesignChangedListener {
    
    private final static String ACTION = "update_course";
    
    private final static Logger logger = Logger.getLogger(CourseDesignChangedByRaceCommitteeHandler.class.getName());
    private JsonSerializer<CourseBase> courseSerializer;
    
    public CourseDesignChangedByRaceCommitteeHandler(URI updateURI, String tracTracUsername, String tracTracPassword, Serializable tracTracEventId, Serializable raceId) {
        super(updateURI, ACTION, tracTracUsername, tracTracPassword, tracTracEventId, raceId);
        this.courseSerializer = new CourseJsonSerializer(
                new CourseBaseJsonSerializer(
                        new WaypointJsonSerializer(
                                new ControlPointJsonSerializer(
                                        new MarkJsonSerializer(), 
                                        new GateJsonSerializer(new MarkJsonSerializer())))));
    }

    @Override
    public void courseDesignChanged(CourseBase newCourseDesign) throws MalformedURLException, IOException {
        if (!isActive()) {
            return;
        }
        
        JSONObject serializedCourseDesign = courseSerializer.serialize(newCourseDesign);
        String payload = serializedCourseDesign.toJSONString();
        URL currentCourseDesignURL = buildUpdateURL();
        logger.info("Using " + currentCourseDesignURL.toString() + " for the course update!");
        logger.info("Payload is " + payload);
        HttpURLConnection connection = (HttpURLConnection) currentCourseDesignURL.openConnection();
        try {
            setConnectionProperties(connection, payload);
            sendWithPayload(connection, payload);
            try {
                checkAndLogUpdateResponse(connection);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            } else {
                logger.severe("Connection to TracTrac Course Update URL " + currentCourseDesignURL.toString() + " could not be established");
            }
        }
    }
}
