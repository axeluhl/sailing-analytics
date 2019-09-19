package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.impl.CourseTemplateImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseConfigurationBuilder;
import com.sap.sailing.server.gateway.deserialization.impl.CourseConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseConfigurationJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseTemplateJsonSerializer;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/courseconfiguration")
public class CourseConfigurationResource extends AbstractSailingServerResource {

    private final JsonSerializer<CourseConfiguration> courseConfigurationJsonSerializer;
    private final JsonSerializer<CourseTemplate> courseTemplateJsonSerializer;

    public CourseConfigurationResource() {
        courseConfigurationJsonSerializer = new CourseConfigurationJsonSerializer();
        courseTemplateJsonSerializer = new CourseTemplateJsonSerializer();
    }

    private JsonDeserializer<CourseConfiguration> getCourseConfigurationDeserializer(final Regatta regatta,
            final CourseTemplate courseTemplate) {
        return new CourseConfigurationJsonDeserializer(this.getSharedSailingData(), regatta, courseTemplate);
    }

    private Response getBadRegattaErrorResponse(String regattaName) {
        return Response.status(Status.NOT_FOUND)
                .entity("Could not find a regatta with name '" + StringEscapeUtils.escapeHtml(regattaName) + "'.")
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
    @Path("getFromRegatta/{regattaName}")
    public Response createCourseConfigurationForRegatta(@PathParam("regattaName") String regattaName,
            @PathParam("raceName") String raceName) throws Exception {
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            return getBadRegattaErrorResponse(regattaName);
        }
        // TODO: get CourseConfiguration for regatta and race
        CourseConfiguration courseConfiguration = null;

        String jsonString = courseConfigurationJsonSerializer.serialize(courseConfiguration).toJSONString();
        return Response.ok(jsonString).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("getFromCourseTemplate/{courseTemplateId}")
    public Response createCourseConfigurationForCourseTemplate(@PathParam("courseTemplateId") String courseTemplateId,
            @QueryParam("reagttaName") String regattaName) {
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

        // TODO: build from coursetemplate?
        CourseConfigurationBuilder builder = new CourseConfigurationBuilder(this.getSharedSailingData(), regatta,
                courseTemplate);
        CourseConfiguration courseConfiguration = builder.build();

        String jsonString = courseConfigurationJsonSerializer.serialize(courseConfiguration).toJSONString();
        return Response.ok(jsonString).build();

    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("createCourseTemplate")
    public Response createCourseTemplate(@QueryParam("regattaName") String regattaName, String json) throws Exception {
        if (json == null || json.isEmpty()) {
            getBadCourseConfigurationValidationErrorResponse(
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
        final CourseConfiguration courseConfiguration = getCourseConfigurationDeserializer(regatta,
                /* courseTemplate */ null).deserialize((JSONObject) parsedObject);

        // TODO: create courseTemplate über new CourseTemplateImpl(...)?
        CourseTemplate courseTemplate = null; //new CourseTemplateImpl();

        String jsonString = courseTemplateJsonSerializer.serialize(courseTemplate).toJSONString();
        return Response.ok(jsonString).build();
    }
}
