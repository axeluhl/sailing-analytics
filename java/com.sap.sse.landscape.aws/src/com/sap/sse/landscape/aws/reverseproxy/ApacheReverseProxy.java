package com.sap.sse.landscape.aws.reverseproxy;

import java.util.UUID;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;

/**
 * Represents an HTTP/HTTPS reverse proxy that is able to manage redirection rule sets for
 * "level 7" routing, primarily based on hostnames. Each hostname can be mapped to at most
 * one {@link ApplicationReplicaSet application replica set}, and only one redirect strategy
 * can be selected for such a hostname. The reverse proxy will rewrite such requests, including
 * HTTP to HTTPS forwarding and depending on the strategy selected appending a path to a bare URL
 * with no path components that point to a specific landing page.<p>
 * 
 * Generally, setting a redirect for a hostname will replace an already existing mapping for an
 * equal hostname. Clients using this interface don't have to worry about the details of
 * reloading the configuration or restarting the reverse proxy service; calling the methods
 * through this interface will do what it takes.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApacheReverseProxy {
    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the
     * {@code /index.html} landing page for the application replica set provided.
     */
    <ShardingKey> void setPlainRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, ApplicationProcessMetrics> applicationReplicaSet);
    
    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the
     * {@code /gwt/Home.html} landing page for the application replica set provided.
     */
    <ShardingKey> void setHomeRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, ApplicationProcessMetrics> applicationReplicaSet);

    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the
     * event page for the event with ID {@code eventId} that is expected to be hosted by the
     * application replica set provided.
     */
    <ShardingKey> void setEventRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, ApplicationProcessMetrics> applicationReplicaSet, UUID eventId);

    /**
     * Configures a redirect in this reverse proxy such that requests for it will go to the event series page for the
     * event series identified by the UUID of the leaderboard group that represents the series and which is expected to
     * be hosted by the application replica set provided.
     */
    <ShardingKey> void setEventSeriesRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, ApplicationProcessMetrics> applicationReplicaSet,
            UUID leaderboardGroupId);
    
    /**
     * Removes any existing redirect mapping for the {@code hostname} provided. If no such mapping
     * exists, the method does nothing.
     */
    void removeRedirect(String hostname);
}