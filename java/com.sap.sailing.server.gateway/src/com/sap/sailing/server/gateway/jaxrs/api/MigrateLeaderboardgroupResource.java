package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.gateway.deserialization.impl.RemoteSailingServerReferenceJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sailing.server.gateway.serialization.impl.RemoteSailingServerReferenceJsonSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.util.RemoteServerUtil;
import com.sap.sse.util.HttpUrlConnectionHelper;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

@Path ("/v1/scopemigration")
public class MigrateLeaderboardgroupResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(MigrateLeaderboardgroupResource.class.getName());

    private static final String MDI_PATH = "/sailingserver/api/v1/masterdataimport";
    private static final String COMPARESERVERS_PATH = "/sailingserver/api/v1/compareservers";
    private static final String REMOTESERVERREFERENCE_BASE_PATH = "/sailingserver/api/v1/remoteserverreference";
    private static final String REMOTESERVERREFERENCEADD_PATH = REMOTESERVERREFERENCE_BASE_PATH+"/add";
    private static final String REMOTESERVERREFERENCEDELETE_PATH = REMOTESERVERREFERENCE_BASE_PATH+"/remove";
    private static final String REMOTESERVERREFERENCEUPDATE_PATH = REMOTESERVERREFERENCE_BASE_PATH+"/update";
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
    private static final String RESPONSE_CODE = "responseCode";

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
                final Pair<JSONObject, Number> mdi = doMDI(effectiveBaseServer, dedicatedServer, requestedLeaderboardGroups,
                        baseServerBearerToken, dedicatedServerBearerToken, override);
                mdi.getA().put(RESPONSE_CODE, mdi.getB());
                final Pair<JSONObject, Number> compareServers = doCompareServers(effectiveBaseServer, dedicatedServer,
                        dedicatedServerBearerToken, baseServerBearerToken, requestedLeaderboardGroups);
                compareServers.getA().put(RESPONSE_CODE, compareServers.getB());
                // add a remote reference from the base server pointing to the dedicated server
                final Pair<JSONObject, Number> remoteServerReferenceAdd = doRemoteServerReferenceAdd(effectiveBaseServer,
                        dedicatedServer, baseServerBearerToken, getIdsOfImportedEvents(mdi.getA()));
                remoteServerReferenceAdd.getA().put(RESPONSE_CODE, remoteServerReferenceAdd.getB());
                // FIXME bug5311 and now remove the event / the leaderboard groups in the base location, but preserve security/ownerships/ACLs in case of shared security!
                // FIXME bug5311 adjust request routing such that requests targeting the content moved will be routed to the new "dedicatedServer" location
                result.put(MDI, mdi.getA());
                result.put(COMPARE_SERVERS, compareServers.getA());
                result.put(REMOTE_SERVER_REFERENCE_ADD, remoteServerReferenceAdd.getA());
                if (mdi.getB().intValue() != Status.OK.getStatusCode() || compareServers.getB().intValue() != Status.OK.getStatusCode()
                        || remoteServerReferenceAdd.getB().intValue() != Status.OK.getStatusCode()) {
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
     * From the leaderboardgroupsImported[].events[] paths extracts all event IDs that were imported during the master data import.
     */
    private Iterable<UUID> getIdsOfImportedEvents(JSONObject mdiResult) {
        final JSONArray leaderboardgroupsImported = (JSONArray) mdiResult.get(MasterDataImportResource.LEADERBOARDGROUPS_IMPORTED);
        final Set<UUID> result = new HashSet<>();
        for (final Object lg : leaderboardgroupsImported) {
            for (final Object eventIdObject : (JSONArray) ((JSONObject) lg).get(LeaderboardGroupConstants.EVENTS)) {
                result.add(UUID.fromString(eventIdObject.toString()));
            }
        }
        return result;
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
                final Pair<JSONObject, Number> mdi = doMDI(dedicatedServer, archiveServer, requestedLeaderboardGroups,
                        dedicatedServerBearerToken, archiveServerBearerToken, override);
                final Pair<JSONObject, Number> compareServers = doCompareServers(dedicatedServer, archiveServer,
                        archiveServerBearerToken, dedicatedServerBearerToken, requestedLeaderboardGroups);
                final Pair<JSONObject, Number> remoteServerReferenceRemove = doRemoteServerReferenceRemove(
                        archiveServer, dedicatedServer, archiveServerBearerToken, getIdsOfImportedEvents(mdi.getA()));
                // FIXME bug5311: update archive server reverse proxy settings for the event(s) imported, then dismantle the dedicated replica set and consider archiving its DB to "slow" if it was on "live", probably controlling dismantling by an optional parameter that defaults to false
                result.put(MDI, mdi.getA());
                result.put(COMPARE_SERVERS, compareServers.getA());
                result.put(REMOTE_SERVER_REFERENCE_REMOVED, remoteServerReferenceRemove.getA());
                if (mdi.getB().intValue() != Status.OK.getStatusCode() || compareServers.getB().intValue() != Status.OK.getStatusCode()
                        || remoteServerReferenceRemove.getB().intValue() != Status.OK.getStatusCode()) {
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
    private Pair<JSONObject, Number> doMDI(String remoteServerHostAsString, String dedicatedServerHostAsString,
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
    private Pair<JSONObject, Number> putFormAndReturnJsonAndResponseCode(String serverHost, String serverToken,
            String serverPath, final StringJoiner formBody) throws IOException, Exception {
        return submitFormAndReturnJsonAndResponseCode("PUT", serverHost, serverToken, serverPath, formBody);
    }

    /**
     * @return a pair whose {@link Pair#getA() first component} holds the parsed response as a JSON object, and whose
     *         {@link Pair#getB() second element} is the HTTP response code
     */
    private Pair<JSONObject, Number> postFormAndReturnJsonAndResponseCode(String serverHost, String serverToken,
            String serverPath, final StringJoiner formBody) throws IOException, Exception {
        return submitFormAndReturnJsonAndResponseCode("POST", serverHost, serverToken, serverPath, formBody);
    }
    
    /**
     * @return a pair whose {@link Pair#getA() first component} holds the parsed response as a JSON object, and whose
     *         {@link Pair#getB() second element} is the HTTP response code
     */
    private Pair<JSONObject, Number> submitFormAndReturnJsonAndResponseCode(String httpMethod, String serverHost, String serverToken,
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
                url, /* timeout */ Duration.ONE_MINUTE, httpMethod, serverToken, contentType, preConnectModifier, postConnectModifier,
                /* output stream consumer */ Optional.empty());
        final JSONObject json = parseInputStreamToJsonAndLog(urlConnection);
        return new Pair<>(json, urlConnection.getResponseCode());
    }

    private Pair<JSONObject, Number> doCompareServers(String server1, String server2, String bearer1,
            String bearer2, Set<String> leaderboardGroupIds) throws Exception {
        final StringJoiner form = new StringJoiner("&");
        form.add(CompareServersResource.SERVER1_FORM_PARAM + "=" + server1);
        form.add(CompareServersResource.SERVER2_FORM_PARAM + "=" + server2);
        form.add(CompareServersResource.BEARER1_FORM_PARAM + "=" + URLEncoder.encode(bearer1, "utf-8"));
        form.add(CompareServersResource.BEARER2_FORM_PARAM + "=" + URLEncoder.encode(bearer2, "utf-8"));
        form.add(addLeaderboardGroupIdsToStringJoiner(CompareServersResource.LEADERBOARDGROUP_UUID_FORM_PARAM, leaderboardGroupIds).toString());
        return postFormAndReturnJsonAndResponseCode(server2, bearer1, COMPARESERVERS_PATH, form);
    }

    private Pair<JSONObject, Number> doRemoteServerReferenceAdd(String serverToAddTo, String serverUrlToBeAdded,
            String serverToAddToBearerToken, Iterable<UUID> eventIds) throws Exception {
        final Pair<JSONObject, Number> result;
        final RemoteSailingServerReference existingRef = getExistingRemoteServerReference(serverToAddTo, serverToAddToBearerToken, serverUrlToBeAdded);
        if (existingRef == null) {
            final StringJoiner form = new StringJoiner("&");
            form.add(RemoteServerReferenceResource.REMOTE_SERVER_URL + "=" + serverUrlToBeAdded);
            form.add(RemoteServerReferenceResource.REMOTE_SERVER_NAME + "=" + serverUrlToBeAdded);
            result = postFormAndReturnJsonAndResponseCode(serverToAddTo, serverToAddToBearerToken, REMOTESERVERREFERENCEADD_PATH, form);
        } else {
            if (existingRef.isInclude()) {
                final Set<UUID> eventsToAdd = new HashSet<>();
                Util.addAll(eventIds, eventsToAdd);
                Util.removeAll(existingRef.getSelectedEventIds(), eventsToAdd);
                if (!eventsToAdd.isEmpty()) {
                    eventsToAdd.addAll(existingRef.getSelectedEventIds());
                    existingRef.updateSelectedEventIds(eventsToAdd);
                    result = doRemoteServerReferenceUpdate(serverToAddTo, serverToAddToBearerToken, existingRef);
                } else {
                    // nothing to do, all events migrated are already referenced by the inclusive reference
                    result = new Pair<>(new RemoteSailingServerReferenceJsonSerializer().serialize(existingRef), Status.OK.getStatusCode());
                }
            } else {
                // exclusive; are any of those events we migrated excluded explicitly? If so, "un-exclude":
                final Set<UUID> eventsToUnexclude = new HashSet<>();
                Util.addAll(eventIds, eventsToUnexclude);
                Util.retainAll(existingRef.getSelectedEventIds(), eventsToUnexclude);
                if (!eventsToUnexclude.isEmpty()) {
                    final Set<UUID> eventsToExclude = new HashSet<>(existingRef.getSelectedEventIds());
                    Util.removeAll(eventIds, eventsToExclude);
                    existingRef.updateSelectedEventIds(eventsToExclude);
                    result = doRemoteServerReferenceUpdate(serverToAddTo, serverToAddToBearerToken, existingRef);
                } else {
                    // nothing to do, all events migrated are already referenced by the inclusive reference
                    result = new Pair<>(new RemoteSailingServerReferenceJsonSerializer().serialize(existingRef), Status.OK.getStatusCode());
                }
            }
        }
        return result;
    }
    
    private Pair<JSONObject, Number> doRemoteServerReferenceUpdate(String serverOnWhichToUpdateRemoteReference,
            String bearerToken, RemoteSailingServerReference updatedRef) throws IOException, Exception {
        final StringJoiner form = new StringJoiner("&");
        form.add(RemoteServerReferenceResource.REMOTE_SERVER_NAME + "=" + updatedRef.getName());
        form.add(RemoteServerReferenceResource.REMOTE_SERVER_IS_INCLUDE + "=" + updatedRef.isInclude());
        for (final UUID eventId : updatedRef.getSelectedEventIds()) {
            form.add(RemoteServerReferenceResource.REMOTE_SERVER_EVENT_IDS + "=" + eventId);
        }
        return putFormAndReturnJsonAndResponseCode(serverOnWhichToUpdateRemoteReference, bearerToken, REMOTESERVERREFERENCEUPDATE_PATH, form);
    }

    private RemoteSailingServerReference getExistingRemoteServerReference(final String serverUrlFromWhereToFetch,
            final String bearerToken, final String remoteReferenceServerUrl) throws MalformedURLException, Exception {
        final HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes()).build();
        final URL getRemoteReferencesUrl = RemoteServerUtil.createRemoteServerUrl(RemoteServerUtil.createBaseUrl(serverUrlFromWhereToFetch), REMOTESERVERREFERENCE_BASE_PATH, null);
        final HttpGet getRequest = new HttpGet(getRemoteReferencesUrl.toString());
        getRequest.addHeader("Authorization", "Bearer "+bearerToken);
        final HttpResponse response = client.execute(getRequest);
        final InputStream inputStream = response.getEntity().getContent();
        final JSONArray remoteServerReferencesJson = (JSONArray) new JSONParser().parse(new InputStreamReader(inputStream));
        final URL remoteReferenceServerUrlAsUrl = new URL(remoteReferenceServerUrl);
        for (final Object o : remoteServerReferencesJson) {
            final RemoteSailingServerReference ref = new RemoteSailingServerReferenceJsonDeserializer().deserialize((JSONObject) o);
            if (ref.getURL().equals(remoteReferenceServerUrlAsUrl)) {
                return ref;
            }
        }
        return null;
    }

    private Pair<JSONObject, Number> doRemoteServerReferenceRemove(String serverFromWhichToDelete,
            String serverUrlToDelete, String serverFromWhichToDeleteBearerToken, Iterable<UUID> eventIds)
            throws Exception {
        final RemoteSailingServerReference existingRef = getExistingRemoteServerReference(serverFromWhichToDelete, serverFromWhichToDeleteBearerToken, serverUrlToDelete);
        final Pair<JSONObject, Number> result;
        if (existingRef != null) {
            if (existingRef.isInclude()) {
                final Set<UUID> eventIdsToKeep = new HashSet<>(existingRef.getSelectedEventIds());
                Util.removeAll(eventIds, eventIdsToKeep);
                if (eventIdsToKeep.isEmpty()) {
                    // remove ref entirely
                    final StringJoiner form = new StringJoiner("&");
                    form.add(RemoteServerReferenceResource.REMOTE_SERVER_NAME + "=" + serverUrlToDelete);
                    result = postFormAndReturnJsonAndResponseCode(serverFromWhichToDelete,
                            serverFromWhichToDeleteBearerToken, REMOTESERVERREFERENCEDELETE_PATH, form);
                } else {
                    existingRef.updateSelectedEventIds(eventIdsToKeep);
                    result = doRemoteServerReferenceUpdate(serverFromWhichToDelete, serverFromWhichToDeleteBearerToken, existingRef);
                }
            } else {
                // check if more events now need to be excluded
                if (!Util.containsAll(existingRef.getSelectedEventIds(), eventIds)) {
                    final Set<UUID> extendedEventIdsToExclude = new HashSet<>(existingRef.getSelectedEventIds());
                    Util.addAll(eventIds, extendedEventIdsToExclude);
                    existingRef.updateSelectedEventIds(extendedEventIdsToExclude);
                    result = doRemoteServerReferenceUpdate(serverFromWhichToDelete, serverFromWhichToDeleteBearerToken, existingRef);
                } else {
                    // everything that needs to be excluded is already excluded; nothing more to do
                    result = new Pair<>(new RemoteSailingServerReferenceJsonSerializer().serialize(existingRef), Status.OK.getStatusCode());
                }
            }
        } else {
            // no reference to serverUrlToDelete found, therefore nothing to delete
            result = new Pair<>(new RemoteSailingServerReferenceJsonSerializer().serialize(existingRef), Status.OK.getStatusCode());
        }
        return result;
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