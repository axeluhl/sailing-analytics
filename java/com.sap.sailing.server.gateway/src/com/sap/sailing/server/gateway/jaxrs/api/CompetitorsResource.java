package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;

@Path("/v1/competitors")
public class CompetitorsResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(CompetitorsResource.class.getName());
    
    /**
     * The maximum size of an image uploaded by a user as a team image, in megabytes (1024*1024 bytes)
     */
    private static final int MAX_SIZE_IN_MB = 5;

    public static JSONObject getCompetitorJSON(Competitor competitor) {
        // see http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft#Competitor-Information
        JSONObject json = new JSONObject();
        json.put("id", competitor.getId().toString());
        json.put("name", competitor.getName());
        json.put("sailID", competitor.getBoat().getSailID());
        json.put("nationality", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
        json.put("countryCode", competitor.getTeam().getNationality().getCountryCode().getTwoLetterISOCode());
        json.put("boatClassName", competitor.getBoat().getBoatClass().getName());

        return json;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{competitorId}")
    public Response getCompetitor(@PathParam("competitorId") String competitorIdAsString) {
        Response response;
        Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(
                competitorIdAsString);
        if (competitor == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a competitor with id '" + competitorIdAsString + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            String jsonString = getCompetitorJSON(competitor).toJSONString();
            response = Response.ok(jsonString, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{competitor-id}/team")
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

    //
    // example for testing upload:
    // $ curl -v -H "Content-Type:image/jpeg" \
    //     --data-binary @<path-to-local-jpg> \
    //     http://127.0.0.1:8888/sailingserver/api/v1/competitors/<competitor-id>/team/image
    @POST
    @Consumes({ "image/jpeg", "image/png" })
    @Path("{competitor-id}/team/image")
    public String setTeamImage(@PathParam("competitor-id") String competitorId, InputStream uploadedInputStream,
            @HeaderParam("Content-Type") String fileType, @HeaderParam("Content-Length") long sizeInBytes) {

        RacingEventService service = getService();
        CompetitorStore store = service.getCompetitorStore();
        Competitor competitor = store.getExistingCompetitorByIdAsString(competitorId);
        if (competitor == null) {
            logger.log(Level.INFO, "Could not find competitor to store image for: " + competitorId);
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity("Could not find competitor with id " + competitorId).type(MediaType.TEXT_PLAIN).build());
        }

        String fileExtension = "";
        if (fileType.equals("image/jpeg")) {
            fileExtension = ".jpg";
        } else if (fileType.equals("image/png")) {
            fileExtension = ".png";
        }

        URI imageUri;
        try {
            if (sizeInBytes > 1024 * 1024 * MAX_SIZE_IN_MB) {
                throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                        .entity("Image is larger than " + MAX_SIZE_IN_MB + "MB").build());
            }
            imageUri = getService().getFileStorageManagementService().getActiveFileStorageService()
                    .storeFile(uploadedInputStream, fileExtension, sizeInBytes);
        } catch (IOException | OperationFailedException | InvalidPropertiesException e) {
            logger.log(Level.WARNING, "Could not store competitor image", e);
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Could not store competitor image").type(MediaType.TEXT_PLAIN).build());
        }

        getService().getCompetitorStore().updateCompetitor(competitorId, competitor.getName(), competitor.getColor(), competitor.getEmail(), 
                competitor.getBoat().getSailID(), competitor.getTeam().getNationality(), imageUri);
        logger.log(Level.INFO, "Set team image for competitor " + competitor.getName());

        JSONObject result = new JSONObject();
        result.put(DeviceMappingConstants.JSON_TEAM_IMAGE_URI, imageUri.toString());
        return result.toString();
    }
}
