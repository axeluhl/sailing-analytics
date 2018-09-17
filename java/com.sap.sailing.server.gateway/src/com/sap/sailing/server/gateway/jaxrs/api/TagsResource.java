package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.tagging.TagDTODeSerializer;
import com.sap.sailing.server.tagging.TaggingService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@Path("/v1/{" + RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "}/{" + RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME
        + "}/{" + RaceLogServletConstants.PARAMS_RACE_FLEET_NAME + "}/tags")
public class TagsResource extends AbstractSailingServerResource {

    private static final Logger logger = Logger.getLogger(TagsResource.class.getName());
    private static final String APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";
    private static final String TEXT_PLAIN_UTF8 = MediaType.TEXT_PLAIN + ";charset=UTF-8";

    private final TagDTODeSerializer serializer;

    public TagsResource() {
        serializer = new TagDTODeSerializer();
    }

    /**
     * Loads public tags from {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog} and private tags from
     * {@link com.sap.sse.security.UserStore UserStore} in case current user is logged in. Only loads tags when
     * parameters <code>leaderboardName</code>, <code>raceColumnName</code> and <code>fleetName</code> can identify
     * {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog}.
     * 
     * @return status 200 (including tags) if request was successful, otherwise 400 (bad request) or 500 (internal
     *         server error)
     */
    @GET
    @Produces({ APPLICATION_JSON_UTF8, TEXT_PLAIN_UTF8 })
    public Response getTags(@PathParam(RaceLogServletConstants.PARAMS_LEADERBOARD_NAME) String leaderboardName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName) {
        // TODO: Allow to get only public, only private tags or both of them
        // TODO: Allow to get only unrevoked tags
        Response response;
        TaggingService taggingService = getService().getTaggingService();
        if (taggingService != null) {
            List<TagDTO> tags = new ArrayList<TagDTO>();

            List<TagDTO> publicTags = taggingService.getPublicTags(leaderboardName, raceColumnName, fleetName);
            Util.addAll(publicTags, tags);

            List<TagDTO> privateTags = taggingService.getPrivateTags(leaderboardName, raceColumnName, fleetName);
            Util.addAll(privateTags, tags);

            JSONArray jsonTags = serializer.serialize(tags);
            response = Response.ok(jsonTags.toJSONString()).type(APPLICATION_JSON_UTF8).build();
        } else {
            response = Response.status(Status.INTERNAL_SERVER_ERROR).type(TEXT_PLAIN_UTF8)
                    .entity("Tagging Service not found!").build();
            logger.warning("Tagging Service not found!");
        }
        return response;
    }

    /**
     * Saves new tag.
     * 
     * @param tag
     *            may not be empty
     * @param visible
     *            default is <code>false</code>
     * 
     * @return status 201 (created) if creation was successful, otherwise 400 (bad request)
     * @see TaggingService#addTag(String, String, String, String, String, String, boolean, TimePoint)
     */
    @POST
    @Produces({ APPLICATION_JSON_UTF8, TEXT_PLAIN_UTF8 })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createTag(@Context UriInfo uriInfo,
            @PathParam(RaceLogServletConstants.PARAMS_LEADERBOARD_NAME) String leaderboardName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName, @FormParam("tag") String tag,
            @FormParam("comment") String comment, @FormParam("image") String imageURL,
            @FormParam("public") boolean visibleForPublic, @FormParam("raceTimepoint") long raceTimepoint) {
        Response response;
        TaggingService taggingService = getService().getTaggingService();
        boolean successful = taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL,
                visibleForPublic, new MillisecondsTimePoint(raceTimepoint));
        if (successful) {
            response = Response.created(uriInfo.getRequestUri()).build();
        } else {
            String errorMessage = taggingService.getLastErrorCode().getMessage();
            response = Response.status(Status.BAD_REQUEST).type(TEXT_PLAIN_UTF8).entity(errorMessage).build();
            logger.warning("Could not save tag! " + errorMessage);
        }
        return response;
    }

    /**
     * Removes tag given as json string.
     * 
     * @return status 204 (no content) if deletion was successful, otherwise 400 (bad request)
     * @see TagDTO
     * @see TaggingService#removeTag(String, String, String, TagDTO)
     */
    @DELETE
    @Produces({ APPLICATION_JSON_UTF8, TEXT_PLAIN_UTF8 })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteTag(@PathParam(RaceLogServletConstants.PARAMS_LEADERBOARD_NAME) String leaderboardName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName,
            @FormParam("tag_json") String tagJson) {
        Response response;
        TagDTO tagToRemove = serializer.deserializeTag(tagJson);
        TaggingService taggingService = getService().getTaggingService();
        boolean successful = taggingService.removeTag(leaderboardName, raceColumnName, fleetName, tagToRemove);
        if (successful) {
            response = Response.noContent().build();
        } else {
            String errorMessage = taggingService.getLastErrorCode().getMessage();
            response = Response.status(Status.BAD_REQUEST).type(TEXT_PLAIN_UTF8).entity(errorMessage).build();
            logger.warning("Could not remove tag! " + errorMessage);
        }
        return response;
    }

    /**
     * Updates tag.
     * 
     * @param tag
     *            may not be empty
     * @param visible
     *            default is <code>false</code>
     * 
     * @return status 204 (no content) if update was successful, otherwise 400 (bad request)
     * @see TagDTO
     * @see TaggingService#updateTag(String, String, String, TagDTO, String, String, String, boolean)
     */
    @PUT
    @Produces({ APPLICATION_JSON_UTF8, TEXT_PLAIN_UTF8 })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateTag(@PathParam(RaceLogServletConstants.PARAMS_LEADERBOARD_NAME) String leaderboardName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @PathParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName,
            @FormParam("tag_json") String tagJson, @FormParam("tag") String tag, @FormParam("comment") String comment,
            @FormParam("image") String imageURL, @FormParam("public") boolean visibleForPublic) {
        // TODO: What happens if parameters are missing? Old values should stay the same.
        Response response;
        TagDTO tagToUpdate = serializer.deserializeTag(tagJson);
        TaggingService taggingService = getService().getTaggingService();
        boolean successful = taggingService.updateTag(leaderboardName, raceColumnName, fleetName, tagToUpdate, tag,
                comment, imageURL, visibleForPublic);
        if (successful) {
            response = Response.noContent().build();
        } else {
            String errorMessage = taggingService.getLastErrorCode().getMessage();
            response = Response.status(Status.BAD_REQUEST).type(TEXT_PLAIN_UTF8).entity(errorMessage).build();
            logger.warning("Could not update tag! " + errorMessage);
        }
        return response;
    }
}
