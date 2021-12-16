package com.sap.sailing.landscape.gateway.jaxrs.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.landscape.LandscapeService;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.gateway.impl.AwsApplicationReplicaSetJsonSerializer;
import com.sap.sailing.landscape.gateway.jaxrs.AbstractLandscapeResource;
import com.sap.sailing.server.gateway.interfaces.CompareServersResult;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sailing.server.gateway.serialization.impl.CompareServersResultJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MasterDataImportResultJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RemoteSailingServerReferenceJsonSerializer;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.security.util.RemoteServerUtil;

@Path("/landscape")
public class SailingLandscapeResource extends AbstractLandscapeResource {
    private static final Logger logger = Logger.getLogger(SailingLandscapeResource.class.getName());

    private static final String REMOTE_SERVER_REFERENCE_REMOVED = "remoteServerReferenceRemoved";
    private static final String COMPARE_SERVERS = "compareServers";
    private static final String MDI = "MDI";
    private static final String REMOTE_SERVER_REFERENCE_ADD = "remoteServerReferenceAdd";
    private static final String DEDICATED_SERVER_FORM_PARAM = "dedicated";
    private static final String ARCHIVE_SERVER_FORM_PARAM = "archive";
    private static final String SERVER1_FORM_PARAM = "server1";
    private static final String SERVER2_FORM_PARAM = "server2";
    private static final String USER1_FORM_PARAM = "user1";
    private static final String USER2_FORM_PARAM = "user2";
    private static final String PASSWORD1_FORM_PARAM = "password1";
    private static final String PASSWORD2_FORM_PARAM = "password2";
    private static final String BEARER1_FORM_PARAM = "bearer1";
    private static final String BEARER2_FORM_PARAM = "bearer2";
    private static final String LEADERBOARD_GROUPS_UUID_FORM_PARAM = "leaderboardgroupUUID[]";
    private static final String ARCHIVE_SERVER_BASE_URL = "www.sapsailing.com";
    private static final String OVERRIDE = "override";
    private static final String RESPONSE_CODE = "responseCode";
    private static final String REGION_FORM_PARAM = "regionId";
    private static final String REPLICA_SET_NAME_FORM_PARAM = "replicaSetName";
    private static final String MASTER_INSTANCE_TYPE_FORM_PARAM = "masterInstanceType";
    private static final String DYNAMIC_LOAD_BALANCER_MAPPING_FORM_PARAM = "dynamicLoadBalancerMapping";
    private static final String RELEASE_NAME_FORM_PARAM = "releaseName";
    private static final String KEY_NAME_FORM_PARAM = "keyName";
    private static final String PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM = "privateKeyEncryptionPassphrase";
    private static final String MASTER_REPLICATION_BEARER_TOKEN_FORM_PARAM = "masterReplicationBearerToken";
    private static final String REPLICA_REPLICATION_BEARER_TOKEN_FORM_PARAM = "replicaReplicationBearerToken";
    private static final String DOMAIN_NAME_FORM_PARAM = "domainName";
    private static final String MEMORY_IN_MEGABYTES_FORM_PARAM = "memoryInMegabytes";
    private static final String MEMORY_TOTAL_SIZE_FACTOR_FORM_PARAM = "memoryTotalSizeFactor";

    @Context
    UriInfo uriInfo;

    @Path ("/createapplicationreplicaset")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response createApplicationReplicaSet(
            @FormParam(REGION_FORM_PARAM) String regionId,
            @FormParam(REPLICA_SET_NAME_FORM_PARAM) String replicaSetName,
            @FormParam(MASTER_INSTANCE_TYPE_FORM_PARAM) String masterInstanceType,
            @FormParam(DYNAMIC_LOAD_BALANCER_MAPPING_FORM_PARAM) @DefaultValue("false") boolean dynamicLoadBalancerMapping,
            @FormParam(RELEASE_NAME_FORM_PARAM) String releaseNameOrNullForLatestMaster,
            @FormParam(KEY_NAME_FORM_PARAM) String optionalKeyName,
            @FormParam(PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM) String privateKeyEncryptionPassphrase,
            @FormParam(MASTER_REPLICATION_BEARER_TOKEN_FORM_PARAM) String masterReplicationBearerToken,
            @FormParam(REPLICA_REPLICATION_BEARER_TOKEN_FORM_PARAM) String replicaReplicationBearerToken,
            @FormParam(DOMAIN_NAME_FORM_PARAM) String domainName,
            @FormParam(MEMORY_IN_MEGABYTES_FORM_PARAM) Integer optionalMemoryInMegabytesOrNull,
            @FormParam(MEMORY_TOTAL_SIZE_FACTOR_FORM_PARAM) Integer optionalMemoryTotalSizeFactorOrNull) {
        Response response;
        try {
            final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet = getLandscapeService()
                    .createApplicationReplicaSet(regionId, replicaSetName, masterInstanceType, dynamicLoadBalancerMapping,
                            releaseNameOrNullForLatestMaster, optionalKeyName,
                            privateKeyEncryptionPassphrase == null ? null : privateKeyEncryptionPassphrase.getBytes(),
                            masterReplicationBearerToken, replicaReplicationBearerToken, domainName,
                            optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
            final JSONObject result = new AwsApplicationReplicaSetJsonSerializer().serialize(replicaSet);
            response = Response.ok(streamingOutput(result)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception trying to create application replica set "+replicaSetName+" on behalf of user "+getSecurityService().getCurrentUser(), e);
            response = badRequest(e.getMessage());
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

    @GET
    @Produces("application/text;charset=UTF-8")
    public Response getRegressions() throws IOException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                LandscapeService landscapeService = getLandscapeService();
                output.write(landscapeService.helloWorld().getBytes());
            }
        }).header("Content-Type", "application/text").build();
    }
}
