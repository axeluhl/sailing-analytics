package com.sap.sailing.landscape.gateway.jaxrs.api;

import java.text.SimpleDateFormat;
import java.util.Optional;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.landscape.AwsSessionCredentialsWithExpiry;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.gateway.impl.AwsApplicationProcessJsonSerializer;
import com.sap.sailing.landscape.gateway.impl.AwsApplicationReplicaSetJsonSerializer;
import com.sap.sailing.landscape.gateway.impl.HostJsonSerializer;
import com.sap.sailing.landscape.gateway.jaxrs.AbstractLandscapeResource;
import com.sap.sailing.landscape.procedures.SailingAnalyticsHostSupplier;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.MongoUriParser;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.mongodb.MongoEndpoint;

@Path("/landscape")
public class SailingLandscapeResource extends AbstractLandscapeResource {
    private static final Logger logger = Logger.getLogger(SailingLandscapeResource.class.getName());

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
    private static final String TIMEOUT_IN_MILLISECONDS_FORM_PARAM = "timeoutInMilliseconds";
    private static final String REMOVE_APPLICATION_REPLICA_SET_FORM_PARAM = "removeApplicationReplicaSet";
    private static final String MAX_NUMBER_OF_COMPARE_SERVER_ATTEMPTS_FORM_PARAM = "maxNumberOfCompareserverAttempts";
    private static final String DURATION_TO_WAIT_BEFORE_COMPARE_SERVERS_IN_MILLISECONDS_FORM_PARAM = "durationToWaitBeforeCompareServersInMilliseconds";
    private static final String UUID_FOR_MDI_PROGRESS = "uuidForMdiProgress";
    private static final String BEARER_TOKEN_FOR_REPLICA_SET_TO_ARCHIVE_FORM_PARAM = "bearerTokenForReplicaSetToArchive";
    private static final String BEARER_TOKEN_FOR_ARCHIVE_FORM_PARAM = "bearerTokenForArchive";
    private static final String MONGO_URI_TO_ARCHIVE_DB_TO_FORM_PARAM = "mongoUriToArchiveDbTo";
    private static final String AWS_KEY_ID_FORM_PARAM = "awsKeyId";
    private static final String AWS_KEY_SECRET_FORM_PARAM = "awsKeySecret";
    private static final String AWS_MFA_TOKEN_FORM_PARAM = "awsMfaToken";
    private static final String SESSION_TOKEN_EXPIRY_UNIX_TIME_MILLIS = "sessionTokenExpiryMillis";
    private static final String SESSION_TOKEN_EXPIRY_ISO = "sessionTokenExpiryISO";
    private static final String RESPONSE_STATUS = "status";
    private static final String RESPONSE_ERROR_MESSAGE = "message";
    private static final String RESPONSE_RELEASE = "release";
    private static final String HOST_ID_FORM_PARAM = "hostId";

    @Context
    UriInfo uriInfo;
    
    @Path("/createsessioncredentials")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response createSessionCredentials(
            @FormParam(AWS_KEY_ID_FORM_PARAM) String awsKeyId,
            @FormParam(AWS_KEY_SECRET_FORM_PARAM) String awsKeySecret,
            @FormParam(AWS_MFA_TOKEN_FORM_PARAM) String mfaToken
            ) {
        checkLandscapeManageAwsPermission();
        Response response;
        try {
            getLandscapeService().createMfaSessionCredentials(awsKeyId, awsKeySecret, mfaToken);
            response = Response.ok().build();
        } catch (Exception e) {
            response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return response;
    }
    
    @Path("/getsessioncredentials")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getSessionCredentials() {
        checkLandscapeManageAwsPermission();
        Response response;
        try {
            final AwsSessionCredentialsWithExpiry sessionCredentials = getLandscapeService().getSessionCredentials();
            if (sessionCredentials == null) {
                response = Response.status(Status.NOT_FOUND).entity("No session credentials found for user "+getSecurityService().getCurrentUser().getName()).build();
            } else {
                final JSONObject result = new JSONObject();
                result.put(SESSION_TOKEN_EXPIRY_UNIX_TIME_MILLIS, sessionCredentials.getExpiration().asMillis());
                result.put(SESSION_TOKEN_EXPIRY_ISO, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(sessionCredentials.getExpiration().asDate()));
                response = Response.ok().entity(streamingOutput(result)).build();
            }
        } catch (Exception e) {
            response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return response;
    }
    
    @Path("/clearsessioncredentials")
    @POST
    @Produces("application/json;charset=UTF-8")
    public Response clearSessionCredentials() {
        checkLandscapeManageAwsPermission();
        Response response;
        try {
            getLandscapeService().clearSessionCredentials();
            response = Response.ok().build();
        } catch (Exception e) {
            response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return response;
    }
    
    @Path("/createapplicationreplicaset")
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
        checkLandscapeManageAwsPermission();
        Response response;
        try {
            final Release release = getLandscapeService().getRelease(releaseNameOrNullForLatestMaster);
            final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet = getLandscapeService()
                    .createApplicationReplicaSet(regionId, replicaSetName, masterInstanceType, dynamicLoadBalancerMapping,
                            release.getName(), optionalKeyName,
                            privateKeyEncryptionPassphrase == null ? null : privateKeyEncryptionPassphrase.getBytes(),
                            masterReplicationBearerToken, replicaReplicationBearerToken, domainName,
                            optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
            final JSONObject result = new AwsApplicationReplicaSetJsonSerializer(release.getName()).serialize(replicaSet);
            response = Response.ok(streamingOutput(result)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception trying to create application replica set "+replicaSetName+" on behalf of user "+getSecurityService().getCurrentUser(), e);
            response = badRequest(e.getMessage());
        }
        return response;
    }
    
    @Path("/upgradeapplicationreplicaset")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response upgradeApplicationReplicaSet(
            @FormParam(REGION_FORM_PARAM) String regionId,
            @FormParam(REPLICA_SET_NAME_FORM_PARAM) String replicaSetName,
            @FormParam(RELEASE_NAME_FORM_PARAM) String releaseNameOrNullForLatestMaster,
            @FormParam(KEY_NAME_FORM_PARAM) String optionalKeyName,
            @FormParam(PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM) String privateKeyEncryptionPassphrase,
            @FormParam(REPLICA_REPLICATION_BEARER_TOKEN_FORM_PARAM) String replicaReplicationBearerToken,
            @FormParam(TIMEOUT_IN_MILLISECONDS_FORM_PARAM) Long optionalTimeoutInMilliseconds) {
        checkLandscapeManageAwsPermission();
        ResponseBuilder responseBuilder;
        final JSONObject result = new JSONObject();
        final AwsRegion region = new AwsRegion(regionId);
        byte[] passphraseForPrivateKeyDecryption = privateKeyEncryptionPassphrase==null?null:privateKeyEncryptionPassphrase.getBytes();
        AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet;
        try {
            replicaSet = getLandscapeService().getApplicationReplicaSet(region, replicaSetName, optionalTimeoutInMilliseconds, optionalKeyName, passphraseForPrivateKeyDecryption);
            final Release release = getLandscapeService().upgradeApplicationReplicaSet(region, replicaSet,
                    releaseNameOrNullForLatestMaster, optionalKeyName, passphraseForPrivateKeyDecryption,
                    replicaReplicationBearerToken);
            responseBuilder = Response.ok();
            result.put(RESPONSE_STATUS, "OK");
            result.put(RESPONSE_RELEASE, release.getName());
        } catch (Exception e) {
            result.put(RESPONSE_STATUS, "ERROR");
            result.put(RESPONSE_ERROR_MESSAGE, e.getMessage());
            responseBuilder = Response.serverError();
        }
        return responseBuilder.entity(streamingOutput(result)).build();
    }    
    
    @Path("/movetoarchiveserver")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response moveToArchiveServer(
            @FormParam(REGION_FORM_PARAM) String regionId,
            @FormParam(REPLICA_SET_NAME_FORM_PARAM) String replicaSetName,
            @FormParam(KEY_NAME_FORM_PARAM) String optionalKeyName,
            @FormParam(PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM) String privateKeyEncryptionPassphrase,
            @FormParam(BEARER_TOKEN_FOR_REPLICA_SET_TO_ARCHIVE_FORM_PARAM) String bearerTokenOrNullForApplicationReplicaSetToArchive,
            @FormParam(BEARER_TOKEN_FOR_ARCHIVE_FORM_PARAM) String bearerTokenOrNullForArchive,
            @FormParam(TIMEOUT_IN_MILLISECONDS_FORM_PARAM) Long optionalTimeoutInMilliseconds,
            @FormParam(DURATION_TO_WAIT_BEFORE_COMPARE_SERVERS_IN_MILLISECONDS_FORM_PARAM) @DefaultValue("300000") long durationToWaitBeforeCompareServersInMillis,
            @FormParam(MAX_NUMBER_OF_COMPARE_SERVER_ATTEMPTS_FORM_PARAM) @DefaultValue("5") int maxNumberOfCompareServerAttempts,
            @FormParam(REMOVE_APPLICATION_REPLICA_SET_FORM_PARAM) @DefaultValue("true") boolean removeApplicationReplicaSet,
            @FormParam(MONGO_URI_TO_ARCHIVE_DB_TO_FORM_PARAM) String mongoUriToArchiveDbTo) {
        checkLandscapeManageAwsPermission();
        Response response;
        final AwsRegion region = new AwsRegion(regionId);
        byte[] passphraseForPrivateKeyDecryption = privateKeyEncryptionPassphrase==null?null:privateKeyEncryptionPassphrase.getBytes();
        final AwsLandscape<String> landscape = getLandscapeService().getLandscape();
        final MongoUriParser<String> mongoUriParser = new MongoUriParser<>(landscape, region);
        try {
            final MongoEndpoint moveDatabaseHere = mongoUriToArchiveDbTo == null ? null : mongoUriParser.parseMongoUri(mongoUriToArchiveDbTo).getEndpoint();
            final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSetToArchive = getLandscapeService()
                    .getApplicationReplicaSet(region, replicaSetName, optionalTimeoutInMilliseconds, optionalKeyName,
                            passphraseForPrivateKeyDecryption);
            if (applicationReplicaSetToArchive == null) {
                response = badRequest("Application replica set with name " + replicaSetName + " not found in region "+regionId);
            } else {
                final UUID uuidForMdiProgress = getLandscapeService().archiveReplicaSet(regionId, applicationReplicaSetToArchive,
                    bearerTokenOrNullForApplicationReplicaSetToArchive, bearerTokenOrNullForArchive,
                    Duration.ofMillis(durationToWaitBeforeCompareServersInMillis), maxNumberOfCompareServerAttempts, removeApplicationReplicaSet,
                    moveDatabaseHere, optionalKeyName, passphraseForPrivateKeyDecryption);
                final JSONObject result = new JSONObject();
                result.put(UUID_FOR_MDI_PROGRESS, uuidForMdiProgress);
                response = Response.ok(streamingOutput(result)).build();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage(), e);
            response = badRequest("Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage());
        }
        return response;
    }

    @Path("/removeapplicationreplicaset")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response removeApplicationReplicaSet(
            @FormParam(REGION_FORM_PARAM) String regionId,
            @FormParam(REPLICA_SET_NAME_FORM_PARAM) String replicaSetName,
            @FormParam(TIMEOUT_IN_MILLISECONDS_FORM_PARAM) Long optionalTimeoutInMilliseconds,
            @FormParam(KEY_NAME_FORM_PARAM) String optionalKeyName,
            @FormParam(PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM) String privateKeyEncryptionPassphrase) {
        checkLandscapeManageAwsPermission();
        Response response;
        final AwsRegion region = new AwsRegion(regionId);
        byte[] passphraseForPrivateKeyDecryption = privateKeyEncryptionPassphrase==null?null:privateKeyEncryptionPassphrase.getBytes();
        try {
            final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> applicationReplicaSetToRemove = getLandscapeService()
                    .getApplicationReplicaSet(region, replicaSetName, optionalTimeoutInMilliseconds, optionalKeyName,
                            passphraseForPrivateKeyDecryption);
            if (applicationReplicaSetToRemove == null) {
                response = badRequest("Application replica set with name "+replicaSetName+" not found in region "+regionId);
            } else {
                getLandscapeService().removeApplicationReplicaSet(regionId, applicationReplicaSetToRemove, optionalKeyName, passphraseForPrivateKeyDecryption);
                response = Response.ok().build();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage(), e);
            response = badRequest("Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage());
        }
        return response;
    }
    
    @Path("/deployreplicatoexistinghost")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response deployReplicaToExistingHost(
            @FormParam(REGION_FORM_PARAM) String regionId,
            @FormParam(REPLICA_SET_NAME_FORM_PARAM) String replicaSetName,
            @FormParam(HOST_ID_FORM_PARAM) String hostId,
            @FormParam(TIMEOUT_IN_MILLISECONDS_FORM_PARAM) Long optionalTimeoutInMilliseconds,
            @FormParam(KEY_NAME_FORM_PARAM) String optionalKeyName,
            @FormParam(PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM) String privateKeyEncryptionPassphrase,
            @FormParam(REPLICA_REPLICATION_BEARER_TOKEN_FORM_PARAM) String replicaReplicationBearerToken,
            @FormParam(MEMORY_IN_MEGABYTES_FORM_PARAM) Integer optionalMemoryInMegabytesOrNull,
            @FormParam(MEMORY_TOTAL_SIZE_FACTOR_FORM_PARAM) Integer optionalMemoryTotalSizeFactorOrNull) {
        checkLandscapeManageAwsPermission();
        Response response;
        final AwsRegion region = new AwsRegion(regionId);
        byte[] passphraseForPrivateKeyDecryption = privateKeyEncryptionPassphrase==null?null:privateKeyEncryptionPassphrase.getBytes();
        try {
            final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet = getLandscapeService()
                    .getApplicationReplicaSet(region, replicaSetName, optionalTimeoutInMilliseconds, optionalKeyName,
                            passphraseForPrivateKeyDecryption);
            if (replicaSet == null) {
                response = badRequest("Application replica set with name "+replicaSetName+" not found in region "+regionId);
            } else {
                final SailingAnalyticsHost<String> hostToDeployTo = getLandscapeService().getLandscape().getHostByInstanceId(region, hostId, new SailingAnalyticsHostSupplier<>());
                if (replicaSet.isEligibleForDeployment(hostToDeployTo,
                        optionalTimeoutInMilliseconds == null ? Optional.empty()
                                : Optional.of(Duration.ofMillis(optionalTimeoutInMilliseconds)),
                        Optional.ofNullable(optionalKeyName), passphraseForPrivateKeyDecryption)) {
                    final SailingAnalyticsProcess<String> process = getLandscapeService().deployReplicaToExistingHost(replicaSet, hostToDeployTo, optionalKeyName,
                            passphraseForPrivateKeyDecryption, replicaReplicationBearerToken,
                            optionalMemoryInMegabytesOrNull, optionalMemoryTotalSizeFactorOrNull);
                    response = Response.ok().entity(streamingOutput(
                            new AwsApplicationProcessJsonSerializer<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>>()
                                    .serialize(process)))
                            .build();
                } else {
                    response = badRequest("Host " + hostToDeployTo
                            + " is not eligible for deploying a process of application replica set " + replicaSetName
                            + ". Check port and directory.");
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage(), e);
            response = badRequest("Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage());
        }
        return response;
    }

    @Path("/gethostseligibleforreplicasetprocessdeployment")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response getHostsEligibleForReplicaSetProcessDeployment(
            @FormParam(REGION_FORM_PARAM) String regionId,
            @FormParam(REPLICA_SET_NAME_FORM_PARAM) String replicaSetName,
            @FormParam(TIMEOUT_IN_MILLISECONDS_FORM_PARAM) Long optionalTimeoutInMilliseconds,
            @FormParam(KEY_NAME_FORM_PARAM) String optionalKeyName,
            @FormParam(PRIVATE_KEY_ENCRYPTION_PASSPHRASE_FORM_PARAM) String privateKeyEncryptionPassphrase) {
        checkLandscapeManageAwsPermission();
        Response response;
        final AwsRegion region = new AwsRegion(regionId);
        byte[] passphraseForPrivateKeyDecryption = privateKeyEncryptionPassphrase==null?null:privateKeyEncryptionPassphrase.getBytes();
        try {
            final AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet = getLandscapeService()
                    .getApplicationReplicaSet(region, replicaSetName, optionalTimeoutInMilliseconds, optionalKeyName,
                            passphraseForPrivateKeyDecryption);
            if (replicaSet == null) {
                response = badRequest("Application replica set with name "+replicaSetName+" not found in region "+regionId);
            } else {
                final JSONArray result = new JSONArray();
                for (final SailingAnalyticsHost<String> host : getLandscapeService().getEligibleHostsForReplicaSet(region, replicaSet, optionalKeyName, passphraseForPrivateKeyDecryption)) {
                    result.add(new HostJsonSerializer<String>().serialize(host));
                }
                response = Response.ok().entity(streamingOutput(result)).build();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage(), e);
            response = badRequest("Error trying to archive replica set "+replicaSetName+" in region "+regionId+": "+e.getMessage());
        }
        return response;
    }
}
