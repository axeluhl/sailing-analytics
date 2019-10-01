package com.sap.sailing.server.gateway.jaxrs.api;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseConfigurationJsonSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/courseconfiguration")
public class CourseConfigurationResource extends AbstractSailingServerResource {

    private final JsonSerializer<CourseConfiguration> courseConfigurationJsonSerializer;
    private final JsonSerializer<CourseBase> courseJsonSerializer;

    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_OPTIONAL_IMAGE_URL = "optionalImageUrl";

    public CourseConfigurationResource() {
        courseConfigurationJsonSerializer = new CourseConfigurationJsonSerializer();
        courseJsonSerializer = new CourseJsonSerializer(new CourseBaseJsonSerializer(
                new WaypointJsonSerializer(new ControlPointJsonSerializer(new MarkJsonSerializer(),
                        new GateJsonSerializer(new MarkJsonSerializer())))));
    }

    private JsonDeserializer<CourseConfiguration> getCourseConfigurationDeserializer(final Regatta regatta) {
        return new CourseConfigurationJsonDeserializer(this.getSharedSailingData(), regatta);
    }

    private Response getBadRegattaErrorResponse(String regattaName) {
        return Response.status(Status.NOT_FOUND)
                .entity("Could not find a regatta with name '" + StringEscapeUtils.escapeHtml(regattaName) + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getBadRaceErrorResponse(String regattaName, String raceColumn, String fleet) {
        return Response.status(Status.NOT_FOUND)
                .entity("Could not find a race with race column '" + StringEscapeUtils.escapeHtml(raceColumn)
                        + "' and fleet '" + fleet
                        + "' for regatta with name '" + StringEscapeUtils.escapeHtml(regattaName) + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getBadCourseTemplateErrorResponse(String courseTemplateId) {
        return Response.status(Status.NOT_FOUND).entity(
                "Could not find a CourseTemplate with ID '" + StringEscapeUtils.escapeHtml(courseTemplateId) + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getBadCourseConfigurationValidationErrorResponse(String errorText) {
        return Response.status(Status.BAD_REQUEST).entity(StringEscapeUtils.escapeHtml(errorText) + ".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("getFromCourse/{regattaName}/{raceColumn}/{fleet}")
    public Response createCourseConfigurationFromCourse(@PathParam("regattaName") String regattaName,
            @PathParam("raceColumn") String raceColumn, @PathParam("fleet") String fleet,
            @QueryParam("tag") List<String> tags) throws Exception {

        if (regattaName == null || raceColumn == null || fleet == null) {
            return getBadCourseConfigurationValidationErrorResponse(
                    "Course configuration is required to have a regatta name and a race name");
        }

        final Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            return getBadRegattaErrorResponse(regattaName);
        }

        final RaceColumn raceColumnByName = findRaceColumnByName(regatta, raceColumn);
        final Fleet fleetByName = findFleetByName(raceColumnByName, fleet);

        if (raceColumnByName == null || fleetByName == null) {
            return getBadRaceErrorResponse(regattaName, raceColumn, fleet);
        }
        
        final RaceDefinition raceDefinition = raceColumnByName.getRaceDefinition(fleetByName);
        final CourseBase courseBase;
        if (raceDefinition != null) {
            courseBase = raceDefinition.getCourse();
        } else {
            final LastPublishedCourseDesignFinder courseDesginFinder = new LastPublishedCourseDesignFinder(
                    regatta.getRacelog(raceColumn, fleet), /* onlyCoursesWithValidWaypointList */ true);
            courseBase = courseDesginFinder.analyze();
        }

        if (courseBase == null) {
            return Response.status(Status.NOT_FOUND).entity("No course found for given race.").build();
        }

        final CourseConfiguration courseConfiguration = getService().getCourseAndMarkConfigurationFactory()
                .createCourseConfigurationFromCourse(courseBase, regatta, tags);

        return Response.ok(courseConfigurationJsonSerializer.serialize(courseConfiguration).toJSONString()).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("getFromCourseTemplate/{courseTemplateId}")
    public Response createCourseConfigurationFromCourseTemplate(@PathParam("courseTemplateId") String courseTemplateId,
            @QueryParam("regattaName") String regattaName,
            @QueryParam("tag") List<String> tags) {
        final CourseTemplate courseTemplate = this.getSharedSailingData()
                .getCourseTemplateById(UUID.fromString(courseTemplateId));
        if (courseTemplate == null) {
            return getBadCourseTemplateErrorResponse(courseTemplateId);
        }
        Regatta regatta = null;
        if (regattaName != null) {
            regatta = findRegattaByName(regattaName);
            if (regatta == null) {
                return getBadRegattaErrorResponse(regattaName);
            }
        }

        final CourseConfiguration courseConfiguration = getService().getCourseAndMarkConfigurationFactory()
                .createCourseConfigurationFromTemplate(courseTemplate, regatta, tags);

        String jsonString = courseConfigurationJsonSerializer.serialize(courseConfiguration).toJSONString();
        return Response.ok(jsonString).build();

    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("createCourseTemplate")
    public Response createCourseTemplate(@QueryParam("regattaName") String regattaName, String json) throws Exception {
        if (json == null || json.isEmpty()) {
            return getBadCourseConfigurationValidationErrorResponse(
                    "Course configuration is required to be given as json object");
        }
        Regatta regatta = null;
        if (regattaName != null) {
            regatta = findRegattaByName(regattaName);
            if (regatta == null) {
                return getBadRegattaErrorResponse(regattaName);
            }
        }

        final Object parsedObject = new JSONParser().parse(json);
        if (parsedObject == null || !(parsedObject instanceof JSONObject)) {
            getBadCourseConfigurationValidationErrorResponse(
                    "Course configuration is required to be given as json object");
        }
        final CourseConfiguration courseConfiguration = getCourseConfigurationDeserializer(regatta)
                .deserialize((JSONObject) parsedObject);

        final Iterable<String> tags = Arrays
                .asList(ArrayUtils.nullToEmpty((String[]) ((JSONObject) parsedObject).get(FIELD_TAGS)));
        final String optionalImageURLStr = (String) ((JSONObject) parsedObject).get(FIELD_OPTIONAL_IMAGE_URL);
        
        final URL optionalImageURL = optionalImageURLStr != null && !optionalImageURLStr.isEmpty()
                ? new URL(optionalImageURLStr)
                : null;

        final CourseConfiguration courseTemplate = getService().getCourseAndMarkConfigurationFactory()
                .createCourseTemplateAndUpdatedConfiguration(courseConfiguration, tags, optionalImageURL);
        final String jsonString = courseConfigurationJsonSerializer.serialize(courseTemplate).toJSONString();
        return Response.ok(jsonString).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("createCourse/{regattaName}/{raceColumn}/{fleet}")
    public Response createCourse(@PathParam("regattaName") String regattaName,
            @PathParam("raceColumn") String raceColumn, @PathParam("fleet") String fleet,
            String json) throws Exception {
        if (json == null || json.isEmpty()) {
            return getBadCourseConfigurationValidationErrorResponse(
                    "Course configuration is required to be given as json object");
        }

        if (regattaName == null || raceColumn == null || fleet == null) {
            return getBadCourseConfigurationValidationErrorResponse(
                    "Course configuration is required to have a regatta name and a race name");
        }

        final Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            return getBadRegattaErrorResponse(regattaName);
        }

        final RaceColumn raceColumnByName = findRaceColumnByName(regatta, raceColumn);
        final Fleet fleetByName = findFleetByName(raceColumnByName, fleet);

        if (raceColumnByName == null || fleetByName == null) {
            return getBadRaceErrorResponse(regattaName, raceColumn, fleet);
        }

        final Object parsedObject = new JSONParser().parse(json);
        if (parsedObject == null || !(parsedObject instanceof JSONObject)) {
            getBadCourseConfigurationValidationErrorResponse(
                    "Course configuration is required to be given as json object");
        }
        final CourseConfiguration courseConfiguration = getCourseConfigurationDeserializer(regatta)
                .deserialize((JSONObject) parsedObject);

        final TimePoint timestampForLogEntries = MillisecondsTimePoint.now();
        final CourseBase course = getService().getCourseAndMarkConfigurationFactory()
                .createCourseFromConfigurationAndDefineMarksAsNeeded(regatta, courseConfiguration,
                        timestampForLogEntries, getService().getServerAuthor());
        final RaceLog raceLog = raceColumnByName.getRaceLog(fleetByName);
        raceLog.add(new RaceLogCourseDesignChangedEventImpl(timestampForLogEntries, getService().getServerAuthor(),
                raceLog.getCurrentPassId(), course, CourseDesignerMode.BY_MARKS));

        return Response.ok(courseJsonSerializer.serialize(course).toJSONString()).build();
    }
}
