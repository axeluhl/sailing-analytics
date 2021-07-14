package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;

@Path ("/v1/masterdataimport")
public class MasterDataImportResource extends AbstractSailingServerResource {
    protected static final String LEADERBOARDGROUPS_IMPORTED = "leaderboardgroupsImported";
    protected static final String IMPORTED_FROM = "importedFrom";
    protected static final String EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM = "exportTrackedRacesAndStartTracking";
    protected static final String EXPORT_DEVICE_CONFIGS_FORM_PARAM = "exportDeviceConfigs";
    protected static final String EXPORT_WIND_FORM_PARAM = "exportWind";
    protected static final String COMPRESS_FORM_PARAM = "compress";
    protected static final String OVERRIDE_FORM_PARAM = "override";
    protected static final String LEADERBOARDGROUP_UUID_FORM_PARAM = "leaderboardgroupUUID[]";
    protected static final String REMOTE_SERVER_BEARER_TOKEN_FORM_PARAM = "remoteServerBearerToken";
    protected static final String REMOTE_SERVER_PASSWORD_FORM_PARAM = "remoteServerPassword";
    protected static final String REMOTE_SERVER_USERNAME_FORM_PARAM = "remoteServerUsername";
    protected static final String REMOTE_SERVER_URL_FORM_PARAM = "remoteServerUrl";
    private static final Logger logger = Logger.getLogger(MasterDataImportResource.class.getName());
    
    public MasterDataImportResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response importMasterData(@FormParam(REMOTE_SERVER_URL_FORM_PARAM) String remoteServerUrlAsString,
            @FormParam(REMOTE_SERVER_USERNAME_FORM_PARAM) String remoteServerUsername,
            @FormParam(REMOTE_SERVER_PASSWORD_FORM_PARAM) String remoteServerPassword,
            @FormParam(REMOTE_SERVER_BEARER_TOKEN_FORM_PARAM) String remoteServerBearerToken,
            @FormParam(LEADERBOARDGROUP_UUID_FORM_PARAM) List<UUID> requestedLeaderboardGroupIds,
            @FormParam(OVERRIDE_FORM_PARAM) @DefaultValue("false") Boolean override,
            @FormParam(COMPRESS_FORM_PARAM) @DefaultValue("true") Boolean compress,
            @FormParam(EXPORT_WIND_FORM_PARAM) @DefaultValue("true") Boolean exportWind,
            @FormParam(EXPORT_DEVICE_CONFIGS_FORM_PARAM) @DefaultValue("false") Boolean exportDeviceConfigs,
            @FormParam(EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM) @DefaultValue("true") Boolean exportTrackedRacesAndStartTracking) {
        Response response = null;
        if (!Util.hasLength(remoteServerUrlAsString)) {
            response = badRequest("Remote server URL parameter "+REMOTE_SERVER_URL_FORM_PARAM+" must be present and non-empty");
        } else if (requestedLeaderboardGroupIds.isEmpty()) {
            response = badRequest("You must specify one or more leaderboard groups by their ID using parameter "+LEADERBOARDGROUP_UUID_FORM_PARAM);
        } else if (!validateAuthenticationParameters(remoteServerUsername, remoteServerPassword, remoteServerBearerToken)) {
            response = badRequest("Specify "+REMOTE_SERVER_USERNAME_FORM_PARAM+" and "+REMOTE_SERVER_PASSWORD_FORM_PARAM+" or alternatively "+REMOTE_SERVER_BEARER_TOKEN_FORM_PARAM+" or none of them.");
        } else {
            final UUID importMasterDataUid = UUID.randomUUID();
            try {
                getSecurityService().checkCurrentUserServerPermission(ServerActions.CAN_IMPORT_MASTERDATA);
                final Map<LeaderboardGroup, ? extends Iterable<Event>> eventsForLeaderboardGroups = getService()
                        .importMasterData(remoteServerUrlAsString,
                                requestedLeaderboardGroupIds.toArray(new UUID[requestedLeaderboardGroupIds.size()]),
                                override, compress, exportWind, exportDeviceConfigs, remoteServerUsername,
                                remoteServerPassword, remoteServerBearerToken, exportTrackedRacesAndStartTracking,
                                importMasterDataUid);
                final JSONObject jsonResponse = new JSONObject();
                jsonResponse.put(LEADERBOARDGROUPS_IMPORTED, getLeaderboardGroupNamesFromIdList(eventsForLeaderboardGroups));
                jsonResponse.put(IMPORTED_FROM, remoteServerUrlAsString);
                jsonResponse.put(OVERRIDE_FORM_PARAM, override);
                jsonResponse.put(EXPORT_WIND_FORM_PARAM, exportWind);
                jsonResponse.put(EXPORT_DEVICE_CONFIGS_FORM_PARAM, exportDeviceConfigs);
                jsonResponse.put(EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM, exportTrackedRacesAndStartTracking);
                response = Response.ok(streamingOutput(jsonResponse)).build();
            } catch (UnauthorizedException e) {
                response = Response.status(Status.UNAUTHORIZED).build();
                logger.warning(e.getMessage() + " for user: " + getSecurityService().getCurrentUser());
            } catch (IllegalArgumentException e) {
                response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
                logger.warning(e.getMessage());
            } catch (Throwable e) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                        .type(MediaType.TEXT_PLAIN).build();
                logger.severe(e.toString());
            }
        }
        return response;
    }
    
    private JSONArray getLeaderboardGroupNamesFromIdList(Map<LeaderboardGroup, ? extends Iterable<Event>> eventsForLeaderboardGroups) {
        final JSONArray result = new JSONArray();
        for (Entry<LeaderboardGroup, ? extends Iterable<Event>> lgAndEventIds : eventsForLeaderboardGroups.entrySet()) {
            final JSONObject lgJson = new JSONObject();
            result.add(lgJson);
            lgJson.put(LeaderboardGroupConstants.ID, lgAndEventIds.getKey().getId().toString());
            lgJson.put(LeaderboardGroupConstants.NAME, lgAndEventIds.getKey().getName());
            final JSONArray eventIds = new JSONArray();
            for (final Event event : lgAndEventIds.getValue()) {
                eventIds.add(event.getId().toString());
            }
            lgJson.put(LeaderboardGroupConstants.EVENTS, eventIds);
        }
        return result;
    }
}
