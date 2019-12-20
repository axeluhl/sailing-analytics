package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseConfigurationJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.PlaceHolderDeviceIdentifierJsonHandler;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/courseconfiguration")
public class CourseConfigurationResource extends AbstractSailingServerResource {
    private static final Logger log = Logger.getLogger(CourseConfigurationResource.class.getName());
    
    private final JsonSerializer<CourseConfiguration> courseConfigurationJsonSerializer;
    private final Function<DeviceIdentifier, Position> positionResolver;
    private final DeviceIdentifierJsonDeserializer deviceIdentifierDeserializer;

    public static final String FIELD_TAGS = "tags";

    public CourseConfigurationResource() {
        final TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceJsonServiceFinder = getServiceFinderFactory()
                .createServiceFinder(DeviceIdentifierJsonHandler.class);
        deviceJsonServiceFinder.setFallbackService(new PlaceHolderDeviceIdentifierJsonHandler());
        courseConfigurationJsonSerializer = new CourseConfigurationJsonSerializer(new DeviceIdentifierJsonSerializer(deviceJsonServiceFinder));
        deviceIdentifierDeserializer = new DeviceIdentifierJsonDeserializer(deviceJsonServiceFinder);
        positionResolver = identifier -> {
            Position lastPosition = null;
            try {
                // FIXME terribly slow! Furthermore, looking up by deviceIdentifier only misses any other position sources
                final Map<DeviceIdentifier, Timed> lastFix = getService().getSensorFixStore()
                        .getLastFix(Collections.singleton(identifier));
                final Timed t = lastFix.get(identifier);
                if (t instanceof GPSFix) {
                    lastPosition = ((GPSFix) t).getPosition();
                }
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                log.log(Level.WARNING, "Could not load associated fix for device " + identifier, e);
            }
            return lastPosition;
        };
    }

    private JsonDeserializer<CourseConfiguration> getCourseConfigurationDeserializer(final Regatta regatta) {
        return new CourseConfigurationJsonDeserializer(this.getSharedSailingData(), deviceIdentifierDeserializer, regatta, positionResolver);
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
        getSecurityService().checkCurrentUserReadPermission(regatta);
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
            final ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(getService(), raceColumnByName.getRaceLog(fleetByName));
            courseBase = raceState.getCourseDesign();
        }
        // courseBase may be null in case, no course is defined for the race yet.
        // createCourseConfigurationFromCourse returns a course configuration with an empty sequence in this case.
        // Any mark already defined in the regatta will be added to the included mark configurations as an initial
        // set of marks to be used while defining a course for the regatta.
        // An additional call to get the marks defined in the regatta isn't necessary with the described behavior of this API.
        final CourseConfiguration courseConfiguration = getService().getCourseAndMarkConfigurationFactory()
                .createCourseConfigurationFromRegatta(courseBase, regatta, tags);
        final JSONObject jsonResult = courseConfigurationJsonSerializer.serialize(courseConfiguration);
        return Response.ok(jsonResult.toJSONString()).build();
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
    public Response createCourseTemplate(@QueryParam("regattaName") String regattaName,
            @QueryParam("markPropertiesGroupOwnership") String markPropertiesGroupOwnership,
            String json) throws Exception {
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
        final Optional<UserGroup> optionalUserGroupForNonDefaultMarkPropertiesOwnership = getOptionalGroupOwnership(
                markPropertiesGroupOwnership);
        final CourseConfiguration courseTemplate = getService().getCourseAndMarkConfigurationFactory()
                .createCourseTemplateAndUpdatedConfiguration(courseConfiguration, tags,
                        optionalUserGroupForNonDefaultMarkPropertiesOwnership);
        final String jsonString = courseConfigurationJsonSerializer.serialize(courseTemplate).toJSONString();
        return Response.ok(jsonString).build();
    }

    private Optional<UserGroup> getOptionalGroupOwnership(String optionalGroupName) {
        final Optional<UserGroup> optionalUserGroupForNonDefaultMarkPropertiesOwnership;
        if (optionalGroupName != null) {
            optionalUserGroupForNonDefaultMarkPropertiesOwnership = Optional.of(
                    getService().getSecurityService().getUserGroupByName(optionalGroupName));
        } else {
            optionalUserGroupForNonDefaultMarkPropertiesOwnership = Optional.empty();
        }
        return optionalUserGroupForNonDefaultMarkPropertiesOwnership;
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("createCourse/{regattaName}/{raceColumn}/{fleet}")
    public Response createCourse(@PathParam("regattaName") String regattaName,
            @QueryParam("markPropertiesGroupOwnership") String markPropertiesGroupOwnership,
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
        getSecurityService().checkCurrentUserUpdatePermission(regatta);
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
        final Optional<UserGroup> optionalUserGroupForNonDefaultMarkPropertiesOwnership = getOptionalGroupOwnership(
                markPropertiesGroupOwnership);
        final CourseBase course = getService().getCourseAndMarkConfigurationFactory()
                .createCourseFromConfigurationAndDefineMarksAsNeeded(regatta, courseConfiguration,
                        timestampForLogEntries, getService().getServerAuthor(), optionalUserGroupForNonDefaultMarkPropertiesOwnership);
        final RaceLog raceLog = raceColumnByName.getRaceLog(fleetByName);
        raceLog.add(new RaceLogCourseDesignChangedEventImpl(timestampForLogEntries, getService().getServerAuthor(),
                raceLog.getCurrentPassId(), course, CourseDesignerMode.BY_MARKS));
        final CourseConfiguration courseConfigurationResult = getService().getCourseAndMarkConfigurationFactory()
                .createCourseConfigurationFromRegatta(course, regatta, /* tagsToFilterMarkProperties */ null);
        final String jsonString = courseConfigurationJsonSerializer.serialize(courseConfigurationResult).toJSONString();
        return Response.ok(jsonString).build();
    }
}
