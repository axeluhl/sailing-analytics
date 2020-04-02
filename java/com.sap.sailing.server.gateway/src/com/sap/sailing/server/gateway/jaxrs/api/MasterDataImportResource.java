package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;

@Path ("/v1/masterdataimport")
public class MasterDataImportResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(MasterDataImportResource.class.getName());
    
    public MasterDataImportResource() {
    }

    @POST
    // TODO: Move User/Password information from query string to form parameter, due to logging full URLs
    // in access log. Accept bearer token. Use current subject as default
    @Produces("application/json;charset=UTF-8")
    public Response importMasterData(@QueryParam("targetServerUrl") String targetServerUrlAsString,
            @QueryParam("targetServerUsername") String targetServerUsername,
            @QueryParam("targetServerPassword") String targetServerPassword,
            @QueryParam("names[]") List<String> requestedLeaderboardGroups, 
            @QueryParam("override") Boolean override,
            @QueryParam("compress") Boolean compress, 
            @QueryParam("exportWind") Boolean exportWind,
            @QueryParam("exportDeviceConfigs") Boolean exportDeviceConfigs,
            @QueryParam("exportTrackedRacesAndStartTracking") Boolean exportTrackedRacesAndStartTracking) {
        Response response = null;
        if (targetServerUrlAsString == null || requestedLeaderboardGroups.isEmpty() || targetServerUsername == null
                || targetServerPassword == null || override == null || compress == null || exportWind == null
                || exportDeviceConfigs == null || exportTrackedRacesAndStartTracking == null) {
            response = Response.status(Status.BAD_REQUEST).build();
        } else {
            final UUID importMasterDataUid = UUID.randomUUID();
            try {
                getSecurityService().checkCurrentUserServerPermission(ServerActions.CAN_IMPORT_MASTERDATA);
                getService().importMasterData(targetServerUrlAsString,
                        requestedLeaderboardGroups.toArray(new String[requestedLeaderboardGroups.size()]), override,
                        compress, exportWind, exportDeviceConfigs, targetServerUsername, targetServerPassword,
                        exportTrackedRacesAndStartTracking, importMasterDataUid);
                final JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("LeaderboardgroupsImported", requestedLeaderboardGroups);
                jsonResponse.put("ImportedFrom", targetServerUrlAsString);
                jsonResponse.put("override", override);
                jsonResponse.put("exportWind", exportWind);
                jsonResponse.put("exportDeviceConfigs", exportDeviceConfigs);
                jsonResponse.put("exportTrackedRacesAndStartTracking", exportTrackedRacesAndStartTracking);
                response = Response.ok(jsonResponse.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
            } catch (UnauthorizedException e) {
                response = Response.status(Status.UNAUTHORIZED).build();
                logger.warning(e.getMessage() + " for user: " + getSecurityService().getCurrentUser());
            } catch (Throwable e) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                        .type(MediaType.TEXT_PLAIN).build();
                logger.severe(e.toString());
                e.printStackTrace();
            }
        }
        return response;
    }
}
