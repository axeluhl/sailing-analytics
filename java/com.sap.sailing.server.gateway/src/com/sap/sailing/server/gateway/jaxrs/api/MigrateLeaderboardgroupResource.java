package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.gateway.interfaces.CompareServersResult;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sailing.server.gateway.interfaces.SailingServerFactory;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.CompareServersResultJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MasterDataImportResultJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RemoteSailingServerReferenceJsonSerializer;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Util;
import com.sap.sse.security.util.RemoteServerUtil;

@Path ("/v1/scopemigration")
public class MigrateLeaderboardgroupResource extends AbstractSailingServerResource {

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
            @FormParam(OVERRIDE) @DefaultValue("false") boolean override) { // TODO parameters should be added to control MDI waiting
        Response response;
        final String effectiveBaseServer = !Util.hasLength(baseServer) ? uriInfo.getBaseUri().getAuthority() : baseServer;
        final SailingServerFactory sailingServerFactory = getSailingServerFactory();
        if (sailingServerFactory == null) {
            throw new IllegalStateException("Couldn't find SailingServerFactory");
        }
        if (!validateAuthenticationParameters(user1, password1, bearer1)) {
            response = badRequest("Specify "+USER1_FORM_PARAM+" and "+PASSWORD1_FORM_PARAM+" or alternatively "+BEARER1_FORM_PARAM+" or none of them.");
        } else if (!validateAuthenticationParameters(user2, password2, bearer2)) {
            response = badRequest("Specify "+USER2_FORM_PARAM+" and "+PASSWORD2_FORM_PARAM+" or alternatively "+BEARER2_FORM_PARAM+" or none of them.");
        } else if (!Util.hasLength(dedicatedServer)) {
            response = badRequest("A target server must be specified with the "+SERVER2_FORM_PARAM+" parameter");
        } else {
            final String baseServerBearerToken = getSecurityService().getOrCreateTargetServerBearerToken(effectiveBaseServer, user1, password1, bearer1);
            final String dedicatedServerBearerToken = getSecurityService().getOrCreateTargetServerBearerToken(dedicatedServer, user2, password2, bearer2);
            try {
                final JSONObject result = new JSONObject();
                final SailingServer baseSailingServer = getSailingServerFactory().getSailingServer(RemoteServerUtil.createBaseUrl(effectiveBaseServer), baseServerBearerToken);
                final SailingServer dedicatedSailingServer = getSailingServerFactory().getSailingServer(RemoteServerUtil.createBaseUrl(dedicatedServer), dedicatedServerBearerToken);
                final UUID mdiProgressTrackingUuid = UUID.randomUUID();
                final MasterDataImportResult mdiResult = dedicatedSailingServer.importMasterData(baseSailingServer, Util.map(requestedLeaderboardGroups, UUID::fromString), override,
                        /* compress */ true, /* exportWind */ true, /* exportDeviceConfigs */ true, /* exportTrackedRacesAndStartTracking */ true,
                        Optional.of(mdiProgressTrackingUuid));
                final JSONObject mdiResultAsJson;
                final JSONObject compareServersResultAsJson;
                final CompareServersResult compareServersResult;
                final RemoteSailingServerReference remoteSailingServerReference;
                final JSONObject addRemoteSailingServerReferenceResultAsJson;
                if (mdiResult != null) {
                    mdiResultAsJson = new MasterDataImportResultJsonSerializer().serialize(mdiResult);
                    mdiResultAsJson.put(RESPONSE_CODE, Status.OK.getStatusCode());
                    compareServersResult = baseSailingServer.compareServers(
                            /* baseSailingServer */ Optional.empty(), dedicatedSailingServer, Optional.of(Util.map(mdiResult.getLeaderboardGroupsImported(), NamedWithUUID::getId)));
                    if (compareServersResult != null) {
                        compareServersResultAsJson = new CompareServersResultJsonSerializer().serialize(compareServersResult);
                        compareServersResultAsJson.put(RESPONSE_CODE, compareServersResult.hasDiffs() ? Status.CONFLICT.getStatusCode() : Status.OK.getStatusCode());
                        // add a remote reference from the base server pointing to the dedicated server
                        // TODO shouldn't adding the reference happen only if comparing servers went ok?
                        remoteSailingServerReference = baseSailingServer.addRemoteServerEventReferences(dedicatedSailingServer, mdiResult.getEventIdsImported());
                        if (remoteSailingServerReference != null) {
                            addRemoteSailingServerReferenceResultAsJson = new RemoteSailingServerReferenceJsonSerializer().serialize(remoteSailingServerReference);
                            addRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.OK.getStatusCode());
                        } else {
                            addRemoteSailingServerReferenceResultAsJson = new JSONObject();
                            addRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                        }
                    } else {
                        compareServersResultAsJson = new JSONObject();
                        compareServersResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                        remoteSailingServerReference = null;
                        addRemoteSailingServerReferenceResultAsJson = new JSONObject();
                        addRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                } else {
                    mdiResultAsJson = new JSONObject();
                    mdiResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    compareServersResult = null;
                    compareServersResultAsJson = new JSONObject();
                    compareServersResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    remoteSailingServerReference = null;
                    addRemoteSailingServerReferenceResultAsJson = new JSONObject();
                    addRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                }
                // FIXME bug5311 and now remove the event / the leaderboard groups in the base location, but preserve security/ownerships/ACLs in case of shared security! Maybe remember ownerships/ACLs before deleting, then re-establish after deleting...
                // FIXME bug5311 adjust request routing such that requests targeting the content moved will be routed to the new "dedicatedServer" location
                result.put(MDI, mdiResultAsJson);
                result.put(COMPARE_SERVERS, compareServersResultAsJson);
                result.put(REMOTE_SERVER_REFERENCE_ADD, addRemoteSailingServerReferenceResultAsJson);
                if (mdiResult == null || compareServersResult == null || remoteSailingServerReference == null) {
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
            final String archiveServerBearerToken = getSecurityService().getOrCreateTargetServerBearerToken(
                    archiveServer, user1, password1, bearer1);
            final String dedicatedServerBearerToken = getSecurityService().getOrCreateTargetServerBearerToken(
                    dedicatedServer, user2, password2, bearer2);
            try {
                final JSONObject result = new JSONObject();
                final SailingServer dedicatedSailingServer = getSailingServerFactory().getSailingServer(RemoteServerUtil.createBaseUrl(dedicatedServer), dedicatedServerBearerToken);
                final SailingServer archiveSailingServer = getSailingServerFactory().getSailingServer(RemoteServerUtil.createBaseUrl(archiveServer), archiveServerBearerToken);
                final UUID mdiProgressTrackingUuid = UUID.randomUUID();
                final MasterDataImportResult mdiResult = archiveSailingServer.importMasterData(dedicatedSailingServer, Util.map(requestedLeaderboardGroups, UUID::fromString), override,
                        /* compress */ true, /* exportWind */ true, /* exportDeviceConfigs */ true, /* exportTrackedRacesAndStartTracking */ true,
                        Optional.of(mdiProgressTrackingUuid));
                final JSONObject mdiResultAsJson;
                final JSONObject compareServersResultAsJson;
                final CompareServersResult compareServersResult;
                final RemoteSailingServerReference remoteSailingServerReference;
                final JSONObject removeRemoteSailingServerReferenceResultAsJson;
                if (mdiResult != null) {
                    mdiResultAsJson = new MasterDataImportResultJsonSerializer().serialize(mdiResult);
                    mdiResultAsJson.put(RESPONSE_CODE, Status.OK.getStatusCode());
                    compareServersResult = archiveSailingServer.compareServers(
                            /* archiveSailingServer */ Optional.empty(), dedicatedSailingServer, Optional.of(Util.map(mdiResult.getLeaderboardGroupsImported(), NamedWithUUID::getId)));
                    if (compareServersResult != null) {
                        compareServersResultAsJson = new CompareServersResultJsonSerializer().serialize(compareServersResult);
                        compareServersResultAsJson.put(RESPONSE_CODE, compareServersResult.hasDiffs() ? Status.CONFLICT.getStatusCode() : Status.OK.getStatusCode());
                        // TODO shouldn't the reference removal happen only if comparing servers went ok?
                        // add a remote reference from the base server pointing to the dedicated server
                        remoteSailingServerReference = archiveSailingServer.removeRemoteServerEventReferences(dedicatedSailingServer, mdiResult.getEventIdsImported());
                        removeRemoteSailingServerReferenceResultAsJson = remoteSailingServerReference == null ? new JSONObject() : new RemoteSailingServerReferenceJsonSerializer().serialize(remoteSailingServerReference);
                        removeRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.OK.getStatusCode());
                    } else {
                        compareServersResultAsJson = new JSONObject();
                        compareServersResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                        remoteSailingServerReference = null;
                        removeRemoteSailingServerReferenceResultAsJson = new JSONObject();
                        removeRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                } else {
                    mdiResultAsJson = new JSONObject();
                    mdiResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    compareServersResult = null;
                    compareServersResultAsJson = new JSONObject();
                    compareServersResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    remoteSailingServerReference = null;
                    removeRemoteSailingServerReferenceResultAsJson = new JSONObject();
                    removeRemoteSailingServerReferenceResultAsJson.put(RESPONSE_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
                }
                // FIXME bug5311: update archive server reverse proxy settings for the event(s) imported, then dismantle the dedicated replica set and consider archiving its DB to "slow" if it was on "live", probably controlling dismantling by an optional parameter that defaults to false; see LandscapeManagementWriteServiceImpl.archiveReplicaSet
                result.put(MDI, mdiResultAsJson);
                result.put(COMPARE_SERVERS, compareServersResultAsJson);
                result.put(REMOTE_SERVER_REFERENCE_REMOVED, removeRemoteSailingServerReferenceResultAsJson);
                if (mdiResult == null || compareServersResult == null) {
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
}