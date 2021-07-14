package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.util.RemoteServerUtil;
import com.sap.sse.util.HttpUrlConnectionHelper;

@Path ("/v1/scopemigration")
public class MigrateLeaderboardgroupResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(MigrateLeaderboardgroupResource.class.getName());

    private static final String MDI_PATH = "/sailingserver/api/v1/masterdataimport";
    private static final String COMPARESERVERS_PATH = "/sailingserver/api/v1/compareservers";
    private static final String REMOTESERVERREFERENCEADD_PATH = "/sailingserver/api/v1/remoteserverreference/add";
    private static final String REMOTESERVERREFERENCEDELETE_PATH = "/sailingserver/api/v1/remoteserverreference/remove";
    protected static final String REMOTE_SERVER_REFERENCE_REMOVED = "remoteServerReferenceRemoved";
    protected static final String COMPARE_SERVERS = "compareServers";
    protected static final String MDI = "MDI";
    protected static final String REMOTE_SERVER_REFERENCE_ADD = "remoteServerReferenceAdd";
    protected static final String DEDICATED_SERVER_FORM_PARAM = "dedicated";
    protected static final String ARCHIVE_SERVER_FORM_PARAM = "archive";
    protected static final String SERVER1_FORM_PARAM = "server1";
    protected static final String SERVER2_FORM_PARAM = "server2";
    protected static final String USER1_FORM_PARAM = "user1";
    protected static final String USER2_FORM_PARAM = "user2";
    protected static final String PASSWORD1_FORM_PARAM = "password1";
    protected static final String PASSWORD2_FORM_PARAM = "password2";
    protected static final String BEARER1_FORM_PARAM = "bearer1";
    protected static final String BEARER2_FORM_PARAM = "bearer2";
    protected static final String LEADERBOARD_GROUPS_UUID_FORM_PARAM = "leaderboardgroupUUID[]";
    private static final String ARCHIVE_SERVER_BASE_URL = "www.sapsailing.com";
    private static final String OVERRIDE = "override";

    @Context
    UriInfo uriInfo;

    public MigrateLeaderboardgroupResource() {
    }
    
    @Path ("/movetodedicatedserver")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response moveToDedicatedServer(
            @FormParam(SERVER1_FORM_PARAM) String baseServer,
            @FormParam(SERVER2_FORM_PARAM) String dedicatedServer,
            @FormParam(LEADERBOARD_GROUPS_UUID_FORM_PARAM) Set<String> requestedLeaderboardGroups,
            @FormParam(USER1_FORM_PARAM) String user1,
            @FormParam(USER2_FORM_PARAM) String user2,
            @FormParam(PASSWORD1_FORM_PARAM) String password1,
            @FormParam(PASSWORD2_FORM_PARAM) String password2,
            @FormParam(BEARER1_FORM_PARAM) String bearer1,
            @FormParam(BEARER2_FORM_PARAM) String bearer2,
            @FormParam(OVERRIDE) @DefaultValue("false") boolean override) {
        Response response;
        final String effectiveBaseServer = !Util.hasLength(baseServer) ? uriInfo.getBaseUri().getAuthority() : baseServer;
        if (!validateAuthenticationParameters(user1, password1, bearer1)) {
            response = badRequest("Specify "+USER1_FORM_PARAM+" and "+PASSWORD1_FORM_PARAM+" or alternatively "+BEARER1_FORM_PARAM+" or none of them.");
        } else if (!validateAuthenticationParameters(user2, password2, bearer2)) {
            response = badRequest("Specify "+USER2_FORM_PARAM+" and "+PASSWORD2_FORM_PARAM+" or alternatively "+BEARER2_FORM_PARAM+" or none of them.");
        } else if (!Util.hasLength(dedicatedServer)) {
            response = badRequest("A target server must be specified with the "+SERVER2_FORM_PARAM+" parameter");
        } else {
            final String baseServerBearerToken = getService().getOrCreateTargetServerBearerToken(effectiveBaseServer, user1, password1, bearer1);
            final String dedicatedServerBearerToken = getService().getOrCreateTargetServerBearerToken(dedicatedServer, user2, password2, bearer2);
            try {
                final JSONObject result = new JSONObject();
                final Util.Pair<JSONObject, Number> mdi = doMDI(effectiveBaseServer, dedicatedServer, requestedLeaderboardGroups,
                        baseServerBearerToken, dedicatedServerBearerToken, override);
                final Util.Pair<JSONObject, Number> compareServers = doCompareServers(effectiveBaseServer, dedicatedServer,
                        dedicatedServerBearerToken, baseServerBearerToken, requestedLeaderboardGroups);
                // add a remote reference from the base server pointing to the dedicated server
                final Util.Pair<JSONObject, Number> remoteServerReferenceAdd = doRemoteServerReferenceAdd(effectiveBaseServer,
                        dedicatedServer, baseServerBearerToken);
                // FIXME bug5311 and now remove the event / the leaderboard groups in the base location, but preserve security/ownerships/ACLs in case of shared security!
                // FIXME bug5311 adjust request routing such that requests targeting the content moved will be routed to the new "dedicatedServer" location
                result.put(MDI, mdi.getA());
                result.put(COMPARE_SERVERS, compareServers.getA());
                result.put(REMOTE_SERVER_REFERENCE_ADD, remoteServerReferenceAdd.getA());
                if (mdi.getB().intValue() != 200 || compareServers.getB().intValue() != 200
                        || remoteServerReferenceAdd.getB().intValue() != 200) {
                    response = Response.status(Status.CONFLICT).entity(streamingOutput(result)).build();
                } else {
                    response = Response.ok(streamingOutput(result)).build();
                }
            } catch (Exception e) {
                response = returnInternalServerError(e);
            }
        }
        return response;
    }
    
    @Path ("/movetoarchiveserver")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response moveToArchiveServer(
            @FormParam(ARCHIVE_SERVER_FORM_PARAM) @DefaultValue(ARCHIVE_SERVER_BASE_URL) String archiveServer,
            @FormParam(DEDICATED_SERVER_FORM_PARAM) String dedicatedServer,
            @FormParam(LEADERBOARD_GROUPS_UUID_FORM_PARAM) Set<String> requestedLeaderboardGroups,
            @FormParam(USER1_FORM_PARAM) String user1,
            @FormParam(USER2_FORM_PARAM) String user2,
            @FormParam(PASSWORD1_FORM_PARAM) String password1,
            @FormParam(PASSWORD2_FORM_PARAM) String password2,
            @FormParam(BEARER1_FORM_PARAM) String bearer1,
            @FormParam(BEARER2_FORM_PARAM) String bearer2,
            @FormParam(OVERRIDE) @DefaultValue("false") boolean override) {
        Response response;
        if (!validateAuthenticationParameters(user1, password1, bearer1)) {
            response = badRequest("Specify "+USER1_FORM_PARAM+" and "+PASSWORD1_FORM_PARAM+" or alternatively "+BEARER1_FORM_PARAM+" or none of them.");
        } else  if (!validateAuthenticationParameters(user2, password2, bearer2)) {
            response = badRequest("Specify "+USER2_FORM_PARAM+" and "+PASSWORD2_FORM_PARAM+" or alternatively "+BEARER2_FORM_PARAM+" or none of them.");
        } else {
            final String archiveServerBearerToken = getService().getOrCreateTargetServerBearerToken(
                    archiveServer, user1, password1, bearer1);
            final String dedicatedServerBearerToken = getService().getOrCreateTargetServerBearerToken(
                    dedicatedServer, user2, password2, bearer2);
            try {
                final JSONObject result = new JSONObject();
                final Util.Pair<JSONObject, Number> mdi = doMDI(dedicatedServer, archiveServer, requestedLeaderboardGroups,
                        dedicatedServerBearerToken, archiveServerBearerToken, override);
                final Util.Pair<JSONObject, Number> compareServers = doCompareServers(dedicatedServer, archiveServer,
                        archiveServerBearerToken, dedicatedServerBearerToken, requestedLeaderboardGroups);
                final Util.Pair<JSONObject, Number> remoteServerReferenceRemove = doRemoteServerReferenceRemove(
                        archiveServer, dedicatedServer, archiveServerBearerToken);
                // FIXME bug5311: update archive server reverse proxy settings for the event(s) imported, then dismantle the dedicated replica set and consider archiving its DB to "slow" if it was on "live"
                result.put(MDI, mdi.getA());
                result.put(COMPARE_SERVERS, compareServers.getA());
                result.put(REMOTE_SERVER_REFERENCE_REMOVED, remoteServerReferenceRemove.getA());
                if (mdi.getB().intValue() != 200 || compareServers.getB().intValue() != 200
                        || remoteServerReferenceRemove.getB().intValue() != 200) {
                    response = Response.status(Status.CONFLICT).entity(streamingOutput(result)).build();
                } else {
                    response = Response.ok(streamingOutput(result)).build();
                }
            } catch (Exception e) {
                response = returnInternalServerError(e);
            }
        }
        return response;
    }
    
    /**
     * @param remoteServerHostAsString
     *            the server from which to import / the exporting server
     * @param dedicatedServerHostAsString
     *            the server where to import to
     * @param leaderboardGroupIds
     *            leaderboardgroup UUIDs to import
     * @param remoteServerBearerToken
     *            authentication towards the exporting server
     * @param dedicatedServerBearerToken
     *            authentication towards the importing server
     * @param override
     *            whether to override existing content in the importing server
     */
    private Util.Pair<JSONObject, Number> doMDI(String remoteServerHostAsString, String dedicatedServerHostAsString,
            Set<String> leaderboardGroupIds, String remoteServerBearerToken, String dedicatedServerBearerToken, boolean override)
            throws Exception {
        final StringJoiner form = new StringJoiner("&");
        form.add(MasterDataImportResource.REMOTE_SERVER_URL_FORM_PARAM + "=" + remoteServerHostAsString);
        form.add(MasterDataImportResource.OVERRIDE_FORM_PARAM + "=" + override);
        form.add(MasterDataImportResource.COMPRESS_FORM_PARAM + "=true");
        form.add(MasterDataImportResource.EXPORT_WIND_FORM_PARAM + "=true");
        form.add(MasterDataImportResource.EXPORT_DEVICE_CONFIGS_FORM_PARAM + "=false");
        form.add(MasterDataImportResource.EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM + "=true");
        form.add(MasterDataImportResource.REMOTE_SERVER_BEARER_TOKEN_FORM_PARAM + "=" + URLEncoder.encode(remoteServerBearerToken, "utf-8"));
        form.add(addLeaderboardGroupIdsToStringJoiner(MasterDataImportResource.LEADERBOARDGROUP_UUID_FORM_PARAM, leaderboardGroupIds).toString());
        return postFormAndReturnJsonAndResponseCode(dedicatedServerHostAsString, dedicatedServerBearerToken, MDI_PATH, form);
    }

    /**
     * @return a pair whose {@link Pair#getA() first component} holds the parsed response as a JSON object, and whose
     *         {@link Pair#getB() second element} is the HTTP response code
     */
    private Pair<JSONObject, Number> postFormAndReturnJsonAndResponseCode(String serverHost, String serverToken,
            String serverPath, final StringJoiner formBody) throws IOException, Exception {
        final byte[] out = formBody.toString().getBytes(StandardCharsets.UTF_8);
        final int length = out.length;
        final URL url = RemoteServerUtil.createRemoteServerUrl(RemoteServerUtil.createBaseUrl(serverHost), serverPath, null);
        final String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
        final Consumer<URLConnection> preConnectModifier = conn->{
            ((HttpURLConnection) conn).setFixedLengthStreamingMode(length);
        };
        final Consumer<URLConnection> postConnectModifier = conn->{
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
                os.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Problem writing form body", e);
            }
        };
        final HttpURLConnection urlConnection = (HttpURLConnection) HttpUrlConnectionHelper.redirectConnectionWithBearerToken(
                url, /* timeout */ Duration.ONE_MINUTE, "POST", serverToken, contentType, preConnectModifier, postConnectModifier,
                /* output stream consumer */ Optional.empty());
        final JSONObject json = parseInputStreamToJsonAndLog(urlConnection);
        return new Util.Pair<>(json, urlConnection.getResponseCode());
    }

    private Util.Pair<JSONObject, Number> doCompareServers(String server1, String server2, String bearer1,
            String bearer2, Set<String> leaderboardGroupIds) throws Exception {
        final StringJoiner form = new StringJoiner("&");
        form.add(CompareServersResource.SERVER1_FORM_PARAM + "=" + server1);
        form.add(CompareServersResource.SERVER2_FORM_PARAM + "=" + server2);
        form.add(CompareServersResource.BEARER1_FORM_PARAM + "=" + URLEncoder.encode(bearer1, "utf-8"));
        form.add(CompareServersResource.BEARER2_FORM_PARAM + "=" + URLEncoder.encode(bearer2, "utf-8"));
        form.add(addLeaderboardGroupIdsToStringJoiner(CompareServersResource.LEADERBOARDGROUP_UUID_FORM_PARAM, leaderboardGroupIds).toString());
        return postFormAndReturnJsonAndResponseCode(server2, bearer1, COMPARESERVERS_PATH, form);
    }

    private Util.Pair<JSONObject, Number> doRemoteServerReferenceAdd(String serverToAddTo, String serverToBeAdded,
            String serverToAddToToken) throws Exception {
        final StringJoiner form = new StringJoiner("&");
        form.add(RemoteServerReferenceResource.REMOTE_SERVER_URL + "=" + serverToBeAdded);
        form.add(RemoteServerReferenceResource.REMOTE_SERVER_NAME + "=" + serverToBeAdded);
        return postFormAndReturnJsonAndResponseCode(serverToAddTo, serverToAddToToken, REMOTESERVERREFERENCEADD_PATH, form);

    }

    private Util.Pair<JSONObject, Number> doRemoteServerReferenceRemove(String serverFromWhichToDelete,
            String serverNameToDelete, String serverFromWhichToDeleteBearerToken) throws Exception {
        final StringJoiner form = new StringJoiner("&");
        form.add(RemoteServerReferenceResource.REMOTE_SERVER_NAME + "=" + serverNameToDelete);
        return postFormAndReturnJsonAndResponseCode(serverFromWhichToDelete,
                serverFromWhichToDeleteBearerToken, REMOTESERVERREFERENCEDELETE_PATH, form);
    }

    private JSONObject parseInputStreamToJsonAndLog(HttpURLConnection connection) throws Exception {
        final JSONParser parser = new JSONParser();
        final JSONObject json = (JSONObject) parser.parse(new InputStreamReader(
                connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST ?
                        connection.getInputStream() :
                        connection.getErrorStream(), "UTF-8"));
        logger.info(connection.getURL().toString() + " returned: " + json.toString());
        return json;
    }

    private StringJoiner addLeaderboardGroupIdsToStringJoiner(String parameterName, Set<String> leaderboardGroupIds) {
        final StringJoiner form = new StringJoiner("&");
        for (String uuid : leaderboardGroupIds) {
            form.add(parameterName + "=" + uuid);
        }
        return form;
    }
}
