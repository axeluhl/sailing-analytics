package com.sap.sse.landscape;

/**
 * Can be used to authenticate a replica to its master. Authentication may happen by
 * username/password or by a bearer token. These different types of credentials know how
 * to represent themselves as environment variable settings which can, e.g., be passed as
 * user data or be appended to an {@code env.sh} file directly.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReplicationCredentials extends UserDataProvider {
    /**
     * Resolves these replication credentials to a bearer token that can be used to authenticate
     * HTTP requests to the host/port provided. If {@code port} is {@code null}, it defaults
     * to 443, the standard HTTPS port.
     */
    <LogT extends Log, MetricsT extends Metrics> String getBearerToken(String hostname, Integer port);
}
