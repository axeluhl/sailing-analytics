package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/v1/team")
public class TeamResource extends AbstractSailingServerResource {

    private static final Logger logger = Logger.getLogger(TeamResource.class.getName());

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{competitor-id}")
    public Response getTeam(@PathParam("competitor-id") String competitorId) {
        Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);

        if (competitor == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a competitor with id '" + competitorId + "'.").type(MediaType.TEXT_PLAIN)
                    .build();
        }

        Team team = competitor.getTeam();

        if (team == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a team associated with competitor '" + competitorId + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        TeamJsonSerializer teamJsonSerializer = new TeamJsonSerializer(new PersonJsonSerializer(
                new NationalityJsonSerializer()));
        JSONObject teamJson = teamJsonSerializer.serialize(team);
        String json = teamJson.toJSONString();

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    @Path("{competitor-id}/image")
    public String setTeamImage(@PathParam("competitor-id") String competitorId,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails) {
        Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
        if (competitor == null) {
            logger.log(Level.INFO, "Could not find competitor to store image for: " + competitorId);
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity("Could not find competitor with id " + competitorId).type(MediaType.TEXT_PLAIN).build());
        }

        URI imageUri;
        try {
            String fileName = fileDetails.getFileName();
            long sizeInBytes = fileDetails.getSize();
            imageUri = getService().getFileStorageService().storeFile(uploadedInputStream, fileName, sizeInBytes);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not store competitor image", e);
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Could not store competitor image").type(MediaType.TEXT_PLAIN).build());
        }

        getService().getCompetitorStore().updateCompetitor(competitorId, competitor.getName(), competitor.getColor(),
                competitor.getBoat().getSailID(), competitor.getTeam().getNationality(), imageUri);
        logger.log(Level.INFO, "Set team image for competitor " + competitor.getName());

        return Response.status(Status.OK).build();
    }
}
