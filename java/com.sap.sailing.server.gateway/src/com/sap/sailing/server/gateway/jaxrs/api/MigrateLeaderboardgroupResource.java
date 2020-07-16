package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.InputStreamReader;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.util.RemoteServerUtil;

@Path ("/v1/scopemigration")
public class MigrateLeaderboardgroupResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(MigrateLeaderboardgroupResource.class.getName());

    private static final String MDI_PATH = "/sailingserver/api/v1/masterdataimport";
    private static final String COMPARESERVERS_PATH = "/sailingserver/api/v1/compareservers";
    private static final String REMOTESERVERREFERENCEADD_PATH = "/sailingserver/api/v1/remoteserverreference/add";
    
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
        final String baseServerBearerToken = getService().getOrCreateTargetServerBearerToken(baseServer, user1,
                password1, bearer1);
        final String dedicatedServerBearerToken = getService().getOrCreateTargetServerBearerToken(dedicatedServer,
                user2, password2, bearer2);
        try {
            final JSONObject result = new JSONObject();
            final Util.Pair<JSONObject, Number> mdi = doMDI(baseServer, dedicatedServer, requestedLeaderboardGroups,
                    baseServerBearerToken, dedicatedServerBearerToken);
            final Util.Pair<JSONObject, Number> compareServers = doCompareServers(baseServer, dedicatedServer,
                    dedicatedServerBearerToken, baseServerBearerToken, requestedLeaderboardGroups);
            final Util.Pair<JSONObject, Number> remoteServerReferenceAdd = doRemoteServerReferenceAdd(dedicatedServer,
                    baseServer, dedicatedServerBearerToken);
            result.put("MDI", mdi.getA());
            result.put("CompareServers", compareServers.getA());
            result.put("RemoteServerReferenceAdd", remoteServerReferenceAdd.getA());
            if (mdi.getB().intValue() != 200 || compareServers.getB().intValue() != 200
                    || remoteServerReferenceAdd.getB().intValue() != 200) {
                response = Response.status(Status.CONFLICT).entity(streamingOutput(result)).build();
            } else {
                response = Response.ok(streamingOutput(result)).build();
            }
        } catch (Exception e) {
            response = returnInternalServerError(e);
        }
        return response;
    }
    
    private Util.Pair<JSONObject, Number> doMDI(String baseServerHostAsString, String dedicatedServerHostAsString,
            Set<String> leaderboardGroupIds, String baseServerBearerToken, String dedicatedServerBearerToken)
            throws Exception {
        final HttpURLConnection mdiConnection = createHttpUrlConnection(dedicatedServerHostAsString,
                dedicatedServerBearerToken, MDI_PATH);
        final StringJoiner form = new StringJoiner("&");
        form.add("targetServerUrl=" + baseServerHostAsString);
        form.add("override=false");
        form.add("compress=true");
        form.add("exportWind=true");
        // TODO: Think about device configs
        form.add("exportDeviceConfigs=false");
        form.add("exportTrackedRacesAndStartTracking=true");
        form.add("targetServerBearerToken=" + URLEncoder.encode(baseServerBearerToken, "utf-8"));
        form.add(addLeaderboardGroupIdsToStringJoiner(leaderboardGroupIds).toString());
        byte[] out = form.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        mdiConnection.setFixedLengthStreamingMode(length);
        mdiConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        mdiConnection.connect();
        try(OutputStream os = mdiConnection.getOutputStream()) {
            os.write(out);
            os.flush();
        }
        final JSONObject json = parseInputStreamToJsonAndLog(mdiConnection);
        Util.Pair<JSONObject, Number> result = new Util.Pair<>(json, mdiConnection.getResponseCode());
        return result;
    }

    private Util.Pair<JSONObject, Number> doCompareServers(String baseServerHostAsString,
            String dedicatedServerHostAsString, String dedicatedServerBearerToken, String baseServerBearerToken,
            Set<String> leaderboardGroupIds) throws Exception {
        final HttpURLConnection compareServersConnection = createHttpUrlConnection(dedicatedServerHostAsString,
                dedicatedServerBearerToken, COMPARESERVERS_PATH);
        final StringJoiner form = new StringJoiner("&");
        form.add("server1=" + baseServerHostAsString);
        form.add("server2=" + dedicatedServerHostAsString);
        form.add("bearer1=" + URLEncoder.encode(baseServerBearerToken, "utf-8"));
        form.add("bearer2=" + URLEncoder.encode(dedicatedServerBearerToken, "utf-8"));
        form.add(addLeaderboardGroupIdsToStringJoiner(leaderboardGroupIds).toString());
        byte[] out = form.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        compareServersConnection.setFixedLengthStreamingMode(length);
        compareServersConnection.connect();
        try (OutputStream os = compareServersConnection.getOutputStream()) {
            os.write(out);
            os.flush();
        }
        final JSONObject json = parseInputStreamToJsonAndLog(compareServersConnection);
        final Util.Pair<JSONObject, Number> result = new Pair<JSONObject, Number>(json,
                compareServersConnection.getResponseCode());
        return result;
    }

    private Util.Pair<JSONObject, Number> doRemoteServerReferenceAdd(String dedicateServerHostAsString,
            String baseServerHostAsString, String dedicatedServerBearerToken) throws Exception {
        final HttpURLConnection remoteServerReferenceAddConnection = createHttpUrlConnection(dedicateServerHostAsString,
                dedicatedServerBearerToken, REMOTESERVERREFERENCEADD_PATH);
        final StringJoiner form = new StringJoiner("&");
        form.add("remoteServerUrl=" + baseServerHostAsString);
        form.add("remoteServerName=" + baseServerHostAsString);
        byte[] out = form.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        remoteServerReferenceAddConnection.setFixedLengthStreamingMode(length);
        remoteServerReferenceAddConnection.connect();
        try (OutputStream os = remoteServerReferenceAddConnection.getOutputStream()) {
            os.write(out);
            os.flush();
        }
        final JSONObject json = parseInputStreamToJsonAndLog(remoteServerReferenceAddConnection);
        final Util.Pair<JSONObject, Number> result = new Pair<JSONObject, Number>(json,
                remoteServerReferenceAddConnection.getResponseCode());
        return result;
    }

    private JSONObject parseInputStreamToJsonAndLog(HttpURLConnection connection) throws Exception {
        final JSONParser parser = new JSONParser();
        final JSONObject json = (JSONObject) parser
                .parse(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        logger.info(json.toString());
        return json;
    }

    private HttpURLConnection createHttpUrlConnection(String dedicatedServerHostAsString,
            String dedicatedServerBearerToken, String path) throws Exception {
        final URL url = RemoteServerUtil
                .createRemoteServerUrl(RemoteServerUtil.createBaseUrl(dedicatedServerHostAsString), path, null);
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Authorization", "Bearer " + dedicatedServerBearerToken);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return urlConnection;
    }
    
    private StringJoiner addLeaderboardGroupIdsToStringJoiner(Set<String> leaderboardGroupIds) {
        final StringJoiner form = new StringJoiner("&");
        for (String uuid : leaderboardGroupIds) {
            form.add("UUID[]=" + uuid);
        }
        return form;
    }

    private Response returnInternalServerError(Throwable e) {
        final Response response = Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(e.toString() + "\nSee server log for detailed information.").build();
        logger.severe(e.toString());
        return response;
    }
}
