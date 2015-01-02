package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;

/**
 * Right now this service is only used for quick debugging and testing of the polar api. It used plain text responses.
 * JSON should be used if this resource is to be consumed.
 * 
 * @author Frederik Petersen
 *
 */
@Path("/v1/polars")
public class PolarResource extends AbstractSailingServerResource {

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("{boatClassName}")
    public Response getSpeed(@PathParam("boatClassName") String boatClassName, @QueryParam("angle") double angle,
            @QueryParam("windspeedInKnots") double windSpeed) {
        BoatClass boatClass = getService().getDomainObjectFactory().getBaseDomainFactory()
                .getOrCreateBoatClass(boatClassName);
        SpeedWithConfidence<Void> speedWithConfidence;
        ResponseBuilder responseBuilder;
        try {
            speedWithConfidence = getService().getPolarDataService().getSpeed(boatClass, new KnotSpeedImpl(windSpeed),
                    new DegreeBearingImpl(angle), true);
            String resultString = "Speed: " + speedWithConfidence.getObject().getKnots() + "kn; Confidence: "
                    + speedWithConfidence.getConfidence();
            responseBuilder = Response.ok(resultString, MediaType.TEXT_PLAIN);
        } catch (NotEnoughDataHasBeenAddedException e) {
            responseBuilder = Response.noContent();
        }

        return responseBuilder.build();
    }
    
    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("average/{boatClassName}")
    public Response getAverageSpeedAndBearing(@PathParam("boatClassName") String boatClassName,
            @QueryParam("windspeedInKnots") double wSpeed, @QueryParam("legtype") LegType legType, @QueryParam("tack") Tack tack) {
        BoatClass boatClass = getService().getDomainObjectFactory().getBaseDomainFactory()
                .getOrCreateBoatClass(boatClassName);
        ResponseBuilder responseBuilder;
        Speed windSpeed = new KnotSpeedImpl(wSpeed);
        try {
            PolarDataService service = getService().getPolarDataService();
            SpeedWithBearingWithConfidence<Void> speedWithBearing = service.getAverageSpeedWithBearing(boatClass,
                    windSpeed, legType, tack);
            String resultString = "Speed: " + speedWithBearing.getObject().getKnots() + "kn; Angle: "
                    + speedWithBearing.getObject().getBearing().getDegrees() + "°; Confidence: "
                    + speedWithBearing.getConfidence();
            responseBuilder = Response.ok(resultString, MediaType.TEXT_PLAIN);
        } catch (NotEnoughDataHasBeenAddedException e) {
            responseBuilder = Response.noContent();
        }

        return responseBuilder.build();
    }

}
