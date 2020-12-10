package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;

@Path ("/v1/masterdataimport")
public class MasterDataImportResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(MasterDataImportResource.class.getName());
    
    public MasterDataImportResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response importMasterData(
            @FormParam("targetServerUrl") String targetServerUrlAsString,
            @FormParam("targetServerUsername") String targetServerUsername,
            @FormParam("targetServerPassword") String targetServerPassword,
            @FormParam("targetServerBearerToken") String targetServerBearerToken,
            @FormParam("UUID[]") List<UUID> requestedLeaderboardGroupIds, 
            @FormParam("override") Boolean override,
            @FormParam("compress") Boolean compress, 
            @FormParam("exportWind") Boolean exportWind,
            @FormParam("exportDeviceConfigs") Boolean exportDeviceConfigs,
            @FormParam("exportTrackedRacesAndStartTracking") Boolean exportTrackedRacesAndStartTracking) {
        Response response = null;
        if (!Util.hasLength(targetServerUrlAsString) || requestedLeaderboardGroupIds.isEmpty() || override == null
                || compress == null || exportWind == null || exportDeviceConfigs == null
                || exportTrackedRacesAndStartTracking == null
                || ((Util.hasLength(targetServerUsername) && Util.hasLength(targetServerUsername))
                        && Util.hasLength(targetServerBearerToken))) {
            response = Response.status(Status.BAD_REQUEST).build();
        } else {
            final UUID importMasterDataUid = UUID.randomUUID();
            try {
                getSecurityService().checkCurrentUserServerPermission(ServerActions.CAN_IMPORT_MASTERDATA);
                getService().importMasterData(targetServerUrlAsString,
                        requestedLeaderboardGroupIds.toArray(new UUID[requestedLeaderboardGroupIds.size()]), override,
                        compress, exportWind, exportDeviceConfigs, targetServerUsername, targetServerPassword,
                        targetServerBearerToken, exportTrackedRacesAndStartTracking, importMasterDataUid);
                final JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("LeaderboardgroupsImported", getLeaderboardGroupNamesFromIdList(requestedLeaderboardGroupIds));
                jsonResponse.put("ImportedFrom", targetServerUrlAsString);
                jsonResponse.put("override", override);
                jsonResponse.put("exportWind", exportWind);
                jsonResponse.put("exportDeviceConfigs", exportDeviceConfigs);
                jsonResponse.put("exportTrackedRacesAndStartTracking", exportTrackedRacesAndStartTracking);
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
    
    private JSONObject getLeaderboardGroupNamesFromIdList(List<UUID> uuidList) {
        JSONObject result = new JSONObject();
        for (UUID uuid : uuidList) {
            result.put(uuid, getService().getLeaderboardGroupByID(uuid).getName());
        }
        return result;
    }
}
