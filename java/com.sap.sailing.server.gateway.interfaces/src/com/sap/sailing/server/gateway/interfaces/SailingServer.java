package com.sap.sailing.server.gateway.interfaces;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a remote instance of a server process running the Sailing Analytics and exposes various methods as a
 * convenient Java API which are implemented using the remote server's REST API. In short, this is a Java facade for a
 * REST API.
 * <p>
 * 
 * Objects of this type manage authentication information required for executing its methods as part of their immutable
 * state, therefore at object construction time. Authentication information may be provided in the form of
 * username/password which can then be used to obtain a bearer token for remote access. Or the bearer token for the
 * server can be provided explicitly. Or, as the default, the constructor checks whether running in the scope of
 * an authenticated session and then uses that session user's bearer token, assuming that the server to be represented
 * by this object shares its security service with the server where this object is constructed.<p>
 */
public interface SailingServer {
    Iterable<UUID> getLeaderboardGroupIds() throws Exception;
    Iterable<UUID> getEventIds() throws Exception;
    MasterDataImportResult importMasterData(SailingServer from, Iterable<UUID> leaderboardGroupIds);
    CompareServersResult compareServers(SailingServer a, Optional<SailingServer> b);
    void addRemoteServerReference(SailingServer referencedServer, Optional<Set<UUID>> eventIds);
    void removeRemoteServerReference(SailingServer referencedServer, Optional<Set<UUID>> eventIds);
}
