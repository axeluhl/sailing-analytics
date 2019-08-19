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

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseTemplateJsonSerializer;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/coursetemplate")
public class CourseTemplateResource extends AbstractSailingServerResource {
    
    private final JsonSerializer<CourseTemplate> courseTemplateSerializer;
    
    public CourseTemplateResource() {
        courseTemplateSerializer = new CourseTemplateJsonSerializer();
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
        // TODO implement
        String jsonResult = "";
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
