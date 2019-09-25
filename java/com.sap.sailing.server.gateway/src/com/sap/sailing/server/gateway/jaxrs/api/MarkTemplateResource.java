package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateImpl;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkTemplateJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/marktemplates")
public class MarkTemplateResource extends AbstractSailingServerResource {

    private Response getBadMarkTemplateValidationErrorResponse(String errorText) {
        return Response.status(Status.BAD_REQUEST).entity(StringEscapeUtils.escapeHtml(errorText) + ".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getMarkTemplateNotFoundErrorResponse() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getMarkTemplates() throws Exception {
        Iterable<MarkTemplate> markTemplateList = getSharedSailingData().getAllMarkTemplates();
        JSONArray result = new JSONArray();
        JsonSerializer<MarkTemplate> markTemplateSerializer = new MarkTemplateJsonSerializer();
        for (MarkTemplate markTemplates : markTemplateList) {
            result.add(markTemplateSerializer.serialize(markTemplates));
        }
        final String json = result.toJSONString();
        return Response.ok(json).build();
    }

    @GET
    @Path("{markTemplateId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMarkTemplate(@PathParam("markTemplateId") String markTemplateId) throws Exception {
        MarkTemplate markTemplate = getSharedSailingData().getMarkTemplateById(UUID.fromString(markTemplateId));
        if (markTemplate == null) {
            return getMarkTemplateNotFoundErrorResponse();
        }
        JsonSerializer<MarkTemplate> markTemplateSerializer = new MarkTemplateJsonSerializer();
        final JSONObject serializedMarkedTemplate = markTemplateSerializer.serialize(markTemplate);
        final String json = serializedMarkedTemplate.toJSONString();
        return Response.ok(json).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response createMarkTemplate(@FormParam("name") final String name,
            @FormParam("shortName") final String shortName, @FormParam("color") String rgbColor,
            @FormParam("shape") String shape, @FormParam("pattern") String pattern,
            @FormParam("markType") final String markType) throws Exception {
        if (name == null || name.isEmpty()) {
            return getBadMarkTemplateValidationErrorResponse("name must be given");
        }
        final String effectiveShortName = shortName == null || shortName.isEmpty() ? name : shortName;
        
        Color color = null;
        if (rgbColor != null && rgbColor.length() > 0) {
            try {
                color = new RGBColor(rgbColor);
            } catch (IllegalArgumentException iae) {
                return getBadMarkTemplateValidationErrorResponse(String.format("invalid color %s", iae.getMessage()));
            }
        }
        MarkType type = null;
        if (markType != null && markType.length() > 0) {
            type = MarkType.valueOf(markType);
        }

        final MarkTemplate markTemplate = new MarkTemplateImpl(name, effectiveShortName, color, shape, pattern, type);
        final MarkTemplate createdMarkTemplate = getSharedSailingData().createMarkTemplate(markTemplate);
        JsonSerializer<MarkTemplate> markTemplateSerializer = new MarkTemplateJsonSerializer();
        final JSONObject serializedMarkedTemplate = markTemplateSerializer.serialize(createdMarkTemplate);
        final String json = serializedMarkedTemplate.toJSONString();
        return Response.ok(json).build();
    }

}
