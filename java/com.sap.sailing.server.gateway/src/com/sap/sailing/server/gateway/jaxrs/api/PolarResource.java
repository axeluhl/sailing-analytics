package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.SpeedWithConfidenceWithIntegerRelationJsonSerializer;

@Path("/v1/polars")
public class PolarResource extends AbstractSailingServerResource {

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{boatClassName}")
    public Response getSpeed(@PathParam("boatClassName") String boatClassName, @QueryParam("angle") double angle,
            @QueryParam("windspeed") double windSpeed) {
        BoatClass boatClass = getService().getDomainObjectFactory().getBaseDomainFactory()
                .getOrCreateBoatClass(boatClassName);
        SpeedWithConfidence<Integer> speedWithConfidence;
        ResponseBuilder responseBuilder;
        try {
            speedWithConfidence = getService().getPolarDataService().getSpeed(boatClass, new KnotSpeedImpl(windSpeed),
                    new DegreeBearingImpl(angle));
            SpeedWithConfidenceWithIntegerRelationJsonSerializer serializer = new SpeedWithConfidenceWithIntegerRelationJsonSerializer();
            JSONObject jsonObj = serializer.serialize(speedWithConfidence);
            responseBuilder = Response.ok(jsonObj.toJSONString(), MediaType.APPLICATION_JSON);
        } catch (NotEnoughDataHasBeenAddedException e) {
            responseBuilder = Response.noContent();
        }

        return responseBuilder.build();
    }

}
