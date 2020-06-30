package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;

@Path("/v1/boatclasses")
public class BoatClassesResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getBoatClasses() {
        BoatClassJsonSerializer boatClassJsonSerializer = new BoatClassJsonSerializer();
        final JSONArray result = new JSONArray();
        for (final BoatClass boatClass : getService().getBaseDomainFactory().getBoatClasses()) {
            final JSONObject jsonBoatClass = boatClassJsonSerializer.serialize(boatClass);
            result.add(jsonBoatClass);
        }
        return Response.ok(streamingOutput(result)).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{boatClassName}")
    public Response getBoat(@PathParam("boatClassName") String boatClassName) {
        Response response;
        BoatClass boatClass = getService().getBaseDomainFactory().getBoatClass(boatClassName);
        if (boatClass == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a boat with id '" + StringEscapeUtils.escapeHtml(boatClassName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            final BoatClassJsonSerializer boatClassJsonSerializer = new BoatClassJsonSerializer();
            response = Response.ok(streamingOutput(boatClassJsonSerializer.serialize(boatClass))).build();
        }
        return response;
    }
}
