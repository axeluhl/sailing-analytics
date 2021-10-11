package com.sap.sailing.server.gateway.interfaces;

import java.net.URL;

/**
 * A factory service that obtains {@link SailingServer} instances which represent (usually remote) server processes,
 * identified by a {@link URL} that is used as the base to construct the REST API URLs, plus the authentication
 * information necessary to authenticate calls.
 * <p>
 * 
 * Using {@link #getSailingServer(URL)} may be used to try authentication with the bearer token of the current local
 * user session, if any. If no such session exists, the {@link SailingServer} returned will make its REST calls without
 * any form of authentication, restricting the services that can be invoked to those that are publicly accessible.
 * <p>
 * 
 * The "base URL" needs to contain protocol, host and port specification and an empty path or only "/" as path.
 * <p>
 * 
 * The intended use is through the OSGi service registry: clients are expected to discover and track an implementation
 * of this interface and then use that to obtain an anonymous or authenticated {@link SailingServer} instance.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SailingServerFactory {
    /**
     * Looks for a local authenticated security session; if found, that session's bearer token will be used to
     * authenticate calls to the resulting {@link SailingServer}, assuming it shares a security service with
     * the local server that executes this method. If no local authenticated session is found, calls to the
     * {@link SailingServer} returned will be anonymous and hence be restricted to those publicly available.
     */
    SailingServer getSailingServer(URL baseUrl);
    
    /**
     * Explicitly authenticates calls to the resulting {@link SailingServer} using the {@code bearerToken} provided. If
     * the {@code bearerToken} is {@code null} then this method does the same as {@link #getSailingServer(URL)}, namely
     * looking for a current authenticated session and then using its bearer token or, if not found, continue
     * anonymously.
     */
    SailingServer getSailingServer(URL baseUrl, String bearerToken);
    
    /**
     * Authenticates calls to the resulting {@link SailingServer} by obtaining a bearer token from that server
     * through basic authentication with the {@code username} and {@code password} provided.
     */
    SailingServer getSailingServer(URL baseUrl, String username, String password);
}
