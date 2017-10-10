package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.util.HttpUrlConnectionHelper;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.route.IControl;

public class TracTracCourseDesignUpdateHandler extends UpdateHandler implements CourseDesignChangedListener {
    
    private final static String ACTION = "update_course";
    
    private final static Logger logger = Logger.getLogger(TracTracCourseDesignUpdateHandler.class.getName());
    private final JsonSerializer<CourseBase> courseSerializer;
    private final IRace tractracRace;
    private final DomainFactory domainFactory;
    
    public TracTracCourseDesignUpdateHandler(URI updateURI, String tracTracUsername, String tracTracPassword, Serializable tracTracEventId, Serializable raceId, IRace tractracRace, DomainFactory domainFactory) {
        super(updateURI, ACTION, tracTracUsername, tracTracPassword, tracTracEventId, raceId);
        this.domainFactory = domainFactory;
        this.tractracRace = tractracRace;
        this.courseSerializer = new CourseJsonSerializer(
                new CourseBaseJsonSerializer(
                        new WaypointJsonSerializer(
                                new ControlPointJsonSerializer(
                                        new MarkJsonSerializer(), 
                                        new GateJsonSerializer(new MarkJsonSerializer())))));
    }

    @Override
    public void courseDesignChanged(final CourseBase newCourseDesign) throws MalformedURLException, IOException {
        if (!isActive()) {
            logger.info("Not sending course update to TracTrac because no URL has been provided.");
            return;
        }
        final CourseBase newCourseDesignWithExistingControlPoints = replaceControlPointsByMatchingExistingControlPoints(newCourseDesign);
        JSONObject serializedCourseDesign = courseSerializer.serialize(newCourseDesignWithExistingControlPoints);
        String payload = serializedCourseDesign.toJSONString();
        URL currentCourseDesignURL = buildUpdateURL();
        logger.info("Using " + currentCourseDesignURL.toString() + " for the course update!");
        logger.info("Payload is " + payload);
        HttpURLConnection connection = HttpUrlConnectionHelper.redirectConnection(currentCourseDesignURL);
        try {
            setConnectionPropertiesAndSendWithPayload(connection, payload);
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
    
    private CourseBase replaceControlPointsByMatchingExistingControlPoints(CourseBase courseDesign) {
        final Iterable<IControl> candidates = domainFactory.getControlsForCourseArea(tractracRace.getEvent(), tractracRace.getCourseArea());
        final CourseBase result = new CourseDataImpl(courseDesign.getName());
        int zeroBasedPosition = 0;
        boolean changed = false;
        for (final Waypoint waypoint : courseDesign.getWaypoints()) {
            if (Util.size(waypoint.getMarks()) > 1) {
                final Iterator<Mark> markIter = waypoint.getMarks().iterator();
                final Mark first = markIter.next();
                final Mark second = markIter.next();
                final ControlPoint existingControlPoint = domainFactory.getExistingControlWithTwoMarks(candidates,
                        first, second);
                if (existingControlPoint == null) {
                    result.addWaypoint(zeroBasedPosition++, waypoint);
                } else {
                    result.addWaypoint(zeroBasedPosition++, domainFactory.getBaseDomainFactory()
                            .createWaypoint(existingControlPoint, waypoint.getPassingInstructions()));
                    changed = true;
                }
            } else {
                result.addWaypoint(zeroBasedPosition++, waypoint);
            }
        }
        return changed ? result : courseDesign;
    }
}
