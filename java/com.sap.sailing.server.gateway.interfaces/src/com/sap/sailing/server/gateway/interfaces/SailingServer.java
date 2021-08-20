package com.sap.sailing.server.gateway.interfaces;

import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.common.DataImportProgress;

/**
 * Represents a remote instance of a server process running the Sailing Analytics and exposes various methods as a
 * convenient Java API which are implemented using the remote server's REST API. In short, this is a Java facade for a
 * REST API.
 * <p>
 * 
 * Objects of this type manage authentication information required for executing its methods as part of their immutable
 * state, therefore at object construction time. Authentication information may be provided in the form of
 * username/password which can then be used to obtain a bearer token for remote access. Or the bearer token for the
 * server can be provided explicitly. Or, as the default, the constructor checks whether running in the scope of an
 * authenticated session and then uses that session user's bearer token, assuming that the server to be represented by
 * this object shares its security service with the server where this object is constructed.
 * <p>
 */
public interface SailingServer {
    URL getBaseUrl();
    
    Iterable<UUID> getLeaderboardGroupIds() throws Exception;

    Iterable<UUID> getEventIds() throws Exception;

    MasterDataImportResult importMasterData(SailingServer from, Iterable<UUID> leaderboardGroupIds, boolean override,
            boolean compress, boolean exportWind, boolean exportDeviceConfigs,
            boolean exportTrackedRacesAndStartTracking, Optional<UUID> progressTrackingUuid) throws Exception;
    
    DataImportProgress getMasterDataImportProgress(UUID progressTrackingUuid) throws Exception;

    /**
     * Compares all {@link #getLeaderboardGroupIds() leaderboard groups} of {@code a} (or this server, if {@code a} is
     * not present) with the corresponding leaderboard groups in {@code b}. The result of the comparison is returned.
     * Note that this way leaderboard groups may exist in {@code b} that don't exist in {@code a} without producing a
     * 
     * @param a
     *            if not present, this server is compared against {@code b}
     * @param leaderboardGroupIds
     *            if present, specifies which leaderboard groups shall be compared; otherwise, all leaderboard groups of
     *            both servers are compared, and those missing on either end will be reported
     */
    CompareServersResult compareServers(Optional<SailingServer> a, SailingServer b, Optional<Iterable<UUID>> leaderboardGroupIds) throws Exception;

    void addRemoteServerReference(SailingServer referencedServer, Optional<Set<UUID>> eventIds) throws Exception;

    void removeRemoteServerReference(SailingServer referencedServer, Optional<Set<UUID>> eventIds) throws Exception;

    String getBearerToken();
}
