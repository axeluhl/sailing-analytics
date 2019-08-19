package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseTemplateJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseTemplateJsonSerializer;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/coursetemplates")
public class CourseTemplateResource extends AbstractSailingServerResource {
    
    private final JsonSerializer<CourseTemplate> courseTemplateSerializer;
    private final JsonDeserializer<CourseTemplate> courseTemplateDeserializer;
    
    public CourseTemplateResource() {
        courseTemplateSerializer = new CourseTemplateJsonSerializer();
        courseTemplateDeserializer = new CourseTemplateJsonDeserializer(getSharedSailingData()::getMarkTemplateById);
    }

    private Response getBadCourseTemplateValidationErrorResponse(String errorText) {
        return Response.status(Status.BAD_REQUEST).entity(StringEscapeUtils.escapeHtml(errorText) + ".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getCourseTemplateNotFoundErrorResponse() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getCourseTemplates(@QueryParam("tag") List<String> tags) throws Exception {
        Iterable<CourseTemplate> courseTemplateList = getSharedSailingData().getAllCourseTemplates(tags);
        JSONArray result = new JSONArray();
        for (CourseTemplate courseTemplate : courseTemplateList) {
            result.add(courseTemplateSerializer.serialize(courseTemplate));
        }
        final String json = result.toJSONString();
        return Response.ok(json).build();
    }

    @GET
    @Path("{courseTemplateId}")
    @Produces("application/json;charset=UTF-8")
    public Response getCourseTemplate(@PathParam("courseTemplateId") String courseTemplateId) throws Exception {
        CourseTemplate courseTemplate = getSharedSailingData().getCourseTemplateById(UUID.fromString(courseTemplateId));
        if (courseTemplate == null) {
            return getCourseTemplateNotFoundErrorResponse();
        }
        
        final JSONObject serializedMarkedProperties = courseTemplateSerializer.serialize(courseTemplate);
        final String json = serializedMarkedProperties.toJSONString();
        return Response.ok(json).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response createCourseTemplate(String json) throws Exception {
        if (json == null || json.isEmpty()) {
            getBadCourseTemplateValidationErrorResponse("Course template is required to be given as json object");
        }
        final Object parsedObject = new JSONParser().parse(json);
        if (parsedObject == null || !(parsedObject instanceof JSONObject)) {
            getBadCourseTemplateValidationErrorResponse("Course template is required to be given as json object");
        }
        final CourseTemplate deserializedCourseTemplate = courseTemplateDeserializer
                .deserialize((JSONObject) parsedObject);
        final CourseTemplate createdCourseTemplate = getSharedSailingData().createCourseTemplate(
                deserializedCourseTemplate.getName(), deserializedCourseTemplate.getMarkTemplates(),
                deserializedCourseTemplate.getWaypointTemplates(1), deserializedCourseTemplate.getRepeatablePart(),
                deserializedCourseTemplate.getTags(), deserializedCourseTemplate.getOptionalImageURL());
        final JSONObject serializedMarkedProperties = courseTemplateSerializer.serialize(createdCourseTemplate);
        final String jsonResult = serializedMarkedProperties.toJSONString();
        return Response.ok(jsonResult).build();
    }

    @DELETE
    @Path("{courseTemplateId}")
    public Response deleteCourseTemplate(@PathParam("courseTemplateId") String courseTemplateId) throws Exception {
        CourseTemplate courseTemplate = getSharedSailingData().getCourseTemplateById(UUID.fromString(courseTemplateId));
        if (courseTemplate == null) {
            return getCourseTemplateNotFoundErrorResponse();
        }
        
        getSharedSailingData().deleteCourseTemplate(courseTemplate);
        return Response.ok().build();
    }
}
