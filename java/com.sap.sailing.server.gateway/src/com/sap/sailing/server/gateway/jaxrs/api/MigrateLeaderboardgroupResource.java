package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.security.util.RemoteServerUtil;

@Path ("/v1/scopemigration")
public class MigrateLeaderboardgroupResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(MigrateLeaderboardgroupResource.class.getName());

    
    public MigrateLeaderboardgroupResource() {
    }
    
    @Path ("/movetodedicatedserver")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response migrateLeaderboardGroup(
            @FormParam("server1") String baseServer,
            @FormParam("server2") String dedicatedServer,
            @FormParam("UUID[]") Set<String> requestedLeaderboardGroups,
            @FormParam("user1") String user1,
            @FormParam("user2") String user2,
            @FormParam("password1") String password1,
            @FormParam("password2") String password2,
            @FormParam("bearer1") String bearer1,
            @FormParam("bearer2") String bearer2) {
        Response response = null;
        final String baseServerBearerToken = getService().getOrCreateTargetServerBearerToken(baseServer, user1, password1, bearer1);
        final String dedicatedServerBearerToken = getService().getOrCreateTargetServerBearerToken(dedicatedServer, user2, password2, bearer2);
        try {
            doMDI(baseServer, dedicatedServer, requestedLeaderboardGroups, baseServerBearerToken,
                    dedicatedServerBearerToken);
        } catch (Exception e) {
            response = returnInternalServerError(e);
        }
        
        
        return response;
    }
    

    private void doMDI(String baseServerHostAsString, String dedicatedServerHostAsString,
            Set<String> leaderboardGroupIds, String baseServerBearerToken, String dedicatedServerBearerToken)
            throws Exception {
        final URL mdiUrl = RemoteServerUtil.createRemoteServerUrl(
                RemoteServerUtil.createBaseUrl(dedicatedServerHostAsString), "/sailingserver/api/v1/masterdataimport",
                null);
        final HttpURLConnection mdiConnection = (HttpURLConnection) mdiUrl.openConnection();
        mdiConnection.setRequestProperty("Authorization", "Bearer " + dedicatedServerBearerToken);
        mdiConnection.setRequestMethod("POST");
        mdiConnection.setDoOutput(true);
        final StringJoiner query = new StringJoiner("&");
        query.add("targetServerUrl=" + baseServerHostAsString);
        query.add("override=false");
        query.add("compress=true");
        query.add("exportWind=true");
        // TODO: Think about device configs
        query.add("exportDeviceConfigs=false");
        query.add("exportTrackedRacesAndStartTracking=true");
        query.add("targetServerBearerToken=" + URLEncoder.encode(baseServerBearerToken, "utf-8"));
        for (String uuid : leaderboardGroupIds) {
            query.add("uuids[]=" + uuid);
        }
        byte[] out = query.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        mdiConnection.setFixedLengthStreamingMode(length);
        mdiConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        mdiConnection.connect();
        try(OutputStream os = mdiConnection.getOutputStream()) {
            os.write(out);
            os.flush();
        }
    }
    
    
    private Response returnInternalServerError(Throwable e) {
        final Response response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        logger.severe(e.toString());
        return response;
    }
}
