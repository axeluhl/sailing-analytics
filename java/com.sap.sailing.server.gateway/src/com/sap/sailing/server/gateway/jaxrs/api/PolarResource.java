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
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.PolarDataService;
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
    
    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("average/{boatClassName}")
    public Response getAverageSpeedAndBearing(@PathParam("boatClassName") String boatClassName,
            @QueryParam("windspeed") double wSpeed, @QueryParam("coursetype") String courseType, @QueryParam("tack") String tack) {
        BoatClass boatClass = getService().getDomainObjectFactory().getBaseDomainFactory()
                .getOrCreateBoatClass(boatClassName);
        ResponseBuilder responseBuilder;
        Speed windSpeed = new KnotSpeedImpl(wSpeed);
        try {
            PolarDataService service = getService().getPolarDataService();
            SpeedWithBearing speedWithBearing = null;
            if (tack.matches("starboard")) {
                if (courseType.matches("downwind")) {
                    speedWithBearing = service.getAverageDownwindSpeedWithBearingOnStarboardTackFor(boatClass, windSpeed);
                } else if (courseType.matches("upwind")) {
                    speedWithBearing = service.getAverageUpwindSpeedWithBearingOnStarboardTackFor(boatClass, windSpeed);
                }
            } else if (tack.matches("port")) {
                if (courseType.matches("downwind")) {
                    speedWithBearing = service.getAverageDownwindSpeedWithBearingOnPortTackFor(boatClass, windSpeed);
                } else if (courseType.matches("upwind")) {
                    speedWithBearing = service.getAverageUpwindSpeedWithBearingOnPortTackFor(boatClass, windSpeed);
                }
            }
            String resultString = "Speed: " + speedWithBearing.getKnots() + "kn; Angle: " + speedWithBearing.getBearing().getDegrees() + "°";
            responseBuilder = Response.ok(resultString, MediaType.TEXT_PLAIN);
        } catch (NotEnoughDataHasBeenAddedException e) {
            responseBuilder = Response.noContent();
        }

        return responseBuilder.build();
    }

}
